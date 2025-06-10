package org.arcade.atomcity.ui.guide

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.core.openApiGuide
import org.arcade.atomcity.utils.ApiKeyManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoServerApiGuide(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>) {

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            openApiGuide.value = false
        },
        sheetState = sheetState
    ) {
        LazyColumn (
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                TaikoApiGuideContent(apiKeyManager = apiKeyManager, isVisible = isVisible)
            }
        }
    }
}

@Composable
fun TaikoApiGuideContent(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>) {
    Text(
        text = "Taiko Server API Guide",
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = "This guide provides information on how to use the Taiko Server API. " +
                "Make sure to have your API key ready for authentication."
    )
    // Add more content here as needed
    // For example, you can add sections for different API endpoints, usage examples, etc.
}