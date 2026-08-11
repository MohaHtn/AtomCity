package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atomcity.shared.generated.resources.*
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScores(
    taikoViewModel: TaikoViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val isLoading by taikoViewModel.isLoading.collectAsState()
    var showMiniMenu by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    var lastClickMark by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }
    val scoresData by taikoViewModel.scoresData.collectAsState()

    LaunchedEffect(Unit) {
        taikoViewModel.getScores()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        TaikoPlayerDetails(
                            taikoViewModel = taikoViewModel,
                            collapsedFraction = collapsedFraction
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
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
                        showMiniMenu = !showMiniMenu
                    },
                    onHomeClick = {
                        showMiniMenu = !showMiniMenu
                    },
                    onSettingsClick = onNavigateToSettings
                )
            },
        ) { paddingValues ->
            Box {
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
                            if (dataList.taikoServerSongHistoryData.isNotEmpty()) {
                                item {
                                    TaikoProgressionGraph(
                                        history = dataList.taikoServerSongHistoryData,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

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
                                            painter = painterResource(getDifficultyDrawable(score.difficulty)),
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.BottomEnd)
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
        }

        OpenMiniMenu(
            showMiniMenu = showMiniMenu,
            onDismiss = {
                val now = TimeSource.Monotonic.markNow()
                if (now - lastClickMark > 300.milliseconds) {
                    showMiniMenu = false
                    lastClickMark = now
                }
            },
            onItemClick = { route ->
                onNavigateToRoute(route)
                showMiniMenu = false
            },
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)
        )
    }
}

@Composable
fun TaikoProgressionGraph(
    history: List<TaikoServerHistoryEntry>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sortedHistory = remember(history) {
        history.takeLast(20) 
    }

    if (sortedHistory.size < 3) return

    val maxScore = sortedHistory.maxOfOrNull { it.score ?: 0 }?.toFloat() ?: 1000000f
    val minScore = sortedHistory.minOfOrNull { it.score ?: 0 }?.toFloat() ?: 0f
    val scoreRange = (maxScore - minScore).coerceAtLeast(10000f)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipData by remember { mutableStateOf<Pair<TaikoServerHistoryEntry, Offset>?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PROGRESSION DES SCORES TAIKO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(sortedHistory) {
                            detectTapGestures { offset ->
                                val stepX = size.width.toFloat() / (sortedHistory.size - 1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                val entry = sortedHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - ((entry.score?.toFloat() ?: 0f) - minScore) / scoreRange * size.height.toFloat()
                                tooltipData = entry to Offset(x, y)
                                PlatformUtils.hapticImpact()
                            }
                        }
                        .pointerInput(sortedHistory) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = size.width.toFloat() / (sortedHistory.size - 1)
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                val entry = sortedHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - ((entry.score?.toFloat() ?: 0f) - minScore) / scoreRange * size.height.toFloat()
                                
                                if (tooltipData?.first != entry) {
                                    PlatformUtils.hapticTick()
                                    tooltipData = entry to Offset(x, y)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (sortedHistory.size - 1)

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    sortedHistory.forEachIndexed { index, entry ->
                        val x = index * stepX
                        val y = height - ((entry.score?.toFloat() ?: 0f) - minScore) / scoreRange * height
                        val point = Offset(x, y)
                        points.add(point)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFFCF2C00),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.forEach { point ->
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = point)
                        drawCircle(Color(0xFFCF2C00), radius = 2.dp.toPx(), center = point)
                    }
                }

                tooltipData?.let { (entry, offset) ->
                    val xOffset = with(density) { (offset.x - 40.dp.toPx()).toInt() }
                    val yOffset = with(density) { (offset.y - 50.dp.toPx()).toInt() }
                    
                    Surface(
                        modifier = Modifier.offset {
                            IntOffset(
                                xOffset.coerceIn(0, (canvasSize.width - with(density) { 80.dp.toPx() }.toInt())),
                                yOffset.coerceAtLeast(0)
                            )
                        },
                        color = Color(0xFFCF2C00),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(entry.musicName ?: "Taiko", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            Text("${entry.score}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
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

@Composable
fun setDifficultyColorBackground(difficulty: Int?): CardColors {
    return when (difficulty) {
        1 -> CardDefaults.cardColors(containerColor = Color(0xFFCF2C00))
        2 -> CardDefaults.cardColors(containerColor = Color(0xFF657E25))
        3 -> CardDefaults.cardColors(containerColor = Color(0xFF223004))
        4 -> CardDefaults.cardColors(containerColor = Color(0xFFCE2D76))
        5 -> CardDefaults.cardColors(containerColor = Color(0xFF6B1D8C))
        else -> CardDefaults.cardColors(containerColor = Color.Gray)
    }
}
