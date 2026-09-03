package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    LaunchedEffect(Unit) {
        taikoViewModel.fetchCommunityScores()
    }

    AtomCityUserList(
        title = "Utilisateurs Enregistrés",
        items = users,
        isLoading = isLoading,
        onBackClick = onBackClick,
        modifier = modifier
    ) { user ->
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.nickname ?: "Chargement...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "N°Utilisateur : ${user.baid}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
