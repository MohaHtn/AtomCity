package org.arcade.atomcity.ui.game.maimai

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiChartSearchBar(
    navController: NavHostController,
    viewModel: MaiteaViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.searchCharts(it)
                },
                onSearch = {
                    viewModel.searchCharts(it)
                },
                expanded = active,
                onExpandedChange = { active = it },
                placeholder = { Text("Rechercher un morceau ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (active) {
                        IconButton(onClick = {
                            if (query.isNotEmpty()) {
                                query = ""
                                viewModel.searchCharts("")
                            } else {
                                active = false
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                },
                modifier = modifier.fillMaxWidth()
            )
        },
        expanded = active,
        onExpandedChange = { active = it },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 7f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                if (isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp // Extra space to clear the BottomBarPill
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults) { result ->
                        val jacketUrl = viewModel.findJacketUrlBySongName(result.songJson?.name?.jp)

                        val play = MaiteaApiData(
                            id = result.playId,
                            song = result.songJson,
                            achievementFormatted = String.format(LocalLocale.current.platformLocale, "%.2f%%", (result.achievement ?: 0.0) / 100.0),
                            rank = result.rank,
                            difficultyLevel = result.difficultyLevelJson,
                            rating = result.rating,
                            playDate = result.playDate,
                            jacketImageUrl = jacketUrl,
                            isHighScore = false
                        )

                        MaimaiScoreItem(
                            play = play,
                            onClick = {
                                navController.navigate("maimaiScoresDetails/${play.id}")
                            }
                        )
                    }

                    if (searchResults.isEmpty() && query.isNotEmpty() && !isSearching) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                                Text(
                                    text = "Aucun résultat pour \"$query\".",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
