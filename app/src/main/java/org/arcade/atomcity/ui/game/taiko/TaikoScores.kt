package org.arcade.atomcity.ui.game.taiko

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.game.maimai.MaimaiPlayerDetails


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScores(
    taikoViewModel: TaikoViewModel,
    navController: NavHostController
) {
    val isLoading by taikoViewModel.isLoading.collectAsState()


    var showMiniMenu by remember { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val scoresData by taikoViewModel.scoresData.collectAsState()


    LaunchedEffect(Unit) {
        taikoViewModel.fetchMusicDetails()
        taikoViewModel.fetchPlayHistoryPlayData(1)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row {
                        Text(
                            text = "Taiko No Tatsujin |",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (collapsedFraction > 0.5f)
                                Color.Black
                            else
                                Color.Black,
                            fontWeight = if (collapsedFraction > 0.5f)
                                androidx.compose.ui.text.font.FontWeight.Bold
                            else
                                androidx.compose.ui.text.font.FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomBarPill(
                currentPage = taikoViewModel._currentPage.collectAsState().value,
                onPageChange = { newPage ->
                    taikoViewModel.onPageChange(newPage)
                    /*
                                        maiteaViewModel.fetchMaimaiPaginatedData(newPage)
                    */
                },
                onHomeClick = {
                    showMiniMenu = !showMiniMenu
                },
                onSettingsClick = {
                    Log.d("MaimaiScores", "Settings clicked")
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(paddingValues)
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    scoresData?.let { dataList ->
                        items(dataList.taikoServerSongHistoryData.size) { index ->
                            val score = dataList.taikoServerSongHistoryData[index]
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = score.toString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

