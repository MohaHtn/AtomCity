package org.arcade.atomcity.ui.core.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.guide.MaimaiApiGuide
import org.arcade.atomcity.ui.guide.TaikoServerApiGuide
import org.arcade.atomcity.ui.guide.apistatus.ApiCheckList
import org.arcade.atomcity.utils.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettings(
    onBackClick: () -> Unit,
    apiKeyManager: ApiKeyManager,
    maiteaViewModel: MaiteaViewModel,
    taikoViewModel: TaikoViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres des clés API") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn {
                item {
                    Text(
                        text = "Voici l'état des clés API que vous avez entré dans l'application. " +
                                "Vous pouvez les modifier, les supprimer ou ajouter de nouvelles si nécessaire.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                item {
                    ApiCheckList(
                        apiChecklistState = apiKeyManager.getApiChecklistState(),
                    )
                }
            }

            if (GlobalUIState.openApiGuide.value) {
                when (GlobalUIState.selectedGameForGuide.value) {
                    "Taiko no Tatsujin" -> {
                        TaikoServerApiGuide(
                            apiKeyManager = apiKeyManager,
                            isVisible = GlobalUIState.openApiGuide,
                            taikoViewModel = taikoViewModel
                        )
                    }
                    else -> {
                        MaimaiApiGuide(
                            apiKeyManager = apiKeyManager,
                            isVisible = GlobalUIState.openApiGuide,
                            maiteaViewModel = maiteaViewModel
                        )
                    }
                }
            }
        }
    }
}
