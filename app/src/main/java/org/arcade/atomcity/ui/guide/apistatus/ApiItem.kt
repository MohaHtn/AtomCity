package org.arcade.atomcity.ui.guide.apistatus

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.core.openApiGuide
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager

@Composable
internal fun ApiItem(
    name: String,
    hasKey: Boolean
) {
    val context = LocalContext.current
    val apiKeyManager = ApiKeyManager(context)
    val apiKey = apiKeyManager.getApiKey(name.lowercase().replace(" ", ""))
    val dialogVisible = remember { mutableStateOf(false) }
    val revealed = remember { mutableStateOf(false) }

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
            .clickable(enabled = hasKey) { dialogVisible.value = true },
        leadingContent = {
            Icon(
                imageVector = if (hasKey) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                contentDescription = if (hasKey) "API configurée" else "API non configurée",
                tint = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
                text = if (hasKey) "Clé configurée • ${maskKey(apiKey)}" else "Aucun accès configuré",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            TextButton(
                onClick = {
                    openApiGuide.value = true
                    Log.d("API", "Open guide for $name (hasKey=$hasKey)")
                }
            ) {
                if (hasKey) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Modifier",
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text(
                    text = if (hasKey) "Modifier" else "Ajouter",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )

    if (dialogVisible.value) {
        AlertDialog(
            onDismissRequest = { dialogVisible.value = false },
            title = {
                Text(
                    text = "Clé API pour $name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Votre clé API  pour $name est affichée ci-dessous. " +
                                "Cliquez sur la clé pour révéler ou masquer sa valeur.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Note : Ne partagez jamais votre clé API avec d'autres personnes ou applications non fiables.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = when {
                            apiKey.isNullOrBlank() -> "(aucune clé)"
                            revealed.value -> apiKey
                            else -> maskKey(apiKey)
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .clickable(enabled = !apiKey.isNullOrBlank()) {
                                revealed.value = !revealed.value
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    dialogVisible.value = false
                    revealed.value = false
                }) {
                    Text("Fermer")
                }
            }
        )
        apiKeyManager.logAllApiKeys()
    }
}

@Preview(showBackground = true)
@Composable
fun ApiItemConfiguredPreview() {
    AtomCityTheme {
        ApiItem(
            name = "Maimai",
            hasKey = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiItemNotConfiguredPreview() {
    AtomCityTheme {
        ApiItem(
            name = "Taiko",
            hasKey = false
        )
    }
}