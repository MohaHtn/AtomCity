package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse

class TaikoViewModel(private val usecase: GetTaikoServerDataUseCase) : ViewModel() {

    // StateFlow to hold the music details data
    private val _scoresData = MutableStateFlow<TaikoServerPlayHistoryResponse?>(null)
    val scoresData = _scoresData

    // StateFlow to hold the music details data
    private val _musicDetailsData = MutableStateFlow<TaikoServerMusicDetailsResponse?>(null)
    val musicDetailsData = _musicDetailsData

    // StateFlow to hold the user settings data
    private val _userSettingsData = MutableStateFlow<TaikoServerUserSettingsResponse?>(null)
    val userSettingsData = _userSettingsData

    val isLoading = MutableStateFlow(false)
    val isLoadingMusicDetails = MutableStateFlow(false)
    val isLoadingUserSettings = MutableStateFlow(false)
    val isLoadingScores = MutableStateFlow(false)

    internal val _currentPage = MutableStateFlow(1)

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    suspend fun fetchPlayHistoryPlayData(page: Int) {
        isLoadingScores.value = true
        try {
            usecase.getPlayHistoryFlow(page.toString()).collect { response ->
                _scoresData.value = response
            }
        } catch (e: Exception) {
            Log.e("TaikoScoresViewModel", "Error: ${e.message}")
            isLoadingScores.value = false
        } finally {
            isLoadingScores.value = false
        }
    }

    suspend fun fetchMusicDetails() {
        isLoadingMusicDetails.value = true
        try {
            usecase.getMusicDetailsFlow().collect { response ->
                _musicDetailsData.value = response
            }
        } catch (e: Exception) {
            Log.e("TaikoScoresViewModel", "Error: ${e.message}")
            isLoadingMusicDetails.value = false
        } finally {
            isLoadingMusicDetails.value = false
        }
    }

    suspend fun getUserSettings() {
        isLoadingUserSettings.value = true
        try {
            usecase.getUserSettingsFlow().collect { response ->
                _userSettingsData.value = response
            }
        } catch (e: Exception) {
            Log.e("TaikoViewModel", "Error fetching user settings: ${e.message}")
            isLoadingUserSettings.value = false
        } finally {
            isLoadingUserSettings.value = false
        }
    }

    fun mergeMusicDetailsWithScores() {
        val scores = scoresData.value
        val musicDetails = musicDetailsData.value

        val mergedData = scores!!.copy(
            taikoServerSongHistoryData = scores.taikoServerSongHistoryData.map { score ->
                val musicDetail = musicDetails?.entries?.find { it.key == score.songId.toString() }?.value
                if (musicDetail != null) {
                    score.copy(
                        musicName = musicDetail.songName,
                        musicArtist = musicDetail.artistName
                    )
                } else {
                    score
                }
            }
        )
        _scoresData.value = mergedData
    }


    fun getScores() {
        viewModelScope.launch {
            fetchMusicDetails()
            fetchPlayHistoryPlayData(1)
            getUserSettings()
            mergeMusicDetailsWithScores()
        }
    }
}