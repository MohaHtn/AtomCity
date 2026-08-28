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
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import atomcity.shared.generated.resources.*

@Composable
fun BottomBarPill(
    currentPage: Int,
    isLoading: Boolean = false,
    hasNextPage: Boolean = true,
    showPagination: Boolean = true,
    onPageChange: (Int) -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            enabled = showPagination && !isLoading && currentPage != 1,
            onClick = {
                if (showPagination && currentPage > 1) {
                    onPageChange(currentPage - 1)
                }
            },
            icon = {
                if (showPagination) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Page précédente"
                    )
                }
            },
            label = { if (showPagination) Text("") }
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
            label = { 
                Text(
                    text = "Actions",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.stadia_controller_24px),
                    contentDescription = "Jeux"
                )
            },
            label = { 
                Text(
                    text = "Jeux",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = {
                Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Paramètres"
            )},
            label = { 
                Text(
                    text = "Paramètres",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            }
        )
        
        NavigationBarItem(
            selected = false,
            enabled = showPagination && !isLoading && hasNextPage,
            onClick = { 
                if (showPagination) {
                    onPageChange(currentPage + 1) 
                }
            },
            icon = {
                if (showPagination) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Page suivante"
                    )
                }
            },
            label = { if (showPagination) Text("") }
        )
    }
}
