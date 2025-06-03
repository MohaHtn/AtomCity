package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiTeaRepository
import org.arcade.atomcity.model.maitea.MaiTeaResponse

class MainActivityViewModel(private val repository: MaiTeaRepository) : ViewModel() {

    private val _data = MutableStateFlow<MaiTeaResponse?>(null)
    val data: StateFlow<MaiTeaResponse?> = _data

    val dataSize: Int
        get() = _data.value?.data?.size ?: 0

    val currentPage: Int
        get() = currentPage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchMaimaiPaginatedData(page: Int) {
        try  {
            viewModelScope.launch {
                _isLoading.value = true
                repository.getMaiTeaPaginatedData(page).collect { response ->
                    _data.value = response
                    _isLoading.value = false
                    Log.d("MainActivityViewModel", "Data: ${_data.value}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivityViewModel", "Error: ${e.message}")
        }
    }
}