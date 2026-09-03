package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import atomcity.shared.generated.resources.*
import coil3.compose.AsyncImage
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.MarkdownText
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.TaikoUser
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun TaikoScores(
    taikoViewModel: TaikoViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
) {
    val isLoading by taikoViewModel.isLoading.collectAsState()
    val isRefreshing by taikoViewModel.isRefreshing.collectAsState()
    val filteredScores by taikoViewModel.filteredScores.collectAsState()
    val searchQuery by taikoViewModel.searchQuery.collectAsState()
    val showOnlyFavorites by taikoViewModel.showOnlyFavorites.collectAsState()
    val dashboardData by taikoViewModel.dashboardData.collectAsState()
    val showDashboardTrigger by taikoViewModel.showDashboardTrigger.collectAsState()
    val currentPage by taikoViewModel._currentPage.collectAsState()

    var showGamesMenu by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    var showDashboardDialog by remember { mutableStateOf(value = false) }
    var doNotShowAgain by remember { mutableStateOf(value = false) }
    var lastClickMark by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val extraItems = listOf(
        Triple("taikoUserSettings", "Paramètres", "oue les paramètres"),
        Triple("taikoUsers", "Utilisateurs", "Consulter les utilisateurs enregistrés")
    )

    LaunchedEffect(showDashboardTrigger) {
        if (showDashboardTrigger) {
            showDashboardDialog = true
        }
    }

    LaunchedEffect(Unit) {
        taikoViewModel.getScores()
        taikoViewModel.fetchCommunityScores()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = Res.getUri("files/taiko/header.jpg"),
                            contentDescription = "Taiko no Tatsujin screen header.",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alpha = (1f - collapsedFraction * 0.8f).coerceIn(0f, 1f)
                        )

                        val shadowAlpha = (1f - collapsedFraction).coerceIn(0f, 1f)
                        // Top Shadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.9f * shadowAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopCenter)
                        )
                        // Bottom Shadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f * shadowAlpha)
                                        )
                                    )
                                )
                                .align(Alignment.BottomCenter)
                        )

                        LargeTopAppBar(
                            title = {
                                val titleOffsetX = lerp(0.dp, 0.dp, collapsedFraction)
                                val titleOffsetY = lerp(0.dp, 0.dp, collapsedFraction)
                                val nameOffsetX = lerp(0.dp, 0.dp, collapsedFraction)
                                val nameOffsetY = lerp(0.dp, -(2).dp, collapsedFraction)

                                TaikoPlayerDetails(
                                    taikoViewModel = taikoViewModel,
                                    collapsedFraction = collapsedFraction,
                                    titleOffsetX = titleOffsetX,
                                    titleOffsetY = titleOffsetY,
                                    nameOffsetX = nameOffsetX,
                                    nameOffsetY = nameOffsetY
                                )
                            },
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent,
                            ),
                            scrollBehavior = scrollBehavior,
                        )
                    }

                    // Search Bar
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = taikoViewModel::onSearchQueryChange,
                            showOnlyFavorites = showOnlyFavorites,
                            onToggleFavorites = taikoViewModel::onToggleShowOnlyFavorites
                        )
                    }
                }
            },
            bottomBar = {
                BottomBarPill(
                    currentPage = currentPage,
                    isLoading = isLoading,
                    hasNextPage = false,
                    showPagination = false,
                    onPageChange = { newPage ->
                        taikoViewModel.onPageChange(newPage)
                    },
                    onMenuClick = {
                        showActionsMenu = !showActionsMenu
                        showGamesMenu = false
                    },
                    onHomeClick = {
                        showGamesMenu = !showGamesMenu
                        showActionsMenu = false
                    },
                    onSettingsClick = onNavigateToSettings
                )
            },
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    taikoViewModel.getScores(forceRefresh = true)
                    taikoViewModel.fetchCommunityScores()
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                if (isLoading && !isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    MyScoresList(
                        scores = filteredScores,
                        onNavigateToRoute = onNavigateToRoute,
                        onFavoriteToggle = taikoViewModel::toggleFavorite
                    )
                }
            }
        }

        OpenMiniMenu(
            visible = showGamesMenu || showActionsMenu,
            onDismiss = {
                val now = TimeSource.Monotonic.markNow()
                if (now - lastClickMark > 300.milliseconds) {
                    showGamesMenu = false
                    showActionsMenu = false
                    lastClickMark = now
                }
            },
            onItemClick = { route ->
                onNavigateToRoute(route)
                showGamesMenu = false
                showActionsMenu = false
            },
            showGames = showGamesMenu,
            extraItems = if (showActionsMenu) extraItems else emptyList(),
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
        )

        if (showDashboardDialog && (dashboardData != null)) {
            DashboardDialog(
                dashboardData = dashboardData!!,
                doNotShowAgain = doNotShowAgain,
                onDoNotShowAgainChange = { doNotShowAgain = it },
                onDismiss = {
                    showDashboardDialog = false
                    taikoViewModel.dismissDashboard()
                    if (doNotShowAgain) {
                        taikoViewModel.setShowDashboardPreference(false)
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onToggleFavorites) {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoris",
                    tint = if (showOnlyFavorites) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Rechercher un morceau...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        }
    }
}

@Composable
private fun MyScoresList(
    scores: List<TaikoServerHistoryEntry>,
    onNavigateToRoute: (String) -> Unit,
    onFavoriteToggle: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(scores.size) { index ->
            TaikoScoreItem(scores[index], onNavigateToRoute, onFavoriteToggle)
        }
    }
}

@Composable
private fun DashboardDialog(
    dashboardData: String,
    doNotShowAgain: Boolean,
    onDoNotShowAgainChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownText(text = dashboardData)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onDoNotShowAgainChange(!doNotShowAgain)
                            }
                        }
                ) {
                    Checkbox(
                        checked = doNotShowAgain,
                        onCheckedChange = onDoNotShowAgainChange
                    )
                    Text(
                        text = "Ne plus afficher",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
