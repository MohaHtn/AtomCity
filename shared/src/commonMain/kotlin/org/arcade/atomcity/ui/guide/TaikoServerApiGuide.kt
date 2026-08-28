package org.arcade.atomcity.ui.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import atomcity.shared.generated.resources.Res
import atomcity.shared.generated.resources.guide_taiko_step1
import kotlinx.coroutines.launch
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.jetbrains.compose.ui.tooling.preview.Preview

const val TAIKO_API_GUIDE_TITLE = "Ajouter une clé API Taiko pour Taiko no Tatsujin"
const val TAIKO_API_GUIDE_TEXT = "Le serveur Taiko permet de synchroniser vos scores et réglages. Seul l'ID utilisateur est requis pour accéder à vos données publiques."
const val TAIKO_API_GUIDE_STEP1_TITLE = "Récupération de l'ID"
const val TAIKO_API_GUIDE_STEP1_DESC = "Etape 1 Récupération de l'ID utilisateur"
const val TAIKO_API_GUIDE_STEP1_TEXT = "Entrez votre ID utilisateur (User Number) pour synchroniser vos données. Vous pouvez le trouver sur votre profil du serveur Taiko."

const val TAIKO_API_GUIDE_INFO = "Pour accèder à votre compte Taiko no Tatsujin, veuillez entrer vos identifiant, comme si vous vous connectez à https://tatsuj.in/."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoServerApiGuide(
    apiKeyManager: ApiKeyManager,
    isVisible: MutableState<Boolean>,
    taikoViewModel: TaikoViewModel
) {
    val scope = rememberCoroutineScope()
    val existingApiKey by apiKeyManager.getApiKeyFlow("taiko").collectAsState(initial = null)

    TaikoApiGuideContent(
        onDismiss = { isVisible.value = false },
        existingApiKey = existingApiKey,
        onSaveApiKey = { text ->
            scope.launch {
                apiKeyManager.saveApiKey("taiko", text)
                PlatformUtils.log("TaikoServerApiGuide", "API Key saved: $text")
                isVisible.value = false
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoApiGuideContent(
    onDismiss: () -> Unit,
    existingApiKey: String?,
    onSaveApiKey: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        TaikoApiGuideSheetContent(
            existingApiKey = existingApiKey,
            onDismiss = onDismiss,
            onSaveApiKey = onSaveApiKey
        )
    }
}

@Composable
fun TaikoApiGuideSheetContent(
    existingApiKey: String?,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxHeight(0.9f)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = TAIKO_API_GUIDE_TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = TAIKO_API_GUIDE_TEXT,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                GuideStep(
                    number = 1,
                    title = TAIKO_API_GUIDE_STEP1_TITLE,
                    imageRes = Res.drawable.guide_taiko_step1,
                    contentDescription = TAIKO_API_GUIDE_STEP1_DESC
                ) {
                    Text(
                        text = TAIKO_API_GUIDE_STEP1_TEXT,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Preview
@Composable
fun TaikoApiGuidePreview() {
    AtomCityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TaikoApiGuideSheetContent(
                existingApiKey = "152",
                onDismiss = {},
                onSaveApiKey = {}
            )
        }
    }
}
