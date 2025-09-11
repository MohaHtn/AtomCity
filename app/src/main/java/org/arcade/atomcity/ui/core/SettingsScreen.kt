package org.arcade.atomcity.ui.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        text = "Paramètres des clés API",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.padding(8.dp)
                    .clickable {
                        navController.navigate("apiSettings")
                    }
            )
        }
    }
}