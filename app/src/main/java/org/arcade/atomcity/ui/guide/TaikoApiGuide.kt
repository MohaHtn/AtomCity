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
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager

const val TAIKO_API_GUIDE_TITLE = "Ajouter une clé API Taiko pour Taiko no Tatsujin"
const val TAIKO_API_GUIDE_TEXT = "Le serveur Taiko permet de synchroniser vos scores et réglages. Pour le moment, seul l'ID utilisateur est requis pour accéder à vos données publiques."
const val TAIKO_API_GUIDE_STEP1_TITLE = "Récupération de l'ID"
const val TAIKO_API_GUIDE_STEP1_DESC = "Etape 1 Récupération de l'ID utilisateur"
const val TAIKO_API_GUIDE_STEP1_TEXT = "Entrez votre ID utilisateur (User Number) pour synchroniser vos données. Vous pouvez le trouver sur votre profil du serveur Taiko."
const val TAIKO_API_GUIDE_LABEL = "ID Utilisateur (User Number)"
const val TAIKO_API_GUIDE_PLACEHOLDER = "Exemple: 152"
const val TAIKO_API_GUIDE_VALIDATE = "Valider"
const val TAIKO_API_GUIDE_BACK = "Retour"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoServerApiGuide(
    apiKeyManager: ApiKeyManager,
    isVisible: MutableState<Boolean>,
    taikoViewModel: TaikoViewModel
) {
    val scope = rememberCoroutineScope()
    val hasExistingApiKey = remember { !apiKeyManager.getApiKey("taiko").isNullOrEmpty() }

    TaikoApiGuideContent(
        onDismiss = { isVisible.value = false },
        hasExistingApiKey = hasExistingApiKey,
        onSaveApiKey = { text ->
            scope.launch {
                apiKeyManager.saveApiKey("taiko", text)
                // taikoViewModel.addApiKey(text) // Si TaikoViewModel a une méthode similaire
                isVisible.value = false
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoApiGuideContent(
    onDismiss: () -> Unit,
    hasExistingApiKey: Boolean,
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
            hasExistingApiKey = hasExistingApiKey,
            onDismiss = onDismiss,
            onSaveApiKey = onSaveApiKey
        )
    }
}

@Composable
fun TaikoApiGuideSheetContent(
    hasExistingApiKey: Boolean,
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
                    imageRes = R.drawable.guide_maimai_step1, // Placeholder image
                    contentDescription = TAIKO_API_GUIDE_STEP1_DESC
                ) {
                    Text(
                        text = TAIKO_API_GUIDE_STEP1_TEXT,
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
                EnterTaikoApiTextBoxContent(
                    hasExistingApiKey = hasExistingApiKey,
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
fun EnterTaikoApiTextBoxContent(
    hasExistingApiKey: Boolean,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isValidInput by remember(text, isError) {
        derivedStateOf { text.isNotBlank() && !isError }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Enregistrer l'ID",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    if (newText.isBlank()) {
                        isError = true
                        errorMessage = "L'ID ne peut pas être vide."
                    } else if (!newText.all { it.isDigit() }) {
                        isError = true
                        errorMessage = "L'ID doit être composé uniquement de chiffres."
                    } else {
                        isError = false
                        errorMessage = ""
                    }
                },
                label = { Text(TAIKO_API_GUIDE_LABEL) },
                placeholder = {
                    if (!hasExistingApiKey) {
                        Text(TAIKO_API_GUIDE_PLACEHOLDER)
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
                    Text(TAIKO_API_GUIDE_BACK)
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
                    Text(TAIKO_API_GUIDE_VALIDATE)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaikoApiGuidePreview() {
    AtomCityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TaikoApiGuideSheetContent(
                hasExistingApiKey = true,
                onDismiss = {},
                onSaveApiKey = {}
            )
        }
    }
}