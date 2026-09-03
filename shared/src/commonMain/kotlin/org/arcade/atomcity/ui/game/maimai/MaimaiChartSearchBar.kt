package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.AtomCitySearchBar
import org.arcade.atomcity.utils.format

@Composable
fun MaimaiChartSearchBar(
    onNavigateToDetails: (Int) -> Unit,
    viewModel: MaimaiViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Box(modifier = modifier.fillMaxWidth()) {
        if (query.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(52.dp))

                    if (isSearching) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 160.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { result ->
                            val play = ScorefetcherApiData(
                                id = result.playId,
                                song = result.songJson,
                                achievementFormatted = "${((result.achievement ?: 0.0) / 100.0).format(2)}%",
                                rank = result.rank,
                                difficultyLevel = result.difficultyLevelJson,
                                rating = result.rating,
                                playDate = result.playDate,
                                jacketImageUrl = result.jacketImageUrl,
                                isHighScore = false
                            )

                            MaimaiScoreItem(
                                play = play,
                                onClick = {
                                    onNavigateToDetails(play.id ?: 0)
                                }
                            )
                        }

                        if (searchResults.isEmpty() && !isSearching) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                                    Text(
                                        text = "Aucun résultat pour \"$query\".",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AtomCitySearchBar(
            query = query,
            onQueryChange = {
                query = it
                viewModel.searchCharts(it)
            },
            placeholderText = "Rechercher un morceau ...",
            onClearClick = {
                query = ""
                viewModel.searchCharts("")
            }
        )
    }
}
