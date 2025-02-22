package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiTeaRepository
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.arcade.atomcity.model.maitea.MaiTeaResponse

class MainActivityViewModel(private val repository: MaiTeaRepository) : ViewModel() {

    private val _data = MutableStateFlow<MaiTeaResponse?>(null)
    val data: StateFlow<MaiTeaResponse?> = _data

    val dataSize: Int
        get() = _data.value?.data?.size ?: 0

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchData() {
        try  {
            viewModelScope.launch {
                _isLoading.value = true
                repository.getMaiTeaData().collect { response ->
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