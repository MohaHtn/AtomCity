package org.arcade.atomcity.ui.game.maimai

import MaimaiScoreItem
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
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.saveable.rememberSaveable

import coil3.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.ui.game.maimai.details.MaimaiScoreBadgeRow
import org.arcade.atomcity.ui.navigation.Screen
import org.arcade.atomcity.ui.navigation.navigateIfNotCurrent
import org.arcade.atomcity.ui.theme.StatusBarIconsEffect
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.lightPixelRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(
    maimaiViewModel: MaimaiViewModel,
    navController: androidx.navigation.NavHostController,
) {
    val isLoading by maimaiViewModel.isLoading.collectAsState()
    val isImportingScores by maimaiViewModel.isImportingScores.collectAsState()
    val importProgress by maimaiViewModel.importWorkerProgress.collectAsState()
    val importMessage by maimaiViewModel.importWorkerMessage.collectAsState()
    val data by maimaiViewModel.data.collectAsState()
    val isMaimaiImportStateReady by GlobalUIState.isMaimaiImportStateReady
    val hasNextPage by maimaiViewModel.hasNextPage.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    var showGamesMenu by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val currentPage by maimaiViewModel._currentPage.collectAsState()
    
    val playerDataState by maimaiViewModel.playerData.collectAsState()
    val playerData = playerDataState?.data?.firstOrNull()
    val frameUrl = playerData?.options?.frame?.png ?: playerData?.options?.frame?.webp
    var frameLightRatio by remember(frameUrl) { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()

    // The frame is drawn behind the status bar, so its icons follow the same light/dark decision.
    frameLightRatio?.let { StatusBarIconsEffect(darkIcons = it > 0.5f) }

    val playerName = playerData?.name?.trim() ?: ""
    val isBirthdayUser = remember(playerName) {
        playerName.isNotEmpty() && (
            playerName == "♪ｌ☆ｔｔｅ♪" ||
            playerName == "ＭｏｈａＨｔｎ♪" ||
            playerName.contains("ｌ☆ｔｔｅ") ||
            playerName.contains("ＭｏｈａＨｔｎ")
        )
    }


    val extraItems = listOf(
        Triple("maimaiBest30Scores", "30 Meilleurs scores", "Vos 30 meilleures performances"),
        Triple("maimaiPersonalBestPerSession", "Records personnels par jour", "Consulter vos records par jour"),
        Triple("maimaiMostPlayed", "Les plus joués", "Les morceaux les plus joués"),
        Triple("maimaiProgress", "Progression des joueurs", "Consulter la progression de tous les joueurs"),
        Triple("maimaiUsers", "Utilisateurs", "Consulter les utilisateurs enregistrés"),
        Triple("maimaiUtageScreen", "Utage", "Consulter les scores des utage")
    )

    LaunchedEffect(currentPage) {
        maimaiViewModel.fetchMaimaiPaginatedData(page = currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {

                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val surface = MaterialTheme.colorScheme.surface
                    // Until the frame is loaded and measured, fall back to the theme surface.
                    val isFrameLight = frameLightRatio?.let { it > 0.5f }
                        ?: (0.299f * surface.red + 0.587f * surface.green + 0.114f * surface.blue > 0.5f)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (frameUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(frameUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                                onSuccess = { state ->
                                    scope.launch {
                                        frameLightRatio = withContext(Dispatchers.Default) {
                                            state.result.image.lightPixelRatio()
                                        }
                                    }
                                }
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

                            // Once collapsed, the status bar icons sit over a thin slice of the frame:
                            // a soft shade behind them, opposite to their color, keeps them readable.
                            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                            val statusBarShade = if (isFrameLight) Color.White else Color.Black
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(statusBarHeight + 16.dp)
                                    .align(Alignment.TopStart)
                                    .graphicsLayer { alpha = collapsedFraction }
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(statusBarShade.copy(alpha = 0.45f), Color.Transparent)
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
                                                maimaiViewModel = maimaiViewModel,
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
                            viewModel = maimaiViewModel,
                            onNavigateToDetails = { id -> navController.navigateIfNotCurrent("maimaiScoresDetails/$id") },
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
                        maimaiViewModel.onPageChange(newPage)
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
                        navController.navigateIfNotCurrent(Screen.Settings.route)
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
                                onClick = { navController.navigateIfNotCurrent("maimaiScoresDetails/${play.id}") },
                                footer = {
                                    MaimaiScoreBadgeRow(
                                        scoreEntry = play,
                                        compact = true,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
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
                navController.navigateIfNotCurrent(route)
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
