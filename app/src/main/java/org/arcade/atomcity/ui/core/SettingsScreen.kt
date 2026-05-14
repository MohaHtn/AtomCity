package org.arcade.atomcity.ui.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.arcade.atomcity.ui.theme.AtomCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SettingsContent(
                onBackClick = onBackClick,
                onApiSettingsClick = { navController.navigate("apiSettings") }
            )
        }
    }
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
