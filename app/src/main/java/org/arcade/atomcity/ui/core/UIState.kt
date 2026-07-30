package org.arcade.atomcity.ui.core

import androidx.compose.runtime.mutableStateOf

object GlobalUIState {
    var openApiGuide = mutableStateOf(false)
    var selectedGameForGuide = mutableStateOf("")
    var openSaveKeyDialog = mutableStateOf(false)
    var isImportingMaimaiScores = mutableStateOf(false)
    var isMaimaiImportStateReady = mutableStateOf(false)

    // Global Error State
    var globalError = mutableStateOf<String?>(null)

    // Available keys for filtering menus
    var availableApiKeys = mutableStateOf<List<String>>(emptyList())
}
