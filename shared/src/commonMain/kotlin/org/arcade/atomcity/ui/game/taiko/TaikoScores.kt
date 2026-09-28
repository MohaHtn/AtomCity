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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atomcity.shared.generated.resources.*
import coil3.compose.AsyncImage
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.MarkdownText
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
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
    val scoresData by taikoViewModel.scoresData.collectAsState()
    val filteredScores by taikoViewModel.filteredScores.collectAsState()
    val searchQuery by taikoViewModel.searchQuery.collectAsState()
    val showOnlyFavorites by taikoViewModel.showOnlyFavorites.collectAsState()
    val dashboardData by taikoViewModel.dashboardData.collectAsState()
    val showDashboardTrigger by taikoViewModel.showDashboardTrigger.collectAsState()
    val currentPage by taikoViewModel._currentPage.collectAsState()

    val isDataLoading = isLoading || isRefreshing || scoresData == null

    var showGamesMenu by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    var showDashboardDialog by remember { mutableStateOf(value = false) }
    var doNotShowAgain by remember { mutableStateOf(value = false) }
    var lastClickMark by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val extraItems = listOf(
        Triple("taikoMostPlayed", "Les plus joués", "Les morceaux les plus joués"),
        Triple("taikoBestScores", "30 Meilleurs scores", "Vos 30 Meilleurs scores"),
        Triple("taikoProgress", "Complétion", "Consulter la progression de tous les joueurs"),
        Triple("taikoDan", "Dan Dojo", "Vos essais au Dan Dojo"),
        Triple("taikoUserSettings", "Paramètres", "Modifier votre profil de jeu"),
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
                                TaikoPlayerDetails(
                                    taikoViewModel = taikoViewModel,
                                    collapsedFraction = collapsedFraction,
                                    titleOffsetY = 4.dp
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
                            onToggleFavorites = taikoViewModel::onToggleShowOnlyFavorites,
                            enabled = !isDataLoading
                        )
                    }
                }
            },
            bottomBar = {
                BottomBarPill(
                    currentPage = currentPage,
                    isLoading = isDataLoading,
                    hasNextPage = false,
                    showPagination = false,
                    onPageChange = { newPage ->
                        if (!isDataLoading) {
                            taikoViewModel.onPageChange(newPage)
                        }
                    },
                    onMenuClick = {
                        if (!isDataLoading) {
                            showActionsMenu = !showActionsMenu
                            showGamesMenu = false
                        }
                    },
                    onHomeClick = {
                        if (!isDataLoading) {
                            showGamesMenu = !showGamesMenu
                            showActionsMenu = false
                        }
                    },
                    onSettingsClick = {
                        if (!isDataLoading) {
                            onNavigateToSettings()
                        }
                    }
                )
            },
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    if (!isDataLoading) {
                        taikoViewModel.getScores(forceRefresh = true)
                        taikoViewModel.fetchCommunityScores()
                    }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                if (isDataLoading) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else if (filteredScores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Aucun score ne correspond à la recherche" else "Aucun score disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    ScoresList(
                        scores = filteredScores,
                        onNavigateToRoute = { route ->
                            if (!isDataLoading) {
                                onNavigateToRoute(route)
                            }
                        },
                        onFavoriteToggle = { songId ->
                            if (!isDataLoading) {
                                taikoViewModel.toggleFavorite(songId)
                            }
                        }
                    )
                }
            }
        }

        OpenMiniMenu(
            visible = (showGamesMenu || showActionsMenu) && !isDataLoading,
            onDismiss = {
                val now = TimeSource.Monotonic.markNow()
                if (now - lastClickMark > 300.milliseconds) {
                    showGamesMenu = false
                    showActionsMenu = false
                    lastClickMark = now
                }
            },
            onItemClick = { route ->
                if (!isDataLoading) {
                    onNavigateToRoute(route)
                    showGamesMenu = false
                    showActionsMenu = false
                }
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
    onToggleFavorites: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onToggleFavorites,
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoris",
                    tint = if (showOnlyFavorites) {
                        if (enabled) Color.Red else Color.Red.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
                    }
                )
            }
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f)),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Rechercher un morceau...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.38f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    enabled = enabled
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoresList(
    scores: List<TaikoServerHistoryEntry>,
    onNavigateToRoute: (String) -> Unit,
    onFavoriteToggle: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(scores.size) { index ->
            TaikoScoreItem(
                score = scores[index],
                onNavigateToRoute = onNavigateToRoute,
                onFavoriteToggle = onFavoriteToggle
            )
            Spacer(modifier = Modifier.height(8.dp))
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
