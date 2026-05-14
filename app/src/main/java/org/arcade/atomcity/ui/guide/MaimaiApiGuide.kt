package org.arcade.atomcity.ui.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.arcade.atomcity.R
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.LinkText
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager

const val MAIMAI_API_GUIDE_TITLE = "Ajouter une clé API maitea pour maimai"
const val MAIMAI_API_GUIDE_TEXT = "Pour obtenir votre clé API, rendez-vous sur maitea.app et connectez-vous. Si vous n'avez pas de compte, n'hésitez pas à vous en créer un ! Une fois connecté, allez sur 'Profile' dans le menu déroulant."
const val MAIMAI_API_GUIDE_TEXT2 = "Vous arriverez dans cette page."
const val MAIMAI_API_GUIDE_TEXT3 = "En bas de la page, une section 'Access Token' est disponible. Cliquez sur le bouton 'Create Access Token'."
const val MAIMAI_API_GUIDE_URL = "https://maitea.app"
const val MAIMAI_API_GUIDE_STEP1 = "Etape 1 Accès à MaiTea"
const val MAIMAI_API_GUIDE_STEP2 = "Etape 2 Bouton de création de la clé API"
const val MAIMAI_API_GUIDE_STEP3 = "Étape 3 création de la clé API"
const val MAIMAI_API_GUIDE_STEP4 = "Étape 4 Message de confirmation de création de la clé API"
const val MAIMAI_API_GUIDE_SUCCESS = "Lorsque vous voyez le message de succès, vous pouvez copier la clé API et la coller juste en bas."
const val MAIMAI_API_GUIDE_LABEL = "Clé API"
const val MAIMAI_API_GUIDE_PLACEHOLDER = "Exemple: 391|UBvwFPZvDrC3lm9DMSd50e4zXZicB5ssPogJmsw"
const val MAIMAI_API_GUIDE_VALIDATE = "Valider"
const val MAIMAI_API_GUIDE_BACK = "Retour"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiApiGuide(
    apiKeyManager: ApiKeyManager,
    isVisible: MutableState<Boolean>,
    maiteaViewModel: MaiteaViewModel
) {
    val scope = rememberCoroutineScope()
    val existingApiKey = remember { apiKeyManager.getApiKey("maimai") }

    MaimaiApiGuideContent(
        onDismiss = { isVisible.value = false },
        existingApiKey = existingApiKey,
        onSaveApiKey = { text ->
            scope.launch {
                apiKeyManager.saveApiKey("maimai", text)
                maiteaViewModel.addApiKey(text)
                isVisible.value = false
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiApiGuideContent(
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
        MaimaiApiGuideSheetContent(
            existingApiKey = existingApiKey,
            onDismiss = onDismiss,
            onSaveApiKey = onSaveApiKey
        )
    }
}

@Composable
fun MaimaiApiGuideSheetContent(
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
                    text = MAIMAI_API_GUIDE_TITLE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                GuideStep(
                    number = 1,
                    title = "Accès à MaiTea",
                    imageRes = R.drawable.guide_maimai_step1,
                    contentDescription = MAIMAI_API_GUIDE_STEP1
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
                    title = "Navigation vers le profil",
                    imageRes = R.drawable.guide_maimai_step2,
                    contentDescription = MAIMAI_API_GUIDE_STEP2
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
                    title = "Génération du Token",
                    imageRes = R.drawable.guide_maimai_step3,
                    contentDescription = MAIMAI_API_GUIDE_STEP3
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
                    title = "Copie de la clé",
                    imageRes = R.drawable.guide_maimai_step4,
                    contentDescription = MAIMAI_API_GUIDE_STEP4
                ) {
                    Text(
                        text = MAIMAI_API_GUIDE_SUCCESS,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                EnterApiTextBoxContent(
                    existingApiKey = existingApiKey,
                    onDismiss = onDismiss,
                    onSaveApiKey = onSaveApiKey
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun EnterApiTextBoxContent(
    existingApiKey: String?,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    var text by remember { mutableStateOf(existingApiKey ?: "") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isValidInput by remember(text, isError) {
        derivedStateOf { text.isNotBlank() && !isError && text.contains("|") }
    }

    val minApiKeyLength = 10

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Enregistrer la clé",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    when {
                        newText.isBlank() -> {
                            isError = true
                            errorMessage = "La clé API ne peut pas être vide."
                        }
                        !newText.contains("|") -> {
                            isError = true
                            errorMessage = "Format invalide. La clé doit contenir '|'"
                        }
                        newText.length < minApiKeyLength -> {
                            isError = true
                            errorMessage = "Trop courte (min $minApiKeyLength characters)."
                        }
                        else -> {
                            isError = false
                            errorMessage = ""
                        }
                    }
                },
                label = { Text(MAIMAI_API_GUIDE_LABEL) },
                placeholder = {
                    if (existingApiKey.isNullOrBlank()) {
                        Text(MAIMAI_API_GUIDE_PLACEHOLDER)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(MAIMAI_API_GUIDE_BACK)
                }

                Button(
                    onClick = {
                        if (isValidInput) {
                            onSaveApiKey(text)
                        }
                    },
                    enabled = isValidInput,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(MAIMAI_API_GUIDE_VALIDATE)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiApiGuidePreview() {
    AtomCityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MaimaiApiGuideSheetContent(
                existingApiKey = "391|mock_api_key",
                onDismiss = {},
                onSaveApiKey = {}
            )
        }
    }
}
