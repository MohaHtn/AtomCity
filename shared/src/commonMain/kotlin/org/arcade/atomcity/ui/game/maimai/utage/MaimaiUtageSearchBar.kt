package org.arcade.atomcity.ui.game.maimai.utage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.arcade.atomcity.ui.core.AtomCitySearchBar

@Composable
fun UtageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AtomCitySearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholderText = "Rechercher une Utage...",
        modifier = modifier
    )
}
