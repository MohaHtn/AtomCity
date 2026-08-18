package org.arcade.atomcity.presentation.viewmodel

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
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.domain.usecase.*
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.ui.core.GlobalUIState
import kotlin.time.Duration.Companion.milliseconds
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.concurrent.Volatile

class ScorefetcherViewModel(
    private val scoresUseCase: GetScorefetcherScoresUseCase,
    private val profileUseCase: GetScorefetcherProfileUseCase,
    private val importUseCase: ScorefetcherImportUseCase,
    private val analyticsUseCase: GetScorefetcherAnalyticsUseCase,
    private val jacketUseCase: GetScorefetcherJacketUseCase
) : ViewModel() {

   // StateFlow to hold the plays data
    private val _playsData = MutableStateFlow<ScorefetcherPlaysResponse?>(null)
    val data: StateFlow<ScorefetcherPlaysResponse?> = _playsData

    private val _hasNextPage = MutableStateFlow(true)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage

    // StateFlow to hold the player details data
    private val _playerData = MutableStateFlow<ScorefetcherPlayerDetailsResponse?>(null)
    val playerData: StateFlow<ScorefetcherPlayerDetailsResponse?> = _playerData

    // StateFlow to hold the profiles data
    private val _profiles = MutableStateFlow<Map<String, String>>(emptyMap())
    val profiles: StateFlow<Map<String, String>> = _profiles

    // StateFlow to hold the ratings data
    private val _ratings = MutableStateFlow<Map<String, Int>>(emptyMap())
    val ratings: StateFlow<Map<String, Int>> = _ratings

    // StateFlow to hold the 30 maimai best scores
    private val _maimaiBestScores = MutableStateFlow<List<PlayerBest30Response>>(emptyList())
    val maimaiBestScores: StateFlow<List<PlayerBest30Response>> = _maimaiBestScores

    // StateFlow to hold the chart history
    private val _chartHistory = MutableStateFlow<List<ChartHistoryResponse>>(emptyList())
    val chartHistory: StateFlow<List<ChartHistoryResponse>> = _chartHistory

    // StateFlow to hold best-per-player responses
    private val _bestPerPlayer = MutableStateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse>>(emptyList())
    val bestPerPlayer: StateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse>> = _bestPerPlayer

    // StateFlow to hold search results
    private val _searchResults = MutableStateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse>>(emptyList())
    val searchResults: StateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse>> = _searchResults

    // StateFlow to hold most played charts
    private val _mostPlayedCharts = MutableStateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry>>(emptyList())
    val mostPlayedCharts: StateFlow<List<org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry>> = _mostPlayedCharts


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
    val isLoadingPlayById: StateFlow<Boolean> = _isLoadingPlayById

    private val _isLoadingBestPerPlayer = MutableStateFlow(false)

    private val _isLoadingMostPlayed = MutableStateFlow(false)
    val isLoadingMostPlayed: StateFlow<Boolean> = _isLoadingMostPlayed

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
        _isLoadingBestPerPlayer,
        _isLoadingMostPlayed
    ) { loadings ->
        loadings.any { it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isLoadingDetails: StateFlow<Boolean> = combine(
        _isLoadingPlays,
        _isLoadingChartHistory,
        _isLoadingPlayById,
        _isLoadingBestPerPlayer,
        _isLoadingMostPlayed
    ) { loadings ->
        loadings.any { it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun observeImportWorkerStatus() {
        viewModelScope.launch {
            importUseCase.observeImportWorkerStatus().collect { progressInfo ->
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
                    importUseCase.setImportWorkerActive(false)
                    
                    applyImportWorkerStatus(
                        ImportWorkerStatus(
                            isActive = false,
                            state = progressInfo.state,
                            progress = 100,
                            message = if (progressInfo.state == "succeeded") "Importation terminée" else "Échec de l'importation"
                        )
                    )
                    
                    if (progressInfo.state == "succeeded") {
                        importUseCase.clearScorefetcherPaginatedCache()
                        lastPageReached = null
                        viewModelScope.launch {
                            loadMaimaiPaginatedData(_currentPage.value)
                        }
                    } else {
                        _isLoadingPlays.value = false
                    }
                } else {
                    // No local worker, check if there's an ongoing import on the server
                    val remoteStatus = importUseCase.refreshImportWorkerStatus()
                    
                    if (remoteStatus.isActive) {
                        applyImportWorkerStatus(remoteStatus)
                        importUseCase.startScorefetcherImport()
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

            scoresUseCase.getScorefetcherPaginatedData(page).collect { response ->
                if (generation != playsRequestGeneration) return@collect

                val hasData = response?.data?.isNotEmpty() == true
                if (hasData) {
                    _playsData.value = response
                    _hasNextPage.value = lastPageReached == null || page < lastPageReached!!
                } else {
                    _hasNextPage.value = false
                    if (page > 1) {
                        lastPageReached = page - 1
                        _currentPage.value = page - 1
                    } else {
                        _playsData.value = response ?: ScorefetcherPlaysResponse(emptyList())
                        lastPageReached = 1
                    }
                }
            }
        } catch (e: Exception) {
            // Log.e("MainActivityViewModel", "Error: ${e.message}")
        } finally {
            if (generation == playsRequestGeneration) {
                _isLoadingPlays.value = false
            }
        }
    }



    fun fetchMaimaiPlayerDetails() {
        try {
            viewModelScope.launch {
                _isLoadingPlayer.value = true
                profileUseCase.getScorefetcherPlayerDetails().collect { response ->
                    _playerData.value = response
                    _isLoadingPlayer.value = false
                }
            }
        } catch (e: Exception) {
            //Log.e("MainActivityViewModel", "Error: ${e.message}")
            _isLoadingPlayer.value = false
        }
    }

    fun fetchProfiles() {
        viewModelScope.launch {
            try {
                _isLoadingProfiles.value = true
                profileUseCase.getProfiles().collect { profilesMap ->
                    _profiles.value = profilesMap.filterValues { it != null }.mapValues { it.value!! }
                    _isLoadingProfiles.value = false
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching profiles: ${e.message}")
                _isLoadingProfiles.value = false
            }

            try {
                profileUseCase.getRatings().collect { ratingsMap ->
                    _ratings.value = ratingsMap.filterValues { it != null }.mapValues { it.value!! }
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching ratings: ${e.message}")
            }
        }
    }

    fun fetch30BestScores(){
        viewModelScope.launch {
            try {
                _isLoading30BestScores.value = true
                scoresUseCase.get30BestCharts().collect{
                    _maimaiBestScores.value = it
                    _isLoading30BestScores.value = false
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching 30 best scores: ${e.message}")
                _isLoading30BestScores.value = false
            }
        }
    }

    fun fetchChartHistory(songName: String, difficulty: String? = null) {
        viewModelScope.launch {
            try {
                _isLoadingChartHistory.value = true
                analyticsUseCase.getChartHistory(songName, difficulty).collect {
                    _chartHistory.value = it
                    _isLoadingChartHistory.value = false
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching chart history: ${e.message}")
                _isLoadingChartHistory.value = false
            }
        }
    }

    fun fetchBestPerPlayer(songName: String, difficulty: String? = null) {
        viewModelScope.launch {
            try {
                _isLoadingBestPerPlayer.value = true
                analyticsUseCase.getBestPerPlayer(songName, difficulty).collect {
                    _bestPerPlayer.value = it
                    _isLoadingBestPerPlayer.value = false
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching best-per-player: ${e.message}")
                _isLoadingBestPerPlayer.value = false
            }
        }
    }

    // StateFlow to hold a specific play detail
    private val _selectedPlayDetail = MutableStateFlow<ScorefetcherApiData?>(null)
    val selectedPlayDetail: StateFlow<ScorefetcherApiData?> = _selectedPlayDetail

    fun getPlayById(id: Int, keyHash: String) {
        viewModelScope.launch {
            try {
                _isLoadingPlayById.value = true
                scoresUseCase.getPlayById(id, keyHash).collect { response ->
                    response?.let { entry ->
                        _selectedPlayDetail.value = entry
                    }
                }
            } catch (e: Exception) {
                //Log.e("ScorefetcherViewModel", "Error fetching play by id: ${e.message}")
            } finally {
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
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                    _isSearching.value = false
                } else {
                    try {
                        _isSearching.value = true
                        scoresUseCase.searchCharts(query).collect { result ->
                            _searchResults.value = result
                            _isSearching.value = false
                        }
                    } catch (e: Exception) {
                        // Log.e("ScorefetcherViewModel", "Error searching charts: ${e.message}")
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

    fun fetchMostPlayedCharts(isGlobal: Boolean, period: String, date: String? = null, groupByHashkey: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoadingMostPlayed.value = true
                val flow = if (isGlobal) {
                    analyticsUseCase.getMostPlayed(period = period, date = date, groupByHashkey = groupByHashkey)
                } else {
                    analyticsUseCase.getMostPlayedByHash(period = period, date = date, groupByHashkey = groupByHashkey)
                }

                flow.collect { entries ->
                    _mostPlayedCharts.value = entries
                    _isLoadingMostPlayed.value = false
                }
            } catch (e: Exception) {
                // Log.e("ScorefetcherViewModel", "Error fetching most played charts: ${e.message}")
                _isLoadingMostPlayed.value = false
            }
        }
    }

    fun findJacketUrlBySongName(songName: String?): String? {
        return jacketUseCase.findJacketUrlBySongName(songName)
    }
}
