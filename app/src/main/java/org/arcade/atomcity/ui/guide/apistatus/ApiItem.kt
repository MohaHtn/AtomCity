package org.arcade.atomcity.ui.guide.apistatus

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.core.context.GlobalContext

@Composable
internal fun ApiItem(
    name: String,
    key: String = name.lowercase().replace(" ", ""),
) {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current
    
    // In preview mode, we don't use Koin or ApiKeyManager
    val apiKeyManager = if (inspectionMode) null else ApiKeyManager(context)
    val maiteaRepository = if (inspectionMode) null else GlobalContext.get().get<MaiteaRepository>()
    
    val scope = rememberCoroutineScope()
    
    val apiKey by if (inspectionMode) {
        remember { mutableStateOf("Preview Key") }
    } else {
        apiKeyManager!!.getApiKeyFlow(key).collectAsState(initial = null)
    }
    
    val hasKeyActual = apiKey != null
    val dialogVisible = remember { mutableStateOf(false) }
    val revealed = remember { mutableStateOf(false) }
    
    // Actual supported games (for now!)
    val isGameSupported = name == "maimai" || name == "Taiko no Tatsujin"

    fun maskKey(key: String?): String {
        if (key.isNullOrBlank()) return ""
        val visibleStart = 4.coerceAtMost(key.length)
        val visibleEnd = 4.coerceAtMost(key.length - visibleStart)
        return when {
            key.length <= visibleStart + visibleEnd -> "*".repeat(key.length)
            else -> key.take(visibleStart) + " … " + key.takeLast(visibleEnd)
        }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clickable(enabled = hasKeyActual) { dialogVisible.value = true },
        leadingContent = {
            Icon(
                imageVector = if (hasKeyActual) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                contentDescription = if (hasKeyActual) "API configurée" else "API non configurée",
                tint = if (hasKeyActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = if (hasKeyActual) "Clé configurée • ${maskKey(apiKey)}" else "Aucun accès configuré",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            Row() {

                TextButton(
                    onClick = {
                        GlobalUIState.selectedGameForGuide.value = name
                        GlobalUIState.openSaveKeyDialog.value = true
                    },
                    enabled = isGameSupported
                ) {
                    Column(
                    ) {
                        Icon(
                            imageVector = if (hasKeyActual) Icons.Rounded.Edit else Icons.Rounded.Add,
                            contentDescription = if (hasKeyActual) "Modifier la clé API" else "Ajouter une clé API",
                            modifier = Modifier.align( androidx.compose.ui.Alignment.CenterHorizontally)
                        )
                        Text(
                            text = if (hasKeyActual) "Modifier" else "Ajouter",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                }

                TextButton(
                    onClick = {
                        GlobalUIState.selectedGameForGuide.value = name
                        GlobalUIState.openApiGuide.value = true
                    },
                    enabled = isGameSupported
                ) {
                    Column(
                        modifier = Modifier.align( androidx.compose.ui.Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Guide",
                            modifier = Modifier.align( androidx.compose.ui.Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Guide",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

        }
    )

    if (dialogVisible.value) {
        ApiItemDialog(
            name = name,
            apiKey = apiKey,
            revealed = revealed.value,
            onRevealClick = { revealed.value = !revealed.value },
            onDismiss = {
                dialogVisible.value = false
                revealed.value = false
            },
            onDeleteClick = {
                scope.launch {
                    if (name == "maimai" && apiKey != null) {
                        try {
                            maiteaRepository?.removeApiKey(apiKey!!)?.collect {
                                Log.d("ApiItem", "Clé API supprimée du serveur : ${it.message}")
                            }
                        } catch (e: Exception) {
                            Log.e("ApiItem", "Erreur lors de la suppression de la clé sur le serveur", e)
                        }
                    }
                    apiKeyManager?.removeApiKey(key)
                    dialogVisible.value = false
                    revealed.value = false
                }
            },
            maskKey = ::maskKey,
            text = 
                when (GlobalUIState.selectedGameForGuide.value) {
                    "maimai" -> "Votre clé API pour maimai est affichée ci-dessous."
                    "Taiko no Tatsujin" -> "Votre ID utilisateur pour Taiko no Tatsujin est affiché ci-dessous."
                    else -> ""
                }
            
        )
        // TODO: DEBUG ONLY
        apiKeyManager?.logAllApiKeys()
    }

    if (GlobalUIState.openSaveKeyDialog.value && GlobalUIState.selectedGameForGuide.value == name) {
        EditApiKeyDialog(
            title = when (GlobalUIState.selectedGameForGuide.value) {
                "maimai" -> "Ajouter/Modifier la clé API pour maimai"
                "Taiko no Tatsujin" -> "Ajouter/Modifier l'ID Utilisateur pour Taiko no Tatsujin"
                else -> "Ajouter/Modifier"
            },
            existingApiKey = apiKey,
            onDismiss = { GlobalUIState.openSaveKeyDialog.value = false },
            onSaveApiKey = { newKey ->
                scope.launch {
                    apiKeyManager?.saveApiKey(key, newKey)
                    GlobalUIState.openSaveKeyDialog.value = false
                }
            },
            textBoxLabel = when (GlobalUIState.selectedGameForGuide.value) {
                "maimai" -> "Enregistrer la clé"
                "Taiko no Tatsujin" -> "Enregistrer l'ID utilisateur"
                else -> ""
            },
            textBoxExample = when (GlobalUIState.selectedGameForGuide.value) {
                "maimai" -> "Exemple : Exemple: 391|UBvwFPZvDrC3lm9DMSd50e4zXZicB5ssPogJmsw"
                "Taiko no Tatsujin" -> "Exemple : 1234567890"
                else -> ""
            }
        )
    }
}

@Composable
fun ApiItemDialog(
    name: String,
    text: String,
    apiKey: String?,
    revealed: Boolean,
    onRevealClick: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    maskKey: (String?) -> String
) {
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = {
                Text(text = "Supprimer la clé ?")
            },
            text = {
                Column() {
                    Text(text = "Êtes-vous sûr de vouloir supprimer la clé pour $name ?")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Cette action est irréversible. La clé sera également supprimée sur le serveur distant.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation.value = false
                        onDeleteClick()
                        
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation.value = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clé API pour $name",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column {
                Text(
                    text = text + "Appuyez sur ci-dessous pour révéler ou masquer sa valeur.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Note : Ne partagez jamais cette information avec d'autres personnes ou applications non fiables.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                SelectionContainer {
                    Text(
                        text = when {
                            apiKey.isNullOrBlank() -> "(aucune clé)"
                            revealed -> apiKey
                            else -> maskKey(apiKey)
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .clickable(enabled = !apiKey.isNullOrBlank()) {
                                onRevealClick()
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = onDismiss) {
                Text("Fermer")
            }
        },
        dismissButton = {
            if (!apiKey.isNullOrBlank()) {
                TextButton(onClick = { showDeleteConfirmation.value = true }) {
                    Text("Supprimer la clé", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ApiItemConfiguredPreview() {
    AtomCityTheme {
        ApiItem(
            name = "maimai",
            key = "maimai",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiItemNotConfiguredPreview() {
    AtomCityTheme {
        ApiItem(
            name = "Taiko no Tatsujin",
            key = "taiko",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiItemDialogPreviewLongKeyAPI() {
    AtomCityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ApiItemDialog(
                name = "maimai",
                text = "Votre clé API pour maimai est affichée ci-dessous.",
                apiKey = "1234567890abcdef1234567890abcdef1234567890abcdef",
                revealed = false,
                onRevealClick = {},
                onDismiss = {},
                onDeleteClick = {},
                maskKey = { key ->
                    if (key.isNullOrBlank()) ""
                    else {
                        val visibleStart = 4.coerceAtMost(key.length)
                        val visibleEnd = 4.coerceAtMost(key.length - visibleStart)
                        when {
                            key.length <= visibleStart + visibleEnd -> "*".repeat(key.length)
                            else -> key.take(visibleStart) + " … " + key.takeLast(visibleEnd)
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ApiItemDialogPreviewNoLongKeyAPI() {
    AtomCityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ApiItemDialog(
                name = "maimai",
                text = "Votre clé API pour maimai est affichée ci-dessous.",
                apiKey = "12345",
                revealed = false,
                onRevealClick = {},
                onDismiss = {},
                onDeleteClick = {},
                maskKey = { key ->
                    if (key.isNullOrBlank()) ""
                    else {
                        val visibleStart = 4.coerceAtMost(key.length)
                        val visibleEnd = 4.coerceAtMost(key.length - visibleStart)
                        when {
                            key.length <= visibleStart + visibleEnd -> "*".repeat(key.length)
                            else -> key.take(visibleStart) + " … " + key.takeLast(visibleEnd)
                        }
                    }
                }
            )
        }
    }
}
