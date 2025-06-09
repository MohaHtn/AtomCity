package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val isLoading = MutableStateFlow(false)
    val isLoadingMusicDetails = MutableStateFlow(false)
    val isLoadingScores = MutableStateFlow(false)

    internal val _currentPage = MutableStateFlow(1)

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    fun fetchPlayHistoryPlayData(page: Int) {
        isLoadingScores.value = true
        viewModelScope.launch {
            try {
                usecase.execute(page.toString()).collect { response ->
                    _scoresData.value = response
                }
            } catch (e: Exception) {
                Log.e("TaikoScoresViewModel", "Error: ${e.message}")
                isLoadingScores.value = false
            } finally {
                isLoadingScores.value = false
            }
        }
    }

    fun fetchMusicDetails() {
        isLoadingMusicDetails.value = true
        viewModelScope.launch {
            try {
                usecase.executeMusicDetails().collect { response ->
                    _musicDetailsData.value = response
                }
            } catch (e: Exception) {
                Log.e("TaikoScoresViewModel", "Error: ${e.message}")
                isLoadingMusicDetails.value = false
            } finally {
                isLoadingMusicDetails.value = false
            }
        }
    }
}