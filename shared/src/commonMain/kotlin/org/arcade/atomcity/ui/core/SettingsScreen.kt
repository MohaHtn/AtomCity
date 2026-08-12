package org.arcade.atomcity.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.ThemeSettingsManager
import org.koin.compose.koinInject

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
            Text(
                text = "Général",
                style = MaterialTheme.typography.titleMedium,
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
                    .clickable {
                        onApiSettingsClick()
                    }
            )
        }

        if (PlatformUtils.isIos) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Apparence",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                ThemeSettingItem(themeSettingsManager = koinInject())
            }
        }
    }
}

@Composable
fun ThemeSettingItem(themeSettingsManager: ThemeSettingsManager) {
    val currentColor by themeSettingsManager.themeColor.collectAsState(ThemeSettingsManager.DEFAULT_COLOR)
    val scope = rememberCoroutineScope()

    Column {
        ListItem(
            headlineContent = { Text("Couleur du thème") },
            supportingContent = { Text("Changer la couleur dominante de l'application") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = currentColor
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeSettingsManager.PREDEFINED_COLORS.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (color == currentColor) 3.dp else 1.dp,
                            color = if (color == currentColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            scope.launch {
                                themeSettingsManager.setThemeColor(color)
                            }
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    AtomCityTheme {
        SettingsContent(
            onBackClick = {},
            onApiSettingsClick = {}
        )
    }
}
