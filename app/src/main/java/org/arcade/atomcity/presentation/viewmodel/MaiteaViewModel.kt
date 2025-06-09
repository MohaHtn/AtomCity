package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse

// TODO: Use UseCase instead of Repository directly in ViewModel.
class MaiteaViewModel(private val repository: MaiteaRepository) : ViewModel() {

    // StateFlow to hold the plays data
    private val _playsData = MutableStateFlow<MaiteaPlaysResponse?>(null)
    val data: StateFlow<MaiteaPlaysResponse?> = _playsData

    // Expose the plays data size
    val playsDataSize: Int
        get() = _playsData.value?.data?.size ?: 0

    // StateFlow to hold the player details data
    private val _playerData = MutableStateFlow<MaiteaPlayerDetailsResponse?>(null)
    val playerData: StateFlow<MaiteaPlayerDetailsResponse?> = _playerData

    // Expose the current page
    internal val _currentPage = MutableStateFlow(1);

    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    private val _isLoadingPlays = MutableStateFlow(false)
    private val _isLoadingPlayer = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(_isLoadingPlays, _isLoadingPlayer) { playsLoading, playerLoading ->
                playsLoading || playerLoading
            }.collect { value ->
                this@apply.value = value
            }
        }
    }

    fun fetchMaimaiPaginatedData(page: Int) {
        try {
            viewModelScope.launch {
                _isLoadingPlays.value = true
                repository.getMaiTeaPaginatedData(page).collect { response ->
                    _playsData.value = response
                    _isLoadingPlays.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
            _isLoadingPlays.value = false
        }
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
}