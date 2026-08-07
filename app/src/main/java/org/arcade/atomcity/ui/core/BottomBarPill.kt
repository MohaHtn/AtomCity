package org.arcade.atomcity.ui.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.arcade.atomcity.R

@Composable
fun BottomBarPill(
    currentPage: Int,
    isLoading: Boolean = false,
    hasNextPage: Boolean = true,
    onPageChange: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            enabled = !isLoading && currentPage != 1,
            onClick = {
                if (currentPage > 1) {
                    onPageChange(currentPage - 1)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Page précédente"
                )
            },
            label = { Text("") }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onMenuClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Actions"
                )
            },
            label = { Text("Actions") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.stadia_controller_24px),
                    contentDescription = "Jeux"
                )
            },
            label = { Text("Jeux") }
        )
        
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = {
                Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Paramètres"
            )},
            label = { Text("Paramètres") }
        )
        
        NavigationBarItem(
            selected = false,
            enabled = !isLoading && hasNextPage,
            onClick = { onPageChange(currentPage + 1) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Page suivante"
                )
            },
            label = { Text("") }
        )
    }
}
