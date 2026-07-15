package org.arcade.atomcity.ui.game.maimai

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(
    maiteaViewModel: MaiteaViewModel,
    navController: NavHostController
) {
    val isLoading by maiteaViewModel.isLoading.collectAsState()
    val data by maiteaViewModel.data.collectAsState()
    val playerDataState by maiteaViewModel.playerData.collectAsState()
    val playerData = playerDataState?.data?.firstOrNull()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    var frameColor by remember { mutableStateOf<Color?>(null) }
    var showMiniMenu by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val currentPage by maiteaViewModel._currentPage.collectAsState()

    LaunchedEffect(Unit) {
        maiteaViewModel.fetchMaimaiPaginatedData(page = currentPage)
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
            Box(modifier = Modifier.fillMaxWidth()) {
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
                        .height(lerp(210.dp, 135.dp, collapsedFraction))
                )

                // Dark overlay (Scrim) to ensure text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lerp(210.dp, 135.dp, collapsedFraction))
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                            MaimaiPlayerDetails(
                                maiteaViewModel = maiteaViewModel,
                                collapsedFraction = collapsedFraction,
                                onBackClick = { },
                                topAppBarWidth = 0.dp,
                                topAppBarHeight = 0.dp,
                                textColor = appBarTextColor
                            )
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
        },
        bottomBar = {
            BottomBarPill(
                currentPage = currentPage,
                isLoading = isLoading,
                onPageChange = { newPage ->
                    maiteaViewModel.onPageChange(newPage)
                    maiteaViewModel.fetchMaimaiPaginatedData(newPage)
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
            if (isLoading && data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 12.dp,
                        end = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = maiteaViewModel.playsDataSize) { index ->
                        val play = data?.data?.get(index) ?: return@items
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
        }
    }
}
