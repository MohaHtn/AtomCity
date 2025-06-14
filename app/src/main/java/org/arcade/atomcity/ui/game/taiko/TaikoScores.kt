package org.arcade.atomcity.ui.game.taiko

import android.util.Log
import androidx.compose.foundation.Image
import org.arcade.atomcity.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.utils.formatPlayDate


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
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val scoresData by taikoViewModel.scoresData.collectAsState()


    LaunchedEffect(Unit) {
        taikoViewModel.getScores()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row {
                        Text(
                            text = "Taiko No Tatsujin | ",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (collapsedFraction > 0.5f)
                                Color.Black
                            else
                                Color.Black,
                            fontWeight = if (collapsedFraction > 0.5f)
                                FontWeight.Bold
                            else
                                FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Column {
                            Text(
                                text = taikoViewModel.userSettingsData.collectAsState().value?.myDonName ?: "Chargement...",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (collapsedFraction > 0.5f)
                                    Color.Black
                                else
                                    Color.Black,
                                fontWeight = if (collapsedFraction > 0.5f)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                            )

                            Text(
                                text = taikoViewModel.userSettingsData.collectAsState().value?.title.toString() ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (collapsedFraction > 0.5f)
                                    Color.Black
                                else
                                    Color.Black,
                                fontWeight = if (collapsedFraction > 0.5f)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                            )
                        }

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
                },
                onHomeClick = {
                    showMiniMenu = !showMiniMenu
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        },
    ) { paddingValues ->
        Box() {
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
                                    .fillMaxSize(),
                                colors = setDifficultyColorBackground(score.difficulty)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Image(
                                        painter = painterResource(id = getDifficultyDrawableId(score.difficulty)),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .matchParentSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.2f
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxSize()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = score.musicName.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                modifier = Modifier.padding(end = 5.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.White
                                            )
                                            Text(
                                                text = displayDifficultyName(score.difficulty),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = score.musicArtist.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                            Text(
                                                text = score.score.toString(),
                                                style = MaterialTheme.typography.displaySmall,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp)
        ) {
            OpenMiniMenu(
                showMiniMenu = showMiniMenu,
                onDismiss = {
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        showMiniMenu = !showMiniMenu
                        lastClickTime = System.currentTimeMillis()
                    }
                },
                onItemClick = { gameId: String ->
                    Log.d("MaimaiScores", "onItemClick $gameId")
                    navController.navigate("game/$gameId")
                    showMiniMenu = false
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        }
    }

fun displayDifficultyName(difficulty: Int?) : String{
    return when(difficulty){
        1 -> "Kantan (Facile)"
        2 -> "Futsuu (Normal)"
        3 -> "Muzukashii (Difficile)"
        4 -> "Oni (Démoniaque)"
        5 -> "Ura Oni (Ultra Démoniaque)"
        else -> "Inconnu"
    }
}

fun getDifficultyDrawableId(difficulty: Int?): Int {
    return when (difficulty) {
        1 -> R.drawable.taiko_difficulty_easy
        2 -> R.drawable.taiko_difficulty_normal
        3 -> R.drawable.taiko_difficulty_hard
        4 -> R.drawable.taiko_difficulty_evil
        5 -> R.drawable.taiko_difficulty_uraoni
        else -> R.drawable.taiko_difficulty_easy
    }
}

@Composable
fun setDifficultyColorBackground(difficulty: Int?): CardColors {
    return when (difficulty) {
        1 -> CardDefaults.cardColors(containerColor = Color(0xFFCF2C00))
        2 -> CardDefaults.cardColors(containerColor = Color(0xFF657E25))
        3 -> CardDefaults.cardColors(containerColor = Color(0xFF223004))
        4 -> CardDefaults.cardColors(containerColor = Color(0xFFCE2D76))
        else -> CardDefaults.cardColors(containerColor = Color.Gray)
    }
}


