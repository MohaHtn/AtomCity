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
import org.arcade.atomcity.ui.core.LinkText
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.jetbrains.compose.ui.tooling.preview.Preview

const val TAIKO_API_GUIDE_TITLE = "Connectez-vous sur Tatsuj.in"
const val TAIKO_API_GUIDE_INFO = "Pour accèder à votre compte Taiko no Tatsujin pour consulter vos scores et votre profil, vous pouvez entrer vos identifiants comme si vous vous connectez à https://tatsuj.in/."
const val TAIKO_API_GUIDE_URL = "https://tatsuj.in"

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
        TaikoApiGuideSheetContent()
    }
}

@Composable
fun TaikoApiGuideSheetContent() {
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
                GuideStep(
                    number = 1,
                    title = "Se connecter",
                    imageRes = Res.drawable.guide_taiko_step1,
                    contentDescription = "Se connecter"
                ){
                    LinkText(
                        fullText = TAIKO_API_GUIDE_INFO,
                        linkText = "https://tatsuj.in",
                        url = TAIKO_API_GUIDE_URL
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
            TaikoApiGuideSheetContent()
        }
    }
}
