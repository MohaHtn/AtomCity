package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.arcade.atomcity.data.ImportWorkerStatus
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.utils.JacketUrl
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.ui.core.GlobalUIState

// We can add a field to PlayerBest30Response via extension or just use it as is if it has it.
// Wait, PlayerBest30Response does not have jacketImageUrl. 
// I should probably check if I can add it or if I should use a wrapper.
// Actually, I can just use findJacketUrlBySongName in the UI.

class MaiteaViewModel(
   private val repository: MaiteaRepository,
   private val jacketImages: Map<String, String>
) : ViewModel() {

   // StateFlow to hold the plays data
    private val _playsData = MutableStateFlow<MaiteaPlaysResponse?>(null)
    val data: StateFlow<MaiteaPlaysResponse?> = _playsData

    private val _hasNextPage = MutableStateFlow(true)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage

    // StateFlow to hold the player details data
    private val _playerData = MutableStateFlow<MaiteaPlayerDetailsResponse?>(null)
    val playerData: StateFlow<MaiteaPlayerDetailsResponse?> = _playerData

    // StateFlow to hold the profiles data
    private val _profiles = MutableStateFlow<Map<String, String>>(emptyMap())
    val profiles: StateFlow<Map<String, String>> = _profiles

    // StateFlow to hold the 30 maimai best scores
    private val _maimaiBestScores = MutableStateFlow<List<PlayerBest30Response>>(emptyList())
    val maimaiBestScores: StateFlow<List<PlayerBest30Response>> = _maimaiBestScores

    // StateFlow to hold the chart history
    private val _chartHistory = MutableStateFlow<List<ChartHistoryResponse>>(emptyList())
    val chartHistory: StateFlow<List<ChartHistoryResponse>> = _chartHistory

    // StateFlow to hold best-per-player responses
    private val _bestPerPlayer = MutableStateFlow<List<org.arcade.atomcity.model.maitea.BestPerPlayerResponse>>(emptyList())
    val bestPerPlayer: StateFlow<List<org.arcade.atomcity.model.maitea.BestPerPlayerResponse>> = _bestPerPlayer

    // StateFlow to hold search results
    private val _searchResults = MutableStateFlow<List<org.arcade.atomcity.model.maitea.BestPerPlayerResponse>>(emptyList())
    val searchResults: StateFlow<List<org.arcade.atomcity.model.maitea.BestPerPlayerResponse>> = _searchResults


    // Expose the current page
    internal val _currentPage = MutableStateFlow(1)
    private var lastPageReached: Int? = null
    @Volatile
    private var playsRequestGeneration = 0

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    private val _isLoadingPlays = MutableStateFlow(false)
    private val _isImportingScores = MutableStateFlow(false)
    val isImportingScores: StateFlow<Boolean> = _isImportingScores
    private val _importWorkerState = MutableStateFlow("idle")
    val importWorkerState: StateFlow<String> = _importWorkerState
    private val _importWorkerProgress = MutableStateFlow(0)
    val importWorkerProgress: StateFlow<Int> = _importWorkerProgress
    private val _importWorkerMessage = MutableStateFlow<String?>(null)
    val importWorkerMessage: StateFlow<String?> = _importWorkerMessage
    private val _isLoadingPlayer = MutableStateFlow(false)
    private val _isLoadingProfiles = MutableStateFlow(false)
    private val _isLoading30BestScores = MutableStateFlow(false)
    private val _isLoadingChartHistory = MutableStateFlow(false)
    private val _isLoadingPlayById = MutableStateFlow(false)
    private val _isLoadingBestPerPlayer = MutableStateFlow(false)

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchQuery = MutableStateFlow("")

    init {
        observeImportWorkerStatus()
        observeSearchQuery()
    }

    val isLoading: StateFlow<Boolean> = combine(
        _isLoadingPlays,
        _isImportingScores,
        _isLoadingPlayer,
        _isLoadingProfiles,
        _isLoading30BestScores,
        _isLoadingChartHistory,
        _isLoadingPlayById,
        _isLoadingBestPerPlayer
    ) { loadings ->
        loadings.any { it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isLoadingDetails: StateFlow<Boolean> = combine(
        _isLoadingChartHistory,
        _isLoadingPlayById,
        _isLoadingBestPerPlayer
    ) { loadings ->
        loadings.any { it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun observeImportWorkerStatus() {
        viewModelScope.launch {
            repository.observeImportWorkerStatus().collect { progressInfo ->
                val workActive = progressInfo?.state == "enqueued" ||
                        progressInfo?.state == "running" ||
                        progressInfo?.state == "blocked"

                if (workActive && progressInfo != null) {
                    applyImportWorkerStatus(
                        ImportWorkerStatus(
                            isActive = true,
                            state = progressInfo.state,
                            progress = progressInfo.progress,
                            message = progressInfo.message ?: "Importation en cours..."
                        )
                    )
                } else if (progressInfo != null && (progressInfo.state == "succeeded" || progressInfo.state == "failed")) {
                    // Worker just finished
                    repository.setImportWorkerActive(false)
                    
                    applyImportWorkerStatus(
                        ImportWorkerStatus(
                            isActive = false,
                            state = progressInfo.state,
                            progress = 100,
                            message = if (progressInfo.state == "succeeded") "Importation terminée" else "Échec de l'importation"
                        )
                    )
                    
                    if (progressInfo.state == "succeeded") {
                        repository.clearMaiTeaPaginatedCache()
                        lastPageReached = null
                        viewModelScope.launch {
                            loadMaimaiPaginatedData(_currentPage.value)
                        }
                    } else {
                        _isLoadingPlays.value = false
                    }
                } else {
                    // No local worker, check if there's an ongoing import on the server
                    val remoteStatus = repository.refreshImportWorkerStatus()
                    
                    if (remoteStatus.isActive) {
                        applyImportWorkerStatus(remoteStatus)
                        repository.startMaiTeaImport()
                    } else {
                        applyImportWorkerStatus(remoteStatus)
                    }
                }

                GlobalUIState.isMaimaiImportStateReady.value = true
            }
        }
    }

    private fun applyImportWorkerStatus(status: ImportWorkerStatus) {
        _isImportingScores.value = status.isActive
        GlobalUIState.isImportingMaimaiScores.value = status.isActive
        _importWorkerState.value = status.state
        _importWorkerProgress.value = status.progress
        _importWorkerMessage.value = status.message
    }

    fun fetchMaimaiPaginatedData(page: Int) {
        viewModelScope.launch {
            loadMaimaiPaginatedData(page)
        }
    }

    private suspend fun loadMaimaiPaginatedData(page: Int) {
        val generation = ++playsRequestGeneration
        try {
            _isLoadingPlays.value = true

            // For normal fetching (pagination or direct access to GameScreen), we don't force _isImportingScores here.
            // The observer in observeImportWorkerStatus handles the global state.
            repository.getMaiTeaPaginatedData(page).collect { response ->
                if (generation != playsRequestGeneration) return@collect

                val hasData = response?.data?.isNotEmpty() == true
                if (hasData) {
                    response.data.forEach { entry ->
                        entry.jacketImageUrl =
                            findJacketUrlBySongName(entry.song?.name?.jp)
                                ?: findJacketUrlBySongName(entry.song?.name?.en)
                    }
                    _playsData.value = response
                    _hasNextPage.value = lastPageReached == null || page < lastPageReached!!
                } else {
                    _hasNextPage.value = false
                    if (page > 1) {
                        lastPageReached = page - 1
                        _currentPage.value = page - 1
                    } else {
                        _playsData.value = response ?: MaiteaPlaysResponse(emptyList())
                        lastPageReached = 1
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
        } finally {
            if (generation == playsRequestGeneration) {
                _isLoadingPlays.value = false
            }
        }
    }

    fun findJacketUrlBySongName(songName: String?): String? {
        return jacketImages[songName]
    }

    fun fetchMaimaiPlayerDetails() {
        try {
            viewModelScope.launch {
                _isLoadingPlayer.value = true
                repository.getMaiTeaPlayerDetails().collect { response ->
                    _playerData.value = response
                    _isLoadingPlayer.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
            _isLoadingPlayer.value = false
        }
    }

    fun fetchProfiles() {
        viewModelScope.launch {
            try {
                _isLoadingProfiles.value = true
                repository.getProfiles().collect {
                    _profiles.value = it
                    _isLoadingProfiles.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching profiles: ${e.message}")
                _isLoadingProfiles.value = false
            }
        }
    }

    fun fetch30BestScores(){
        viewModelScope.launch {
            try {
                _isLoading30BestScores.value = true
                repository.get30BestCharts().collect{
                    _maimaiBestScores.value = it
                    _isLoading30BestScores.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching 30 best scores: ${e.message}")
                _isLoading30BestScores.value = false
            }
        }
    }

    fun fetchChartHistory(songName: String, difficulty: String? = null) {
        viewModelScope.launch {
            try {
                _isLoadingChartHistory.value = true
                repository.getChartHistory(songName, difficulty).collect {
                    _chartHistory.value = it
                    _isLoadingChartHistory.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching chart history: ${e.message}")
                _isLoadingChartHistory.value = false
            }
        }
    }

    fun fetchBestPerPlayer(songName: String, difficulty: String? = null) {
        viewModelScope.launch {
            try {
                _isLoadingBestPerPlayer.value = true
                repository.getBestPerPlayer(songName, difficulty).collect {
                    _bestPerPlayer.value = it
                    _isLoadingBestPerPlayer.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching best-per-player: ${e.message}")
                _isLoadingBestPerPlayer.value = false
            }
        }
    }

    // StateFlow to hold a specific play detail
    private val _selectedPlayDetail = MutableStateFlow<MaiteaApiData?>(null)
    val selectedPlayDetail: StateFlow<MaiteaApiData?> = _selectedPlayDetail

    fun getPlayById(id: Int, keyHash: String) {
        viewModelScope.launch {
            try {
                _isLoadingPlayById.value = true
                repository.getPlayById(id, keyHash).collect { response ->
                    response?.let { entry ->
                        entry.jacketImageUrl = findJacketUrlBySongName(entry.song?.name?.jp)
                        _selectedPlayDetail.value = entry
                    }
                    _isLoadingPlayById.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching play by id: ${e.message}")
                _isLoadingPlayById.value = false
            }
        }
    }

    fun clearSelectedPlayDetail() {
        _selectedPlayDetail.value = null
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                    _isSearching.value = false
                } else {
                    try {
                        _isSearching.value = true
                        repository.searchCharts(query).collect { result ->
                            _searchResults.value = result
                            _isSearching.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("MaiteaViewModel", "Error searching charts: ${e.message}")
                        _searchResults.value = emptyList()
                        _isSearching.value = false
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun searchCharts(query: String) {
        _searchQuery.value = query
    }
}