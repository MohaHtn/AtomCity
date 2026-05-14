package org.arcade.atomcity.ui.core.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.guide.apistatus.ApiCheckList
import org.arcade.atomcity.utils.ApiKeyManager

@Composable
fun ApiSettings(
    onBackClick: () -> Unit,
    apiKeyManager: ApiKeyManager
) {
    LazyColumn {
        item {
            Text(
                text = "Paramètres des clés API",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        item {
            Text(
                text = "Voici l'état des clés API que vous avez entré dans l'application. " +
                        "Vous pouvez les modifier, les supprimer ou ajouter de nouvelles si nécessaire.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        item {
            ApiCheckList(
                apiChecklistState = apiKeyManager.getApiChecklistState(),
            )
        }
    }


}
