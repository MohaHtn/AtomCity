package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.game.taiko.ErrorCard
import org.arcade.atomcity.ui.game.taiko.InfoCard
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ApiItemDialog(
    name: String,
    text: String,
    apiKey: String?,
    password: String? = null,
    revealed: Boolean,
    revealedPassword: Boolean = false,
    onRevealClick: () -> Unit,
    onRevealPasswordClick: () -> Unit = {},
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    maskKey: (String?) -> String
) {
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = when (GlobalUIState.selectedGameForGuide.value) {
                        "Taiko no Tatsujin" -> "Supprimer les identifiants ?"
                        else -> "Supprimer la clé ?"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = when (GlobalUIState.selectedGameForGuide.value) {
                            "Taiko no Tatsujin" -> "Êtes-vous sûr de vouloir supprimer les identifiants pour $name ?"
                            else -> "Êtes-vous sûr de vouloir supprimer la clé API pour $name ?"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ErrorCard(
                        message = when (GlobalUIState.selectedGameForGuide.value) {
                            "Taiko no Tatsujin" -> "Cette action est irréversible. Vos identifiants seront supprimés de l'application et de la liste d'utilisateurs utilisant l'app sur Scorefetcher."
                            else -> "Cette action est irréversible. La clé API sera également supprimée sur le serveur distant (maitea.app)."
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation.value = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = when (GlobalUIState.selectedGameForGuide.value) {
                            "Taiko no Tatsujin" -> "Supprimer"
                            else -> "Supprimer"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation.value = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = when (GlobalUIState.selectedGameForGuide.value) {
                    "Taiko no Tatsujin" -> "Identifiants de connexion pour $name"
                    "maimai" -> "Clé API pour $name"
                    else -> name
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (name == "Taiko no Tatsujin") {
                    Text(
                        text = "Vos identifiants (Code d'accès et mot de passe) pour Taiko no Tatsujin sont configurés.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoCard(
                        message = "Ce mot de passe est chiffré dans l'application et n'est utilisé qu'avec la communication sur le serveur distant gérant Taiko (tatsuj.in).",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Code d'accès (Identifiant) :",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !apiKey.isNullOrBlank()) { onRevealClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when {
                                        apiKey.isNullOrBlank() -> "(aucun code d'accès)"
                                        revealed -> apiKey
                                        else -> maskKey(apiKey)
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    softWrap = true
                                )
                            }
                            if (!apiKey.isNullOrBlank()) {
                                Text(
                                    text = if (revealed) "Masquer" else "Afficher",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mot de passe :",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !password.isNullOrBlank()) { onRevealPasswordClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when {
                                        password.isNullOrBlank() -> "(aucun mot de passe)"
                                        revealedPassword -> password
                                        else -> maskKey(password)
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    softWrap = true
                                )
                            }
                            if (!password.isNullOrBlank()) {
                                Text(
                                    text = if (revealedPassword) "Masquer" else "Afficher",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = text + "Appuyez sur la clé pour révéler ou masquer sa valeur.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InfoCard(
                        message = "Cette clé API est chiffrée dans l'application et n'est utilisée qu'avec la communication sur le serveur distant gérant maimai (maitea.app), ainsi que le serveur de relais Scorefetcher (nécessaire pour les stats en temps réel).",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !apiKey.isNullOrBlank()) { onRevealClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when {
                                        apiKey.isNullOrBlank() -> "(aucune clé)"
                                        revealed -> apiKey
                                        else -> maskKey(apiKey)
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    softWrap = true
                                )
                            }
                            if (!apiKey.isNullOrBlank()) {
                                Text(
                                    text = if (revealed) "Masquer" else "Afficher",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text("Fermer")
            }
        },
        dismissButton = {
            if (!apiKey.isNullOrBlank() || !password.isNullOrBlank()) {
                TextButton(
                    onClick = { showDeleteConfirmation.value = true }
                ) {
                    Text(
                        text = if (GlobalUIState.selectedGameForGuide.value == "Taiko no Tatsujin") {
                            "Supprimer les identifiants"
                        } else {
                            "Supprimer la clé"
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Preview
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

@Preview
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

@Preview
@Composable
fun ApiItemDialogPreviewTaikoCredentials() {
    AtomCityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ApiItemDialog(
                name = "Taiko no Tatsujin",
                text = "Vos identifiants pour Taiko no Tatsujin sont configurés.",
                apiKey = "012E58B3B4D780AD",
                password = "mysecretpassword123",
                revealed = false,
                revealedPassword = false,
                onRevealClick = {},
                onRevealPasswordClick = {},
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
