package org.arcade.atomcity.ui.guide.apistatus

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import org.arcade.atomcity.ui.guide.apistatus.ApiCheckList
import org.arcade.atomcity.ui.core.openApiGuide
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

    Row(
        modifier = Modifier.fillMaxWidth().clickable(
            enabled = hasKey,
            onClick = {
                dialogVisible.value = true
            }
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasKey) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.Close
                },
                contentDescription = if (hasKey) "API configured" else "API not configured",
                tint = if (hasKey) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            if (!hasKey) {
                Button(
                    modifier = Modifier.padding(start = 56.dp),
                    onClick = { openApiGuide.value = true; }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Annuler"
                        )
                        Text(
                            text = "Ajouter",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else {
                Button(
                    modifier = Modifier.padding(start = 56.dp),
                    onClick = { openApiGuide.value = true; Log.d("API", "Clicked ${openApiGuide.value}") }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Modifier"
                        )
                        Text(
                            text = "Modifier",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }

    if (dialogVisible.value) {
        AlertDialog(
            onDismissRequest = { dialogVisible.value = false },
            title = { Text("Clé API pour $name") },
            text = { Text("Votre clé API: $apiKey") },
            confirmButton = {
                Button(onClick = { dialogVisible.value = false }) {
                    Text("Fermer")
                }
            }
        )
        apiKeyManager.logAllApiKeys()
    }
}