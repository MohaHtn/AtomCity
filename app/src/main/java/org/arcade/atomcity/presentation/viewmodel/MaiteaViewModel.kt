package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.arcade.atomcity.data.ImportWorkerStatus
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import com.squareup.moshi.JsonClass
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.ui.core.GlobalUIState

// We can add a field to PlayerBest30Response via extension or just use it as is if it has it.
// Wait, PlayerBest30Response does not have jacketImageUrl. 
// I should probably check if I can add it or if I should use a wrapper.
// Actually, I can just use findJacketUrlBySongName in the UI.

@JsonClass(generateAdapter = true)
data class JacketUrl(val title: String, val imageUrl: String)

class MaiteaViewModel(
   private val repository: MaiteaRepository,
   private val jacketImages: Map<String, String>
) : ViewModel() {

   // StateFlow to hold the plays data
    private val _playsData = MutableStateFlow<MaiteaPlaysResponse?>(null)
    val data: StateFlow<MaiteaPlaysResponse?> = _playsData

    // StateFlow to hold the player details data
    private val _playerData = MutableStateFlow<MaiteaPlayerDetailsResponse?>(null)
    val playerData: StateFlow<MaiteaPlayerDetailsResponse?> = _playerData

    // StateFlow to hold the profiles data
    private val _profiles = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val profiles: StateFlow<Map<String, List<String>>> = _profiles

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

    init {
        observeImportWorkerStatus()
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
            repository.observeImportWorkerStatus().collect { workInfos ->
                val latestWork = workInfos.lastOrNull()
                val workActive = latestWork?.state == WorkInfo.State.ENQUEUED ||
                    latestWork?.state == WorkInfo.State.RUNNING ||
                    latestWork?.state == WorkInfo.State.BLOCKED

                if (workActive) {
                    val progress = latestWork?.progress?.getInt("progress", 0) ?: 0
                    val message = latestWork?.progress?.getString("message") ?: "Importation en cours..."
                    applyImportWorkerStatus(
                        ImportWorkerStatus(
                            isActive = true,
                            state = latestWork?.state?.name?.lowercase() ?: "running",
                            progress = progress,
                            message = message
                        )
                    )
                } else {
                    // Si pas de worker local, on check le statut distant
                    val remoteStatus = repository.refreshImportWorkerStatus()
                    
                    // On ne repasse à false que si le check remote confirme que c'est inactif
                    // Cela évite le glitch si WorkManager met du temps à indexer le job au boot
                    if (remoteStatus.isActive || !GlobalUIState.isImportingMaimaiScores.value || latestWork?.state?.isFinished == true) {
                        applyImportWorkerStatus(remoteStatus)
                    }
                }

                // Une fois le premier check (local ou remote) terminé, on est prêt
                GlobalUIState.isMaimaiImportStateReady.value = true

                if (latestWork?.state?.isFinished == true) {
                    repository.setImportWorkerActive(false)
                    if (latestWork.state == WorkInfo.State.SUCCEEDED) {
                        repository.clearMaiTeaPaginatedCache()
                        viewModelScope.launch {
                            loadMaimaiPaginatedData(_currentPage.value, startImport = false)
                        }
                    } else {
                        _isLoadingPlays.value = false
                    }
                }
            }
        }
    }

    fun getImportWorkerActive(): Boolean {
        return repository.getImportWorkerActive()
    }

    private fun applyImportWorkerStatus(status: ImportWorkerStatus) {
        _isImportingScores.value = status.isActive
        GlobalUIState.isImportingMaimaiScores.value = status.isActive
        _importWorkerState.value = status.state
        _importWorkerProgress.value = status.progress
        _importWorkerMessage.value = status.message
    }

    fun fetchMaimaiPaginatedData(page: Int, startImport: Boolean = false) {
        viewModelScope.launch {
            loadMaimaiPaginatedData(page, startImport)
        }
    }

    private suspend fun loadMaimaiPaginatedData(page: Int, startImport: Boolean) {
        val generation = ++playsRequestGeneration
        try {
            _isLoadingPlays.value = true

            // Only check or start import if explicitly requested (e.g., coming from WelcomeScreen)
            if (startImport) {
                if (repository.isImportWorkerActive()) {
                    applyImportWorkerStatus(
                        ImportWorkerStatus(
                            isActive = true,
                            state = "running",
                            progress = 0,
                            message = "Importation en cours..."
                        )
                    )
                    _playsData.value = null
                    _isLoadingPlays.value = false
                    return
                }

                if (repository.startMaiTeaImport()) {
                    _playsData.value = null
                    _isLoadingPlays.value = false
                    return
                }
            }

            // For normal fetching (pagination or direct access to GameScreen), we don't force _isImportingScores here.
            // The observer in observeImportWorkerStatus handles the global state.
            repository.getMaiTeaPaginatedData(page).collect { response ->
                if (generation != playsRequestGeneration) return@collect
                response?.let { plays ->
                    plays.data.forEach { entry ->
                        entry.jacketImageUrl =
                            findJacketUrlBySongName(entry.song?.name?.jp)
                                ?: findJacketUrlBySongName(entry.song?.name?.en)
                    }
                    _playsData.value = plays
                } ?: run {
                    _playsData.value = null
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

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchQuery = MutableStateFlow("")

    init {
        observeImportWorkerStatus()
        observeSearchQuery()
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