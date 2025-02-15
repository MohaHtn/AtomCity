package org.arcade.atomcity.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.usecase.GetMaiteatDataUseCase

class MainActivityViewModel(private val getMaiteaDataUseCase: GetMaiteatDataUseCase) : ViewModel() {

    fun fetchData() {
        viewModelScope.launch {
            val data = getMaiteaDataUseCase.execute()
            Log.d("MainActivityViewModel", "Data: $data")
        }
    }
}