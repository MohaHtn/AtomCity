package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import com.squareup.moshi.JsonClass
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response

// We can add a field to PlayerBest30Response via extension or just use it as is if it has it.
// Wait, PlayerBest30Response does not have jacketImageUrl. 
// I should probably check if I can add it or if I should use a wrapper.
// Actually, I can just use findJacketUrlBySongName in the UI.

@JsonClass(generateAdapter = true)
data class JacketUrl(val title: String, val imageUrl: String)

class MaiteaViewModel(
    private val repository: MaiteaRepository,
    private val jacketImages: List<JacketUrl>
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


    // Expose the current page
    internal val _currentPage = MutableStateFlow(1)

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    private val _isLoadingPlays = MutableStateFlow(false)
    private val _isLoadingPlayer = MutableStateFlow(false)
    private val _isLoadingProfiles = MutableStateFlow(false)
    private val _isLoading30BestScores = MutableStateFlow(false)
    private val _isLoadingChartHistory = MutableStateFlow(false)
    private val _isLoadingPlayById = MutableStateFlow(false)
    private val _isLoadingBestPerPlayer = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = combine(
        _isLoadingPlays,
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

    fun fetchMaimaiPaginatedData(page: Int) {
        try {
            viewModelScope.launch {
                _isLoadingPlays.value = true
                repository.getMaiTeaPaginatedData(page).collect { response ->
                    response?.data?.forEach { entry ->
                        entry.jacketImageUrl = findJacketUrlBySongName(entry.song?.name?.jp)
                        // Fallback
                        if (entry.jacketImageUrl == null) {
                            entry.jacketImageUrl = findJacketUrlBySongName(entry.song?.name?.en)
                        }
                    }
                    _playsData.value = response
                    _isLoadingPlays.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
            _isLoadingPlays.value = false
        }
    }

    fun findJacketUrlBySongName(songName: String?): String? {
        return jacketImages.firstOrNull { it.title == songName }?.imageUrl
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

    fun fetchBestPerPlayer(songName: String) {
        viewModelScope.launch {
            try {
                _isLoadingBestPerPlayer.value = true
                repository.getBestPerPlayer(songName).collect {
                    _bestPerPlayer.value = it
                    _isLoadingBestPerPlayer.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching best-per-player: ${e.message}")
                _isLoadingBestPerPlayer.value = false
            }
        }
    }

    fun getPlayById(id: Int, keyHash: String) {
        viewModelScope.launch {
            try {
                _isLoadingPlayById.value = true
                repository.getPlayById(id, keyHash).collect { response ->
                    response?.let { entry ->
                        entry.jacketImageUrl = findJacketUrlBySongName(entry.song?.name?.jp)
                        val currentData = _playsData.value
                        if (currentData != null) {
                            val updatedList = currentData.data.toMutableList()
                            val existingIndex = updatedList.indexOfFirst { it.id == entry.id }
                            if (existingIndex != -1) {
                                updatedList[existingIndex] = entry
                            } else {
                                updatedList.add(entry)
                            }
                            _playsData.value = currentData.copy(data = updatedList)
                        } else {
                            _playsData.value = MaiteaPlaysResponse(data = listOf(entry))
                        }
                    }
                    _isLoadingPlayById.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching play by id: ${e.message}")
                _isLoadingPlayById.value = false
            }
        }
    }
}