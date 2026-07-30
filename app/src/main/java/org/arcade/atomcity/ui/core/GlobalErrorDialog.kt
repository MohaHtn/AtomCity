package org.arcade.atomcity.ui.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun GlobalErrorDialog() {
    val error by GlobalUIState.globalError

    if (error != null) {
        AlertDialog(
            onDismissRequest = { GlobalUIState.globalError.value = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "Erreur de connexion")
            },
            text = {
                Text(text = error!!)
            },
            confirmButton = {
                Button(
                    onClick = { GlobalUIState.globalError.value = null }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
