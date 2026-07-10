package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.core.GlobalUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApiKeyDialog(
    title: String,
    textBoxLabel: String,
    textBoxExample : String,
    existingApiKey: String?,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    var text by remember { mutableStateOf(existingApiKey ?: "") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isValidInput by remember(text, isError) {
        derivedStateOf { text.isNotBlank() && !isError }
    }

    val minApiKeyLength = 10

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    when {
                        newText.isBlank() -> {
                            isError = true
                            errorMessage = "La clé API ne peut pas être vide."
                        }

                        !newText.contains("|") && GlobalUIState.selectedGameForGuide.value == "maimai" -> {
                            isError = true
                            errorMessage = "Format invalide. La clé doit contenir '|'"
                        }

                        newText.length < minApiKeyLength && GlobalUIState.selectedGameForGuide.value == "maimai" -> {
                            isError = true
                            errorMessage =
                                "Trop courte (min $minApiKeyLength caractères)."
                        }

                        else -> {
                            isError = false
                            errorMessage = ""
                        }
                    }
                },
                label = { Text(textBoxLabel) },
                placeholder = {
                    if (existingApiKey.isNullOrBlank()) {
                        Text(textBoxExample)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (GlobalUIState.selectedGameForGuide.value == "Taiko no Tatsujin") {
                        KeyboardType.Number
                    } else {
                        KeyboardType.Text
                    }
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidInput) {
                        onSaveApiKey(text)
                    }
                },
                enabled = isValidInput
            ) {
                Text(text = "Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Retour")
            }
        }
    )
}
