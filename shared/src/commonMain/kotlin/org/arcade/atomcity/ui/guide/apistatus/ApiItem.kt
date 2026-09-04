package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.TokenUtils
import org.koin.compose.koinInject

@Composable
internal fun ApiItem(
    name: String,
    key: String = name.lowercase().replace(" ", ""),
) {
    val inspectionMode = LocalInspectionMode.current
    
    // In preview mode, we don't use Koin
    val apiKeyManager = if (inspectionMode) null else koinInject<ApiKeyManager>()
    val scorefetcherRepository = if (inspectionMode) null else koinInject<IScorefetcherRepository>()
    val taikoRepository = if (inspectionMode) null else koinInject<org.arcade.atomcity.domain.repository.ITaikoServerRepository>()
    
    val scope = rememberCoroutineScope()
    
    val apiKey by if (inspectionMode || apiKeyManager == null) {
        remember { mutableStateOf(if (inspectionMode) "Preview Key" else null) }
    } else {
        apiKeyManager.getApiKeyFlow(key).collectAsState(initial = null)
    }

    val taikoAccessCode by if (key == "taiko" && !inspectionMode && apiKeyManager != null) {
        apiKeyManager.getTaikoAccessCodeFlow().collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val taikoPassword by if (key == "taiko" && !inspectionMode && apiKeyManager != null) {
        apiKeyManager.getTaikoPasswordFlow().collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val hasKeyActual = if (key == "taiko") taikoAccessCode != null else apiKey != null
    val dialogVisible = remember { mutableStateOf(false) }
    val revealed = remember { mutableStateOf(false) }
    val revealedPassword = remember { mutableStateOf(false) }
    val successDialogVisible = remember { mutableStateOf(false) }
    
    // Actual supported games (for now!)
    val isGameSupported = name == "maimai"

    fun maskKey(key: String?): String {
        if (key.isNullOrBlank()) return ""
        val visibleStart = 4.coerceAtMost(key.length)
        val visibleEnd = 4.coerceAtMost(key.length - visibleStart)
        return when {
            key.length <= visibleStart + visibleEnd -> "*".repeat(key.length)
            else -> key.take(visibleStart) + " … " + key.takeLast(visibleEnd)
        }
    }

    OutlinedCard(
        onClick = {
            if (hasKeyActual) {
                GlobalUIState.selectedGameForGuide.value = name
                dialogVisible.value = true
            }
        },
        enabled = hasKeyActual,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasKeyActual) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
            } else if (isGameSupported) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasKeyActual) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Container
            Surface(
                shape = CircleShape,
                color = if (hasKeyActual) {
                    MaterialTheme.colorScheme.primaryContainer
                } else if (isGameSupported) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasKeyActual) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if (hasKeyActual) {
                            MaterialTheme.colorScheme.primary
                        } else if (isGameSupported) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        hasKeyActual -> "Clé configurée • ${maskKey(apiKey)}"
                        isGameSupported -> "Non configuré"
                        else -> "Disponible peut-être un jour ..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasKeyActual) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGameSupported) {
                    FilledTonalButton(
                        onClick = {
                            GlobalUIState.selectedGameForGuide.value = name
                            GlobalUIState.openSaveKeyDialog.value = true
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (hasKeyActual) Icons.Rounded.Edit else Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasKeyActual) "Modifier" else "Ajouter",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    IconButton(
                        onClick = {
                            GlobalUIState.selectedGameForGuide.value = name
                            GlobalUIState.openApiGuide.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (dialogVisible.value) {
        ApiItemDialog(
            name = name,
            apiKey = if (key == "taiko") taikoAccessCode else apiKey,
            password = if (key == "taiko") taikoPassword else null,
            revealed = revealed.value,
            revealedPassword = revealedPassword.value,
            onRevealClick = { revealed.value = !revealed.value },
            onRevealPasswordClick = { revealedPassword.value = !revealedPassword.value },
            onDismiss = {
                dialogVisible.value = false
                revealed.value = false
                revealedPassword.value = false
            },
            onDeleteClick = {
                scope.launch {
                    var isDeletedOnServer = false
                    if (name == "maimai" && apiKey != null) {
                        try {
                            scorefetcherRepository?.removeApiKey(apiKey!!)?.collect { response ->
                                PlatformUtils.log("ApiItem", "Clé API supprimée du serveur : ${response.message}")
                                if (response.message == "API Key and associated data deleted.") {
                                    isDeletedOnServer = true
                                }
                            }
                        } catch (e: Exception) {
                            PlatformUtils.log("ApiItem", "Erreur lors de la suppression de la clé sur le serveur: ${e.message}", true)
                        }
                    }
                    if (key == "taiko") {
                        apiKeyManager?.removeTaikoCredentials()
                    } else {
                        apiKeyManager?.removeApiKey(key)
                    }
                    dialogVisible.value = false
                    revealed.value = false
                    revealedPassword.value = false
                    
                    if (isDeletedOnServer) {
                        successDialogVisible.value = true
                    }
                }
            },
            maskKey = ::maskKey,
            text = 
                when (name) {
                    "maimai" -> "Votre clé API pour maimai est affichée ci-dessous.\n"
                    "Taiko no Tatsujin" -> "Vos identifiants pour Taiko no Tatsujin sont configurés.\n"
                    else -> ""
                }
            
        )
    }

    if (successDialogVisible.value) {
        AlertDialog(
            onDismissRequest = { /* Prevent the user from exiting the dialog by touching anywhere else */ },
            title = {
                Text(text = "Clé supprimée")
            },
            text = {
                Text(text = "La clé API a été supprimée. L'application va se fermer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        successDialogVisible.value = false
                        PlatformUtils.exitApp()
                    }
                ) {
                    Text("Valider")
                }
            }
        )
    }

    if (GlobalUIState.openSaveKeyDialog.value && GlobalUIState.selectedGameForGuide.value == name) {
        if (key == "taiko") {
            EditTaikoCredentialsDialog(
                onDismiss = { GlobalUIState.openSaveKeyDialog.value = false },
                onSaveCredentials = { accessCode, password ->
                    scope.launch {
                        apiKeyManager?.saveTaikoCredentials(accessCode, password)
                        
                        // Link with Scorefetcher
                        try {
                            val authResponse = taikoRepository?.login(org.arcade.atomcity.data.remote.model.taikoserver.TaikoLoginRequest(accessCode, password))
                            val token = authResponse?.authToken ?: authResponse?.token
                            if (token != null) {
                                apiKeyManager?.saveTaikoAuthToken(token)
                                // Extract BAID and link to scorefetcher
                                val baid = TokenUtils.extractBaid(token)?.toIntOrNull() ?: throw Exception("Impossible d'extraire le BAID du token")
                                scorefetcherRepository?.addTaikoUser(baid)
                                PlatformUtils.log("ApiItem", "Taiko user $baid linked to scorefetcher")
                            }
                        } catch (e: Exception) {
                            PlatformUtils.log("ApiItem", "Error linking Taiko: ${e.message}", true)
                        }
                        
                        GlobalUIState.openSaveKeyDialog.value = false
                    }
                },
                existingAccessCode = taikoAccessCode,
                existingPassword = taikoPassword
            )
        } else {
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

                        // Trigger import if we just added/modified the maimai key
                        if (key == "maimai") {
                            scorefetcherRepository?.startScorefetcherImport()
                        }
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
}
