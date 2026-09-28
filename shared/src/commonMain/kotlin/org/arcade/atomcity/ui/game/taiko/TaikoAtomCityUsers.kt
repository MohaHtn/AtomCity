package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.AtomCityUserList

@Composable
fun TaikoAtomCityUsers(
    taikoViewModel: TaikoViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val users by taikoViewModel.taikoUsers.collectAsState()
    val isLoading by taikoViewModel.isLoading.collectAsState()
    var sortAscending by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        taikoViewModel.fetchCommunityScores()
    }

    val sortedUsers = remember(users, sortAscending) {
        if (sortAscending) {
            users.sortedBy { (it.nickname ?: "").lowercase() }
        } else {
            users.sortedByDescending { (it.nickname ?: "").lowercase() }
        }
    }

    AtomCityUserList(
        title = "Utilisateurs Enregistrés",
        items = sortedUsers,
        isLoading = isLoading,
        onBackClick = onBackClick,
        modifier = modifier,
        headerContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trier par :",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilterChip(
                    selected = true,
                    onClick = { sortAscending = !sortAscending },
                    label = {
                        Text(if (sortAscending) "Nom (A - Z)" else "Nom (Z - A)")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                        )
                    },
                )
            }
        },
    ) { user ->
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = user.nickname ?: "Chargement...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "N°Utilisateur : ${user.baid}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
