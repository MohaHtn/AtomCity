package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atomcity.shared.generated.resources.*
import coil3.compose.AsyncImage
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.MarkdownText
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun TaikoScores(
    taikoViewModel: TaikoViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val isLoading by taikoViewModel.isLoading.collectAsState()
    var showGamesMenu by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    var lastClickMark by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }
    val filteredScores by taikoViewModel.filteredScores.collectAsState()
    val searchQuery by taikoViewModel.searchQuery.collectAsState()
    val dashboardData by taikoViewModel.dashboardData.collectAsState()
    val showDashboardTrigger by taikoViewModel.showDashboardTrigger.collectAsState()
    var showDashboardDialog by remember { mutableStateOf(false) }
    var doNotShowAgain by remember { mutableStateOf(false) }

    LaunchedEffect(showDashboardTrigger) {
        if (showDashboardTrigger) {
            showDashboardDialog = true
        }
    }

    val extraItems = listOf(
        Triple("taikoUserSettings", "Paramètres", "oue les paramètres")
    )

    val isRefreshing by taikoViewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        taikoViewModel.getScores()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box {
                    AsyncImage(
                        model = Res.getUri("files/taiko/header.jpg"),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = (1f - collapsedFraction * 0.7f).coerceIn(0f, 1f)
                    )
                    
                    val shadowAlpha = (1f - collapsedFraction).coerceIn(0f, 1f)
                    // Top Shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f * shadowAlpha), Color.Transparent)
                                )
                            )
                            .align(Alignment.TopCenter)
                    )
                    // Bottom Shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f * shadowAlpha))
                                )
                            )
                            .align(Alignment.BottomCenter)
                    )

                    LargeTopAppBar(
                        title = {
                            TaikoPlayerDetails(
                                taikoViewModel = taikoViewModel,
                                collapsedFraction = collapsedFraction
                            )
                        },
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                BottomBarPill(
                    currentPage = taikoViewModel._currentPage.collectAsState().value,
                    isLoading = isLoading,
                    hasNextPage = false,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Fixed Search Bar (Sticky)
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
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { taikoViewModel.onSearchQueryChange(it) },
                            singleLine = true,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Rechercher un morceau...",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { taikoViewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                            }
                        }
                    }
                }

                @Composable
                fun ScoresContent() {
                    if (isLoading && !isRefreshing) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filteredScores.isNotEmpty()) {
                                items(filteredScores.size) { index ->
                                    val score = filteredScores[index]
                                    Card(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .clickable {
                                                score.songId?.let { id ->
                                                    onNavigateToRoute("taikoScoresDetails/$id")
                                                }
                                            },
                                        colors = setDifficultyColorBackground(score.difficulty),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Image(
                                                painter = painterResource(getDifficultyDrawable(score.difficulty)),
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                                    .size(120.dp),
                                                contentScale = ContentScale.Fit,
                                                alpha = 0.3f
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = score.musicName ?: "Song ${score.songId}",
                                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            color = Color.White
                                                        )
                                                        if (!score.musicNameEN.isNullOrBlank() && score.musicNameEN != score.musicName) {
                                                            Text(
                                                                text = score.musicNameEN ?: "",
                                                                style = MaterialTheme.typography.titleSmall,
                                                                color = Color.White.copy(alpha = 0.9f),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        Text(
                                                            text = score.musicArtist ?: "",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.White.copy(alpha = 0.7f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "${displayDifficultyName(score.difficulty)} (${score.stars ?: 0})",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = "★".repeat(score.stars ?: 0),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.Yellow
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = score.score.toString(),
                                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                                            color = Color.White
                                                        )
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            ScoreBadge("GOOD", score.goodCount, Color(0xFFFFD700))
                                                            ScoreBadge("OK", score.okCount, Color(0xFFC0C0C0))
                                                            ScoreBadge("MISS", score.missCount, Color(0xFFE57373))
                                                        }
                                                    }
                                                    
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        if ((score.comboCount ?: 0) > 0) {
                                                            Text(
                                                                text = "COMBO ${score.comboCount}",
                                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                                color = Color.White
                                                            )
                                                        }
                                                        Text(
                                                            text = formatPlayDate(score.playTime.toString()),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.7f)
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

                if (collapsedFraction <= 0.01f) {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { taikoViewModel.getScores(forceRefresh = true) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ScoresContent()
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScoresContent()
                    }
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

        if (showDashboardDialog && dashboardData != null) {
            AlertDialog(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
                onDismissRequest = {
                    showDashboardDialog = false
                    taikoViewModel.dismissDashboard()
                    if (doNotShowAgain) {
                        taikoViewModel.setShowDashboardPreference(false)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        MarkdownText(text = dashboardData!!)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        doNotShowAgain = !doNotShowAgain
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = doNotShowAgain,
                                onCheckedChange = { doNotShowAgain = it }
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
                    TextButton(
                        onClick = {
                            showDashboardDialog = false
                            taikoViewModel.dismissDashboard()
                            if (doNotShowAgain) {
                                taikoViewModel.setShowDashboardPreference(false)
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ScoreBadge(label: String, count: Int?, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = count?.toString() ?: "0",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

fun displayDifficultyName(difficulty: Int?): String {
    return when (difficulty) {
        1 -> "Kantan (Facile)"
        2 -> "Futsuu (Normal)"
        3 -> "Muzukashii (Difficile)"
        4 -> "Oni (Démoniaque)"
        5 -> "Ura Oni (Ultra Démoniaque)"
        else -> "Inconnu"
    }
}

fun getDifficultyDrawable(difficulty: Int?): DrawableResource {
    return when (difficulty) {
        1 -> Res.drawable.taiko_difficulty_easy
        2 -> Res.drawable.taiko_difficulty_normal
        3 -> Res.drawable.taiko_difficulty_hard
        4 -> Res.drawable.taiko_difficulty_evil
        5 -> Res.drawable.taiko_difficulty_uraoni
        else -> Res.drawable.taiko_difficulty_easy
    }
}

fun getDifficultyColor(difficulty: Int?): Color {
    return when (difficulty) {
        1 -> Color(0xFFCF2C00)
        2 -> Color(0xFF657E25)
        3 -> Color(0xFF223004)
        4 -> Color(0xFFCE2D76)
        5 -> Color(0xFF6B1D8C)
        else -> Color.Gray
    }
}

@Composable
fun setDifficultyColorBackground(difficulty: Int?): CardColors {
    return CardDefaults.cardColors(containerColor = getDifficultyColor(difficulty))
}
