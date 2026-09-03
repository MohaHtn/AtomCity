package org.arcade.atomcity.ui.game.maimai

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
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.AtomCityUserList

@Composable
fun AtomCityUsers(
    maimaiViewModel: MaimaiViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profiles by maimaiViewModel.profiles.collectAsState()
    val ratings by maimaiViewModel.ratings.collectAsState()
    val isLoading by maimaiViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        maimaiViewModel.fetchProfiles()
    }

    AtomCityUserList(
        title = "Utilisateurs Enregistrés",
        items = profiles.toList(),
        isLoading = isLoading,
        onBackClick = onBackClick,
        modifier = modifier
    ) { (hash, username) ->
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ratings.containsKey(hash)) {
                MaimaiRatingBadge(
                    rating = ratings[hash],
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
