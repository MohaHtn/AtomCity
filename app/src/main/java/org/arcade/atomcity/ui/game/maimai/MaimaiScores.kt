package org.arcade.atomcity.ui.game.maimai

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.AchievementChip
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.utils.formatPlayDate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.scale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(
    maiteaViewModel: MaiteaViewModel,
    navController: NavHostController
) {

    val isLoading by maiteaViewModel.isLoading.collectAsState()
    val data by maiteaViewModel.data.collectAsState()

    var topAppBarWidth by remember { mutableStateOf(0.dp) }
    var topAppBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    var showMiniMenu by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        maiteaViewModel.fetchMaimaiPaginatedData(page = 1)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(maiteaViewModel.playerData.collectAsState().value?.data?.get(0)?.options?.frame?.png)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Maimai Background Collapsed",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .alpha(collapsedFraction)
                )

                // TopAppBar avec un fond transparent pour laisser voir les images
                LargeTopAppBar(
                    title = {
                        Row {
                            Text(
                                text = "maimai |",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (collapsedFraction > 0.5f)
                                    Color.White
                                else
                                    Color.Black,
                                fontWeight = if (collapsedFraction > 0.5f)
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            MaimaiPlayerDetails(
                                maiteaViewModel,
                                collapsedFraction,
                                onBackClick = { Log.d("MaimaiScores", "Back clicked") },
                                topAppBarWidth = topAppBarWidth,
                                topAppBarHeight = topAppBarHeight,
                                scrollBehavior = scrollBehavior
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent, // Fond transparent pour voir les images
                        scrolledContainerColor = Color.Transparent.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            topAppBarWidth = with(density) { size.width.toDp() }
                            topAppBarHeight = with(density) { size.height.toDp() }
                        }
                )
            }
        },
        bottomBar = {
            BottomBarPill(
                currentPage = maiteaViewModel._currentPage.collectAsState().value,
                onPageChange = { newPage ->
                    maiteaViewModel.onPageChange(newPage)
                    maiteaViewModel.fetchMaimaiPaginatedData(newPage)
                },
                onHomeClick = {
                    showMiniMenu = !showMiniMenu
                },
                onSettingsClick = {
                    navController.navigate("settings")
                    showMiniMenu = false
                },
            )
        },
) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    items(count = maiteaViewModel.playsDataSize) { score ->
                        Card(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                isExpanded = !isExpanded

                                /*
                                                                navController.navigate("maimaiScoresDetails/${data?.data?.get(score)?.id}")
                                */
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.name?.en}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.name?.jp}",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.artist?.en} | ${
                                            data?.data?.get(
                                                score
                                            )?.song?.artist?.jp
                                        }",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = formatPlayDate(data?.data?.get(score)?.playDate),
                                                modifier = Modifier.align(Alignment.CenterStart),
                                                style = MaterialTheme.typography.bodySmall
                                            )

                                            AchievementChip(
                                                text = data?.data?.get(score)?.achievementFormatted?: "",
                                                modifier = Modifier.align(Alignment.CenterEnd)
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
