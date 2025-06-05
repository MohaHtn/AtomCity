package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiTeaRepository
import org.arcade.atomcity.model.maitea.PlaysResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse

class MainActivityViewModel(private val repository: MaiTeaRepository) : ViewModel() {

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

    // Expose the current page as a StateFlow
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun onPageChange(newPage: Int) {
        _currentPage.value = newPage
    }

    fun fetchMaimaiPaginatedData(page: Int) {
        try  {
            viewModelScope.launch {
                _isLoading.value = true
                repository.getMaiTeaPaginatedData(page).collect { response ->
                    _playsData.value = response
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
        }
    }

    fun fetchMaimaiPlayerDetails(){
        try {
            viewModelScope.launch {
                _isLoading.value = true
                repository.getMaiTeaPlayerDetails().collect { response ->
                    _playsData.value = response
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
        }
    }
}