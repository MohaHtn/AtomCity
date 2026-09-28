package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.layout.Arrangement
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
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.AtomCityUserList

enum class MaimaiSortMode {
    RATING,
    ALPHABETICAL,
}

@Composable
fun AtomCityUsers(
    maimaiViewModel: MaimaiViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profiles by maimaiViewModel.profiles.collectAsState()
    val ratings by maimaiViewModel.ratings.collectAsState()
    val isLoading by maimaiViewModel.isLoading.collectAsState()
    var sortMode by remember { mutableStateOf(MaimaiSortMode.RATING) }
    var sortAscending by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        maimaiViewModel.fetchProfiles()
    }

    val sortedProfiles = remember(profiles, ratings, sortMode, sortAscending) {
        val list = profiles.toList()
        when (sortMode) {
            MaimaiSortMode.RATING -> {
                if (sortAscending) {
                    list.sortedWith(
                        compareBy<Pair<String, String>> { ratings[it.first] ?: Int.MAX_VALUE }
                            .thenBy { it.second.lowercase() },
                    )
                } else {
                    list.sortedWith(
                        compareByDescending<Pair<String, String>> { ratings[it.first] ?: -1 }
                            .thenBy { it.second.lowercase() },
                    )
                }
            }
            MaimaiSortMode.ALPHABETICAL -> {
                if (sortAscending) {
                    list.sortedBy { it.second.lowercase() }
                } else {
                    list.sortedByDescending { it.second.lowercase() }
                }
            }
        }
    }

    AtomCityUserList(
        title = "Utilisateurs Enregistrés",
        items = sortedProfiles,
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
                    selected = sortMode == MaimaiSortMode.RATING,
                    onClick = {
                        if (sortMode == MaimaiSortMode.RATING) {
                            sortAscending = !sortAscending
                        } else {
                            sortMode = MaimaiSortMode.RATING
                            sortAscending = false
                        }
                    },
                    label = {
                        Text(
                            if (sortMode == MaimaiSortMode.RATING) {
                                if (sortAscending) "Rating (Croissant)" else "Rating (Décroissant)"
                            } else {
                                "Rating"
                            },
                        )
                    },
                    leadingIcon = if (sortMode == MaimaiSortMode.RATING) {
                        {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
                FilterChip(
                    selected = sortMode == MaimaiSortMode.ALPHABETICAL,
                    onClick = {
                        if (sortMode == MaimaiSortMode.ALPHABETICAL) {
                            sortAscending = !sortAscending
                        } else {
                            sortMode = MaimaiSortMode.ALPHABETICAL
                            sortAscending = true
                        }
                    },
                    label = {
                        Text(
                            if (sortMode == MaimaiSortMode.ALPHABETICAL) {
                                if (sortAscending) "Nom (A - Z)" else "Nom (Z - A)"
                            } else {
                                "Nom"
                            },
                        )
                    },
                    leadingIcon = if (sortMode == MaimaiSortMode.ALPHABETICAL) {
                        {
                            Icon(
                                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        },
    ) { (hash, username) ->
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (ratings.containsKey(hash)) {
                MaimaiRatingBadge(
                    rating = ratings[hash],
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
