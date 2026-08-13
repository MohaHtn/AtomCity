package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
// ...existing code...
import androidx.compose.ui.unit.dp
// ...existing code...

import coil3.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(
    maiteaViewModel: MaiteaViewModel,
    navController: androidx.navigation.NavHostController,
) {
    val isLoading by maiteaViewModel.isLoading.collectAsState()
    val isImportingScores by maiteaViewModel.isImportingScores.collectAsState()
    val importProgress by maiteaViewModel.importWorkerProgress.collectAsState()
    val importMessage by maiteaViewModel.importWorkerMessage.collectAsState()
    val data by maiteaViewModel.data.collectAsState()
    val isMaimaiImportStateReady by GlobalUIState.isMaimaiImportStateReady
    val hasNextPage by maiteaViewModel.hasNextPage.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    var showGamesMenu by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val currentPage by maiteaViewModel._currentPage.collectAsState()
    
    val playerDataState by maiteaViewModel.playerData.collectAsState()
    val playerData = playerDataState?.data?.firstOrNull()
    val frameUrl = playerData?.options?.frame?.png ?: playerData?.options?.frame?.webp

    val extraItems = listOf(
        Triple("maimaiBest30Scores", "Best 30", "Vos 30 meilleures performances"),
        Triple("maimaiMostPlayed", "Plus joués", "Vos morceaux les plus joués"),
        Triple("maimaiUsers", "Utilisateurs", "Voir les autres joueurs")
    )

    LaunchedEffect(currentPage) {
        maiteaViewModel.fetchMaimaiPaginatedData(page = currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {

                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val surface = MaterialTheme.colorScheme.surface
                    val luminance = 0.299f * surface.red + 0.587f * surface.green + 0.114f * surface.blue
                    val isFrameLight = luminance > 0.5f

                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (frameUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(frameUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )

                            val edgeShadowAlpha = 0.45f
                            val edgeColor = if (isFrameLight) Color.Black.copy(alpha = edgeShadowAlpha) else Color.White.copy(alpha = edgeShadowAlpha)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .align(Alignment.BottomStart)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, edgeColor)
                                        )
                                    )
                            )
                        }

                        val overlayAlpha = 0.35f * (1f - collapsedFraction) + 0.15f * collapsedFraction

                        val overlayColor = if (isFrameLight) {
                            Color.White.copy(alpha = overlayAlpha)
                        } else {
                            Color.Black.copy(alpha = overlayAlpha)
                        }

                        // Foreground (text/icon) color should contrast with the
                        // overlay/frame.
                        val foregroundColor = if (isFrameLight) Color.Black else Color.White

                        LargeTopAppBar(
                            title = {
                                // small rounded background wrapping title content only
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(overlayColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "maimai |",
                                                fontWeight = FontWeight.Bold,
                                                color = foregroundColor,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )

                                            MaimaiPlayerDetails(
                                                maiteaViewModel = maiteaViewModel,
                                                collapsedFraction = collapsedFraction,
                                                textColor = foregroundColor
                                            )
                                        }
                                    }
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent,
                                titleContentColor = foregroundColor,
                                navigationIconContentColor = foregroundColor,
                                actionIconContentColor = foregroundColor
                            )
                        )
                    }
                    if (isMaimaiImportStateReady) {
                        MaimaiChartSearchBar(
                            viewModel = maiteaViewModel,
                            onNavigateToDetails = { id -> navController.navigate("maimaiScoresDetails/$id") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 8.dp)
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
            },
            bottomBar = {
                BottomBarPill(
                    currentPage = currentPage,
                    isLoading = isLoading,
                    hasNextPage = hasNextPage,
                    onPageChange = { newPage ->
                        maiteaViewModel.onPageChange(newPage)
                    },
                    onHomeClick = { 
                        showGamesMenu = !showGamesMenu
                        showActionsMenu = false
                    },
                    onMenuClick = { 
                        showActionsMenu = !showActionsMenu
                        showGamesMenu = false
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                        showGamesMenu = false
                        showActionsMenu = false
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (!isMaimaiImportStateReady) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(data?.data ?: emptyList()) { play ->
                            MaimaiScoreItem(
                                play = play,
                                onClick = { navController.navigate("maimaiScoresDetails/${play.id}") }
                            )
                        }
                    }
                }
            }
        }

        OpenMiniMenu(
            visible = showGamesMenu || showActionsMenu,
            onDismiss = {
                showGamesMenu = false
                showActionsMenu = false
            },
            onItemClick = { route ->
                navController.navigate(route)
                showGamesMenu = false
                showActionsMenu = false
            },
            showGames = showGamesMenu,
            extraItems = if (showActionsMenu) extraItems else emptyList(),
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
        )

        if (isImportingScores) {
            MaimaiImportOverlay(
                progress = importProgress,
                message = importMessage ?: ""
            )
        }
    }
}
