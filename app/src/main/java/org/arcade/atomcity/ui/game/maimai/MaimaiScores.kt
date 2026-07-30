package org.arcade.atomcity.ui.game.maimai

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.core.OpenMiniMenu

private const val MAIMAI_IMPORT_FROM_WELCOME_KEY = "maimai_import_from_welcome"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(
    maiteaViewModel: MaiteaViewModel,
    navController: NavHostController
) {
    val isLoading by maiteaViewModel.isLoading.collectAsState()
    val data by maiteaViewModel.data.collectAsState()
    val isImportingScores by GlobalUIState.isImportingMaimaiScores
    val isMaimaiImportStateReady by GlobalUIState.isMaimaiImportStateReady
    val importProgress by maiteaViewModel.importWorkerProgress.collectAsState()
    val importMessage by maiteaViewModel.importWorkerMessage.collectAsState()
    val playerDataState by maiteaViewModel.playerData.collectAsState()
    val playerData = playerDataState?.data?.firstOrNull()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    var frameColor by remember { mutableStateOf<Color?>(null) }
    var showMiniMenu by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val currentPage by maiteaViewModel._currentPage.collectAsState()
    val importRequestedFromWelcome = remember(navController) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.remove<Boolean>(MAIMAI_IMPORT_FROM_WELCOME_KEY) == true
    }
    var hasHandledInitialLoad by remember { mutableStateOf(false) }

    LaunchedEffect(currentPage) {
        maiteaViewModel.fetchMaimaiPaginatedData(
            page = currentPage,
            startImport = !hasHandledInitialLoad && importRequestedFromWelcome && currentPage == 1
        )
        if (!hasHandledInitialLoad) {
            hasHandledInitialLoad = true
        }
    }

    val isBackgroundDark = remember(frameColor) {
        frameColor?.let { color ->
            val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
            luminance < 0.5
        } ?: true
    }

    val appBarTextColor by animateColorAsState(
        targetValue = frameColor?.let { base ->
            if (isBackgroundDark) {
                androidx.compose.ui.graphics.lerp(base, Color.White, 0.9f)
            } else {
                androidx.compose.ui.graphics.lerp(base, Color.Black, 0.9f)
            }
        } ?: Color.White,
        label = "appBarTextColor"
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isBackgroundDark
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val expandedHeight = 170.dp // Slightly more to fit rating badge
                    val collapsedHeight = 64.dp
                    val currentHeight = lerp(expandedHeight, collapsedHeight, collapsedFraction) + statusBarHeight

                    // Expressive background frame
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(playerData?.options?.frame?.png)
                            .allowHardware(false)
                            .crossfade(true)
                            .build(),
                        onSuccess = { result ->
                            val bitmap = (result.result.drawable as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                Palette.from(bitmap).generate { palette ->
                                    palette?.dominantSwatch?.rgb?.let { rgb ->
                                        frameColor = Color(rgb)
                                    }
                                }
                            }
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentHeight)
                    )

                    // Dark overlay (Scrim) to ensure text readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0f),
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

                    LargeTopAppBar(
                        windowInsets = TopAppBarDefaults.windowInsets,
                        title = {
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isBackgroundDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f),
                                label = "titleBackground"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 24.dp) // More padding on end
                                    .background(
                                        color = backgroundColor,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp) // Reduced vertical padding
                            ) {
                                Text(
                                    text = "maimai",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = appBarTextColor
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                VerticalDivider(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .alpha(1f - collapsedFraction),
                                    color = appBarTextColor
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    MaimaiPlayerDetails(
                                        maiteaViewModel = maiteaViewModel,
                                        collapsedFraction = collapsedFraction,
                                        textColor = appBarTextColor
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                            titleContentColor = Color.White
                        )
                    )
                }
                if (isMaimaiImportStateReady) {
                    MaimaiChartSearchBar(
                        viewModel = maiteaViewModel,
                        navController = navController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface) // Ensure no gap color bleed
                            .padding(bottom = 12.dp)
                    )
                }
            }
        },
        bottomBar = {
            BottomBarPill(
                currentPage = currentPage,
                isLoading = isLoading,
                isMaimaiBestScoresEnabled = !isImportingScores,
                onPageChange = { newPage ->
                    maiteaViewModel.onPageChange(newPage)
                },
                onHomeClick = { showMiniMenu = !showMiniMenu },
                onSettingsClick = {
                    navController.navigate("settings")
                    showMiniMenu = false
                },
                onMaimaiUsersClick = {
                    navController.navigate("maimaiUsers")
                    showMiniMenu = false
                },
                onMaimaiBestScoreClick = {
                    navController.navigate("maimaiBest30Scores")
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isMaimaiImportStateReady) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    if (isLoading && data == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding() + 16.dp,
                            start = 12.dp,
                            end = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Log.d("data", "data: $data")
                        items(data?.data ?: emptyList()) { play ->
                            MaimaiScoreItem(
                                play = play,
                                onClick = { navController.navigate("maimaiScoresDetails/${play.id}") }
                            )
                        }
                    }
                }

                // Floating Mini Menu
                OpenMiniMenu(
                    showMiniMenu = showMiniMenu,
                    onDismiss = {
                        if (System.currentTimeMillis() - lastClickTime > 300) {
                            showMiniMenu = !showMiniMenu
                            lastClickTime = System.currentTimeMillis()
                        }
                    },
                    onItemClick = { gameId ->
                        navController.navigate("game/$gameId")
                        showMiniMenu = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp)
                )

                // Import Progress Overlay
                if (isImportingScores) {
                    MaimaiImportOverlay(
                        progress = importProgress,
                        message = importMessage ?: "Chargement de l'importation..."
                    )
                }
            }
        }
    }
}

@Composable
private fun MaimaiImportOverlay(
    progress: Int,
    message: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        label = "progressAnimation"
    )

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Importation des scores sur le serveur distant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    minLines = 2
                )

                Text(
                    text = "Cet import est fait en arrière-plan. Veuillez patienter.\n Les appels à MaiTea peuvent être longs.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }
    }
}
