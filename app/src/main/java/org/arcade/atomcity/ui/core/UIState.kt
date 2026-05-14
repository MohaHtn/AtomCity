package org.arcade.atomcity.ui.core

import androidx.compose.runtime.mutableStateOf

object GlobalUIState {
    var openApiGuide = mutableStateOf(false)
    var selectedGameForGuide = mutableStateOf("")
}
