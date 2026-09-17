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
import org.arcade.atomcity.ui.navigation.Screen
import org.arcade.atomcity.ui.navigation.navigateIfNotCurrent
import org.arcade.atomcity.utils.PlatformUtils

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

    val playerName = playerData?.name?.trim() ?: ""
    val isBirthdayUser = remember(playerName) {
        playerName.isNotEmpty() && (
            playerName == "♪ｌ☆ｔｔｅ♪" ||
            playerName == "ＭｏｈａＨｔｎ♪" ||
            playerName.contains("ｌ☆ｔｔｅ") ||
            playerName.contains("ＭｏｈａＨｔｎ")
        )
    }

    var hasAutoShownBirthday by rememberSaveable { mutableStateOf(false) }
    var showBirthdayDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isBirthdayUser) {
        if (isBirthdayUser && !hasAutoShownBirthday) {
            showBirthdayDialog = true
            hasAutoShownBirthday = true
        }
    }

    val extraItems = listOf(
        Triple("maimaiBest30Scores", "30 Meilleurs scores", "Vos 30 meilleures performances"),
        Triple("maimaiMostPlayed", "Les plus joués", "Les morceaux les plus joués"),
        Triple("maimaiProgress", "Progression Rangs", "Consulter la progression de tous les joueurs"),
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
                                                maimaiViewModel = maimaiViewModel,
                                                collapsedFraction = collapsedFraction,
                                                textColor = foregroundColor
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                if (isBirthdayUser) {
                                    MaimaiBirthdayBadge(
                                        onClick = { showBirthdayDialog = true },
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
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
                                onClick = { navController.navigateIfNotCurrent("maimaiScoresDetails/${play.id}") }
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

        if (showBirthdayDialog) {
            MaimaiBirthdayDialog(
                playerName = playerName,
                onDismiss = { showBirthdayDialog = false }
            )
        }
    }
}

@Composable
fun MaimaiBirthdayBadge(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateDegrees by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFF4081),
        shadowElevation = 6.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🎂",
                fontSize = 16.sp,
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotateDegrees
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Anniversaire !",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun MaimaiBirthdayDialog(
    playerName: String,
    onDismiss: () -> Unit
) {
    DisposableEffect(Unit) {
        PlatformUtils.playBirthdayBgm()
        onDispose {
            PlatformUtils.stopBirthdayBgm()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()

    val bobOffset1 by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val bobOffset2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val milkBobbing by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val milkRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val milkScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "🎈",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer { translationY = bobOffset1 }
                )
                Text(
                    text = "✨",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer { translationY = bobOffset2 }
                )
                Text(
                    text = "🥳",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .graphicsLayer { translationY = bobOffset2 }
                )
                Text(
                    text = "🎁",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer { translationY = bobOffset1 }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("file:///android_asset/maimai/database/birthday/milk_maimai_prism_plus.webp")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Milk Maimai Prism Plus",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(130.dp)
                            .graphicsLayer {
                                translationY = milkBobbing
                                rotationZ = milkRotation
                                scaleX = milkScale
                                scaleY = milkScale
                            }
                    )

                    Text(
                        text = "Joyeux Anniversaire !",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$playerName ! 🍰✨",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Alors faut savoir que y'a que toi qui aura cette pop-up !!! " +
                                        "J'espère que tu continueras à t'amuser à fond sur maimai !! Dommage que ça doit tombé un jour de cours" +
                                        " mais en tous cas je tenais à te le souhaiter à travers cette app !! c:",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "bisous !! ❤️",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
