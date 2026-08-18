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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import atomcity.shared.generated.resources.Res
import atomcity.shared.generated.resources.guide_maimai_step1
import atomcity.shared.generated.resources.guide_maimai_step2
import atomcity.shared.generated.resources.guide_maimai_step3
import atomcity.shared.generated.resources.guide_maimai_step4
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.LinkText
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.jetbrains.compose.ui.tooling.preview.Preview

const val MAIMAI_API_GUIDE_TITLE = "Ajouter une clé API Scorefetcher pour maimai FiNALE"
const val MAIMAI_API_GUIDE_URL = "https://maitea.app"

// Titles
const val MAIMAI_API_TITLE_STEP1 = "Accès à Scorefetcher"
const val MAIMAI_API_TITLE_STEP2 = "Navigation vers le profil"
const val MAIMAI_API_TITLE_STEP3 = "Génération du Token"
const val MAIMAI_API_TITLE_STEP4 = "Message de confirmation de création de la clé API"

// Texts
const val MAIMAI_API_GUIDE_TEXT = "Pour obtenir votre clé API, rendez-vous sur maitea.app et connectez-vous. Si vous n'avez pas de compte, n'hésitez pas à vous en créer un ! Une fois connecté, allez sur 'Profile' dans le menu déroulant."
const val MAIMAI_API_GUIDE_TEXT2 = "Vous arriverez dans cette page. Cliquez sur le menu du site puis \"Settings\". "
const val MAIMAI_API_GUIDE_TEXT3 = "En bas de la page, une section 'Access Token' est disponible. Cliquez sur le bouton 'Create Access Token'."
const val MAIMAI_API_GUIDE_TEXT4 = "Lorsque vous voyez le message de succès, vous pouvez copier la clé API et la coller juste en bas."


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiApiGuide(
    apiKeyManager: ApiKeyManager,
    isVisible: MutableState<Boolean>,
    maimaiViewModel: MaimaiViewModel
) {
    val existingApiKey by apiKeyManager.getApiKeyFlow("maimai").collectAsState(initial = null)

    MaimaiApiGuideContent(
        onDismiss = { isVisible.value = false },
        existingApiKey = existingApiKey,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiApiGuideContent(
    onDismiss: () -> Unit,
    existingApiKey: String?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        MaimaiApiGuideSheetContent(
            existingApiKey = existingApiKey,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun MaimaiApiGuideSheetContent(
    existingApiKey: String?,
    onDismiss: () -> Unit,
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
                    text = MAIMAI_API_GUIDE_TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                GuideStep(
                    number = 1,
                    title = MAIMAI_API_TITLE_STEP1,
                    imageRes = Res.drawable.guide_maimai_step1,
                    contentDescription = MAIMAI_API_TITLE_STEP1
                ) {
                    LinkText(
                        fullText = MAIMAI_API_GUIDE_TEXT,
                        linkText = "maitea.app",
                        url = MAIMAI_API_GUIDE_URL,
                    )
                }
            }

            item {
                GuideStep(
                    number = 2,
                    title = MAIMAI_API_TITLE_STEP2,
                    imageRes = Res.drawable.guide_maimai_step2,
                    contentDescription = MAIMAI_API_GUIDE_TEXT2
                ) {
                    Text(
                        text = MAIMAI_API_GUIDE_TEXT2,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                GuideStep(
                    number = 3,
                    title = MAIMAI_API_TITLE_STEP3,
                    imageRes = Res.drawable.guide_maimai_step3,
                    contentDescription = MAIMAI_API_GUIDE_TEXT3
                ) {
                    Text(
                        text = MAIMAI_API_GUIDE_TEXT3,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                GuideStep(
                    number = 4,
                    title = MAIMAI_API_TITLE_STEP4,
                    imageRes = Res.drawable.guide_maimai_step4,
                    contentDescription = MAIMAI_API_GUIDE_TEXT4
                ) {
                    Text(
                        text = MAIMAI_API_GUIDE_TEXT4,
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
fun MaimaiApiGuidePreview() {
    AtomCityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MaimaiApiGuideSheetContent(
                existingApiKey = "391|mock_api_key",
                onDismiss = {},
            )
        }
    }
}
