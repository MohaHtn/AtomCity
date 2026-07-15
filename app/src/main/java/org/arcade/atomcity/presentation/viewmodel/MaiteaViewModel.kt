package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
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
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response

// TODO: Use UseCase instead of Repository directly in ViewModel.

@JsonClass(generateAdapter = true)
data class JacketUrl(val title: String, val imageUrl: String)

class MaiteaViewModel(
    private val repository: MaiteaRepository,
    private val jacketImages: List<JacketUrl>
) : ViewModel() {

    // StateFlow to hold the plays data
    private val _playsData = MutableStateFlow<MaiteaPlaysResponse?>(null)
    val data: StateFlow<MaiteaPlaysResponse?> = _playsData

    // Expose the plays data size
    val playsDataSize: Int
        get() = _playsData.value?.data?.size ?: 0

    // StateFlow to hold the player details data
    private val _playerData = MutableStateFlow<MaiteaPlayerDetailsResponse?>(null)
    val playerData: StateFlow<MaiteaPlayerDetailsResponse?> = _playerData

    // StateFlow to hold the profiles data
    private val _profiles = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val profiles: StateFlow<Map<String, List<String>>> = _profiles

    // StateFlow to hold the 30 maimai best scores
    private val _maimaiBestScores = mutableStateOf<PlayerBest30Response?>(null)
    val maimaiBestScores: StateFlow<PlayerBest30Response?> = _maimaiBestScores


    private val hashKey = mutableStateOf("")

    // Expose the current page
    internal val _currentPage = MutableStateFlow(1)

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    private val _isLoadingPlays = MutableStateFlow(false)
    private val _isLoadingPlayer = MutableStateFlow(false)
    private val _isLoadingProfiles = MutableStateFlow(false)
    private val _isLoading30BestScores = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = combine(
        _isLoadingPlays,
        _isLoadingPlayer,
        _isLoadingProfiles
    ) { playsLoading, playerLoading, profilesLoading ->
        playsLoading || playerLoading || profilesLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun fetchMaimaiPaginatedData(page: Int) {
        try {
            viewModelScope.launch {
                _isLoadingPlays.value = true
                repository.getMaiTeaPaginatedData(page).collect { response ->
                    response?.data?.forEach { entry ->
                        entry.jacketImageUrl = findJacketUrlBySongName(entry.song?.name?.jp)
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

    fun addApiKey(apikey: String) {
        try {
            viewModelScope.launch {
                repository.addApiKey(apikey).collect { success ->
                    if (success) {
                        Log.d("MaiteaViewModel", "API key added successfully")
                    } else {
                        Log.e("MaiteaViewModel", "Failed to add API key")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MaiteaViewModel", "Error adding API key: ${e.message}")
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
                repository.get30BestCharts(this.hashKey).collect{
                    _maimaiBestScores.value = it
                    _isLoading30BestScores.value = false
                }
            } catch (e: Exception) {
                Log.e("MaiteaViewModel", "Error fetching 30 best scores: ${e.message}")
                _isLoading30BestScores.value = false
            }
        }
    }
}