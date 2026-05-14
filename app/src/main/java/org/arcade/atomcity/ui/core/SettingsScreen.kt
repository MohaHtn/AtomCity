package org.arcade.atomcity.ui.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.ui.tooling.preview.Preview
import org.arcade.atomcity.ui.theme.AtomCityTheme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    navController: NavController
) {
    SettingsContent(
        onBackClick = onBackClick,
        onApiSettingsClick = { navController.navigate("apiSettings") }
    )
}

@Composable
fun SettingsContent(
    onBackClick: () -> Unit,
    onApiSettingsClick: () -> Unit
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
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "API Settings Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                headlineContent = {
                    Text(
                        text = "Paramètres des clés API",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = "Modifier vos différents accès API",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onApiSettingsClick()
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AtomCityTheme {
        SettingsContent(
            onBackClick = {},
            onApiSettingsClick = {}
        )
    }
}
