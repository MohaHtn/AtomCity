package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.arcade.atomcity.model.maitea.MaiTeaResponse

class MainActivityViewModel(private val getMaiteaDataUseCase: GetMaiTeaDataUseCase) : ViewModel() {

    var data: MaiTeaResponse? = null
        private set

    val dataSize: Int
        get() = data?.data?.size ?: 0

    var isLoading = mutableStateOf(true)
        private set

    fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            data = getMaiteaDataUseCase.execute()
            isLoading.value = false
            Log.d("MainActivityViewModel", "Data: $data")
        }
    }
}