package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import kotlin.math.*
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.DifficultyRepository
import org.arcade.atomcity.data.LevelInfo
import org.arcade.atomcity.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.model.scorefetcher.playsResponse.*
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.utils.formatPlayDate
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.PlatformUtils
import org.koin.compose.koinInject
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScoresDetails(
    scoreEntry: ScorefetcherApiData? = null,
    scorefetcherViewModel: ScorefetcherViewModel? = null,
    onBackClick: () -> Unit,
    onHistoryClick: (Int) -> Unit = {}
) {
    val difficultyColor = getJacketBorderColor(scoreEntry?.difficultyLevel?.value)
    
    val difficultyRepository: DifficultyRepository = koinInject()
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }
    val scope = rememberCoroutineScope()

    val chartHistory by scorefetcherViewModel?.chartHistory?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val bestPerPlayer by scorefetcherViewModel?.bestPerPlayer?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val isLoading by scorefetcherViewModel?.isLoadingDetails?.collectAsState() ?: remember { mutableStateOf(false) }
    val isLoadingPlayById by scorefetcherViewModel?.isLoadingPlayById?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(scoreEntry?.song?.id, scoreEntry?.difficultyLevel?.key) {

        val song = scoreEntry?.song
        val diffLevel = scoreEntry?.difficultyLevel

        if (song?.id != null && diffLevel?.key != null) {
            levelInfo = difficultyRepository.getLevelByDifficulty(
                songId = song.id!!, 
                diffIndex = diffLevel.key!!,
                songTitle = song.name?.jp,
                altTitle = song.name?.en
            )
        }
        
        val songNameForHistory = song?.name?.en ?: song?.name?.jp
        songNameForHistory?.let {
            scorefetcherViewModel?.fetchChartHistory(it, diffLevel?.value)
            scorefetcherViewModel?.fetchBestPerPlayer(it, diffLevel?.value)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Détails du Score",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (scoreEntry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading || isLoadingPlayById) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Impossible de charger les détails du score",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val song = scoreEntry.song
            val diffLevel = scoreEntry.difficultyLevel
            val songName = song?.name
            val songArtist = song?.artist
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            ElevatedCard(
                colors = getDifficultyColorBackground(diffLevel?.value),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Badges + Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaimaiScoreBadgeRow(
                            scoreEntry = scoreEntry,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = formatPlayDate(scoreEntry.playDate),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Jacket
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(difficultyColor.copy(alpha = 0.1f), CircleShape)
                        )
                        AsyncImage(
                            model = scoreEntry.jacketImageUrl,
                            contentDescription = songName?.jp,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(4.dp, difficultyColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Song Info
                    Text(
                        text = songName?.jp ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (songName?.en != null && songName.en != songName.jp) {
                        Text(
                            text = songName.en ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.alpha(0.5f).padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val artistJp = songArtist?.jp
                    val artistEn = songArtist?.en
                    
                    Text(
                        text = artistJp ?: artistEn ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.alpha(0.8f)
                    )
                    
                    if (artistEn != null && artistEn != artistJp) {
                        Text(
                            text = artistEn,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.alpha(0.4f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Achievement Display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (scoreEntry.rank != null) {
                            Text(
                                text = scoreEntry.rank!!,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = difficultyColor,
                                    fontSize = 32.sp
                                ),
                                modifier = Modifier.padding(bottom = 12.dp, end = 8.dp)
                            )
                        }

                        Text(
                            text = scoreEntry.achievementFormattedFixed?.replace("%", "") ?: "0.00",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 56.sp,
                                letterSpacing = (-2).sp
                            )
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = difficultyColor
                            ),
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Difficulty Badge with Level Info
                    MaimaiDifficultyBadge(
                        difficultyValue = diffLevel?.value,
                        levelInfo = levelInfo,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed Stats Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DETAILS DU SCORE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = difficultyColor
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                        listOf("PERFECT", "GREAT", "GOOD", "MISS").forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )

                    val detail = scoreEntry.scoreDetail

                    detail?.tap?.let {
                        DetailRow("Taps", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.hold?.let { 
                        DetailRow("Holds", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.slide?.let { 
                        DetailRow("Slides", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.breakk?.let {
                        DetailRow("Breaks", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.hits?.let {
                        DetailRow("Total", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                }
            }

            // Best results per player
            if (isLoading && bestPerPlayer.isEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else if (bestPerPlayer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MEILLEURS SCORES D'ATOM CITY DE CETTE CHART",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = difficultyColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 8.dp)
                )

                bestPerPlayer.forEach { b ->
                    BestPerPlayerItem(b)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (isLoading && chartHistory.isEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else if (chartHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                if (chartHistory.size < 3) {
                    GraphPlaceholder(difficultyColor)
                } else {
                    PersonalBestProgressionGraph(
                        chartHistory = chartHistory,
                        difficultyColor = difficultyColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ScoreHistoryGraph(
                        chartHistory = chartHistory,
                        difficultyColor = difficultyColor
                    )

                    val isUtage = diffLevel?.value?.lowercase() == "utage" || 
                                 diffLevel?.label == "宴"

                    if (!isUtage) {
                        Spacer(modifier = Modifier.height(24.dp))

                        RatingVsScoreGraph(
                            chartHistory = chartHistory,
                            difficultyColor = difficultyColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "HISTORIQUE DES SCORES • ${chartHistory.size} ESSAI(S)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = difficultyColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 8.dp)
                )

                chartHistory.forEach { historyEntry ->
                    ChartHistoryItem(historyEntry, onClick = { historyEntry.playId?.let { onHistoryClick(it) } })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
}

@Composable
fun ChartHistoryItem(historyEntry: ChartHistoryResponse, onClick: () -> Unit = {}) {
    val difficultyColor = getJacketBorderColor(historyEntry.difficultyLevelJson?.value?.lowercase())
    val label = historyEntry.difficultyLevelJson?.label
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatPlayDate(historyEntry.playDate),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val diffJson = historyEntry.difficultyLevelJson
                    Text(
                        text = if (label == "宴") {
                            (diffJson?.label + " (Utage)") ?: ""
                        } else {
                            "${diffJson?.label ?: ""} ${historyEntry.difficultyLevel ?: ""}"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = historyEntry.rank ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
                Text(
                    text = "${((historyEntry.achievement ?: 0.0) / 100.0).format(2)}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun BestPerPlayerItem(b: BestPerPlayerResponse) {
    val difficultyColor = getJacketBorderColor(b.difficultyLevelJson?.value?.lowercase())
    val label = b.difficultyLevelJson?.label
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatPlayDate(b.playDate),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = b.playerName ?: "",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        val bDiffJson = b.difficultyLevelJson
                        Text(
                            text = if (label == "宴") {
                                (bDiffJson?.label + " (Utage)") ?: ""
                            } else {
                                "${bDiffJson?.label ?: ""} ${b.difficultyLevel ?: ""}"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (label != "宴") {
                            val displayRating = b.rating?.format(2) ?: b.ratingFormatted ?: ""
                            if (displayRating.isNotEmpty()) {
                                Text(
                                    text = displayRating,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.alpha(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = b.rank ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
                Text(
                    text = "${((b.achievement ?: 0.0) / 100.0).format(2)}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun MaimaiScoreBadgeRow(scoreEntry: ScorefetcherApiData, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (scoreEntry.isHighScore == true) {
            ScoreBadge(
                text = "MEILLEUR SCORE",
                containerColor = Color(0xFFFFF9C4),
                contentColor = Color(0xFFFBC02D)
            )
        }
        if (scoreEntry.fullCombo == 1) {
            val hasGreats = (scoreEntry.scoreDetail?.hits?.great ?: 0) > 0
            ScoreBadge(
                text = "FULL COMBO",
                containerColor = if (hasGreats) Color(0xFFE3F2FD) else Color(0xFFFFF9C4),
                contentColor = if (hasGreats) Color(0xFF1976D2) else Color(0xFFFBC02D)
            )
        }
        if (scoreEntry.isAllPerfect == true) {
            val allBreaksPerfect = (scoreEntry.scoreDetail?.breakk?.great ?: 0) == 0 &&
                    (scoreEntry.scoreDetail?.breakk?.good ?: 0) == 0 &&
                    (scoreEntry.scoreDetail?.breakk?.bad ?: 0) == 0
            
            ScoreBadge(
                text = if (allBreaksPerfect) "ALL PERFECT +" else "ALL PERFECT",
                containerColor = Color(0xFFE0F2F1),
                contentColor = Color(0xFF00897B)
            )
        }
        if (scoreEntry.isTrackSkip == true) {
            ScoreBadge(
                text = "TRACK SKIP",
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFE53935)
            )
        }
    }
}

@Composable
fun ScoreBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = contentColor
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailRow(label: String, p: Int, gr: Int, gd: Int, m: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1.2f)
        )
        
        // Value columns with pill-shaped backgrounds
        listOf(
            p to Color(0xFFFFD700),     // Perfect - Gold
            gr to Color(0xFFFF4081),    // Great - Pink
            gd to Color(0xFF00E676),    // Good - Green
            m to Color.Gray             // Miss - Gray
        ).forEach { (count, color) ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (count > 0) {
                    Surface(
                        color = color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = color
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Composable
fun GraphPlaceholder(difficultyColor: Color) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Pour afficher les graphiques, faites au moins 3 essais de cette chart !",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = difficultyColor.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreHistoryGraph(
    chartHistory: List<ChartHistoryResponse>,
    difficultyColor: Color
) {
    val sortedHistory = remember(chartHistory) {
        chartHistory.sortedBy { it.playDate }.takeLast(20)
    }

    if (sortedHistory.size < 3) {
        GraphPlaceholder(difficultyColor)
        return
    }

    val maxAchievement = remember(sortedHistory) {
        val maxVal = sortedHistory.maxOfOrNull { it.achievement ?: 0.0 } ?: 10000.0
        val minVal = sortedHistory.minOfOrNull { it.achievement ?: 0.0 } ?: 0.0
        val padding = if (maxVal == minVal) 1000.0 else (maxVal - minVal) * 0.1
        maxVal + padding
    }
    val minAchievement = remember(sortedHistory) {
        val maxVal = sortedHistory.maxOfOrNull { it.achievement ?: 0.0 } ?: 10000.0
        val minVal = sortedHistory.minOfOrNull { it.achievement ?: 0.0 } ?: 0.0
        val padding = if (maxVal == minVal) 1000.0 else (maxVal - minVal) * 0.1
        (minVal - padding).coerceAtLeast(0.0)
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipData by remember { mutableStateOf<Pair<ChartHistoryResponse, Offset>?>(null) }
    val haptic = LocalHapticFeedback.current


    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "PROGRESSION DU SCORE SUR LE TEMPS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(sortedHistory) {
                            detectTapGestures { offset ->
                                val stepX = if (sortedHistory.size > 1) size.width.toFloat() / (sortedHistory.size - 1) else size.width.toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                val entry = sortedHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - (((entry.achievement ?: 0.0) - minAchievement) / (maxAchievement - minAchievement) * size.height.toFloat()).toFloat()
                                tooltipData = entry to Offset(x, y)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        .pointerInput(sortedHistory) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = if (sortedHistory.size > 1) size.width.toFloat() / (sortedHistory.size - 1) else size.width.toFloat()
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                val entry = sortedHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - (((entry.achievement ?: 0.0) - minAchievement) / (maxAchievement - minAchievement) * size.height.toFloat()).toFloat()
                                
                                if (tooltipData?.first != entry) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    tooltipData = entry to Offset(x, y)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    sortedHistory.forEachIndexed { index, entry ->
                        val x = index.toFloat() * stepX
                        val achievement = entry.achievement ?: 0.0
                        val y = height - ((achievement - minAchievement) / (maxAchievement - minAchievement) * height).toFloat()

                        val point = Offset(x, y)
                        points.add(point)

                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }

                    // Draw background grid lines
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val y = height * i / gridLines
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.1f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Path
                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Points
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            center = point,
                            radius = 10.dp.toPx()
                        )
                        drawCircle(
                            color = difficultyColor,
                            center = point,
                            radius = 7.dp.toPx()
                        )
                    }
                }

                // Tooltip / Overlay for selected point
                tooltipData?.let { (entry, offset) ->
                    Surface(
                        modifier = Modifier.offset {
                            IntOffset(
                                (offset.x - 50.dp.toPx()).toInt().coerceIn(0, (canvasSize.width - 100.dp.toPx()).toInt()),
                                (offset.y - 75.dp.toPx()).toInt().coerceAtLeast(0)
                            )
                        },
                        color = difficultyColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Text(
                                text = formatPlayDate(entry.playDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun RatingVsScoreGraph(
    chartHistory: List<ChartHistoryResponse>,
    difficultyColor: Color
) {
    val dataPoints = remember(chartHistory) {
        val last20 = chartHistory.sortedBy { it.playDate }.takeLast(20)
        val isUtageInternal = last20.any { it.difficultyLevel == "宴" || it.difficultyLevelJson?.value?.lowercase() == "utage" }
        if (isUtageInternal) {
            last20
                .filter { it.achievement != null }
                .sortedBy { it.achievement }
        } else {
            last20
                .filter { it.achievement != null && it.rating != null }
                .sortedBy { it.achievement }
        }
    }

    if (dataPoints.size < 3) {
            GraphPlaceholder(difficultyColor)
            return
    }


    val minAchievement = dataPoints.first().achievement!!
    val maxAchievement = dataPoints.last().achievement!!

    val isUtage = dataPoints.any { it.difficultyLevel == "宴" || it.difficultyLevelJson?.value?.lowercase() == "utage" }
    val minRating = if (isUtage) 0.0 else dataPoints.minOf { it.rating!! }
    val maxRating = if (isUtage) 1.0 else dataPoints.maxOf { it.rating!! }



    val xRange = (maxAchievement - minAchievement).coerceAtLeast(100.0)
    val yRange = (maxRating - minRating).coerceAtLeast(1.0)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipData by remember { mutableStateOf<Pair<ChartHistoryResponse, Offset>?>(null) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "PROGRESSION DU SCORE PARMI LES ESSAIS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val entry = dataPoints.minByOrNull { e ->
                                    val x = ((e.achievement!! - minAchievement) / xRange * size.width).toFloat()
                                    abs(offset.x - x)
                                } ?: return@detectTapGestures
                                val x = ((entry.achievement!! - minAchievement) / xRange * size.width).toFloat()
                                val y = (size.height - (((entry.rating ?: 0.0) - minRating) / yRange * size.height)).toFloat()
                                
                                tooltipData = entry to Offset(x, y)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        .pointerInput(dataPoints) {
                            detectHorizontalDragGestures { change, _ ->
                                val entry = dataPoints.minByOrNull { e ->
                                    val x = ((e.achievement!! - minAchievement) / xRange * size.width).toFloat()
                                    abs(change.position.x - x)
                                } ?: return@detectHorizontalDragGestures
                                val x = ((entry.achievement!! - minAchievement) / xRange * size.width).toFloat()
                                val y = (size.height - (((entry.rating ?: 0.0) - minRating) / yRange * size.height)).toFloat()
                                
                                if (tooltipData?.first != entry) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    tooltipData = entry to Offset(x, y)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw grid
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val x = width * i / gridLines
                        val y = height * i / gridLines
                        drawLine(Color.Gray.copy(0.1f), Offset(x, 0f), Offset(x, height), 1.dp.toPx())
                        drawLine(Color.Gray.copy(0.1f), Offset(0f, y), Offset(width, y), 1.dp.toPx())
                    }

                    val points = dataPoints.map { entry ->
                        val x = ((entry.achievement!! - minAchievement) / xRange * width).toFloat()
                        val y = (height - (((entry.rating ?: 0.0) - minRating) / yRange * height)).toFloat()
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = difficultyColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    points.forEach { point ->
                        drawCircle(Color.White, radius = 10.dp.toPx(), center = point)
                        drawCircle(difficultyColor, radius = 7.dp.toPx(), center = point)
                    }
                }

                // Tooltip
                tooltipData?.let { (entry, offset) ->
                    Surface(
                        modifier = Modifier.offset {
                            IntOffset(
                                (offset.x - 50.dp.toPx()).toInt().coerceIn(0, (canvasSize.width - 100.dp.toPx()).toInt()),
                                (offset.y - 60.dp.toPx()).toInt().coerceAtLeast(0)
                            )
                        },
                        color = difficultyColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            if (entry.difficultyLevel != "宴" && entry.difficultyLevelJson?.value?.lowercase() != "utage") {
                                val displayRating = entry.rating?.format(2) ?: entry.ratingFormatted
                                if (!displayRating.isNullOrBlank()) {
                                    Text(
                                        text = "Rating : $displayRating",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
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

@Composable
fun PersonalBestProgressionGraph(
    chartHistory: List<ChartHistoryResponse>,
    difficultyColor: Color
) {
    val pbHistory = remember(chartHistory) {
        val sorted = chartHistory.sortedBy { it.playDate }
        val result = mutableListOf<ChartHistoryResponse>()
        var currentMax = -1.0
        sorted.forEach { entry ->
            val achievement = entry.achievement ?: 0.0
            if (achievement > currentMax) {
                result.add(entry)
                currentMax = achievement
            }
        }
        result.takeLast(20)
    }

    val maxAchievement = remember(pbHistory) {
        val maxVal = pbHistory.maxOfOrNull { it.achievement ?: 0.0 } ?: 10000.0
        val minVal = pbHistory.minOfOrNull { it.achievement ?: 0.0 } ?: 0.0
        val padding = if (maxVal == minVal) 1000.0 else (maxVal - minVal) * 0.1
        maxVal + padding
    }
    val minAchievement = remember(pbHistory) {
        val maxVal = pbHistory.maxOfOrNull { it.achievement ?: 0.0 } ?: 10000.0
        val minVal = pbHistory.minOfOrNull { it.achievement ?: 0.0 } ?: 0.0
        val padding = if (maxVal == minVal) 1000.0 else (maxVal - minVal) * 0.1
        (minVal - padding).coerceAtLeast(0.0)
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipData by remember { mutableStateOf<Pair<ChartHistoryResponse, Offset>?>(null) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "PROGRESSION DES RECORDS PERSONNELS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(pbHistory) {
                            detectTapGestures { offset ->
                                val stepX = if (pbHistory.size > 1) size.width.toFloat() / (pbHistory.size - 1) else size.width.toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, pbHistory.size - 1)
                                val entry = pbHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - (((entry.achievement ?: 0.0) - minAchievement) / (maxAchievement - minAchievement) * size.height.toFloat()).toFloat()
                                tooltipData = entry to Offset(x, y)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        .pointerInput(pbHistory) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = if (pbHistory.size > 1) size.width.toFloat() / (pbHistory.size - 1) else size.width.toFloat()
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, pbHistory.size - 1)
                                val entry = pbHistory[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - (((entry.achievement ?: 0.0) - minAchievement) / (maxAchievement - minAchievement) * size.height.toFloat()).toFloat()
                                
                                if (tooltipData?.first != entry) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    tooltipData = entry to Offset(x, y)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = if (pbHistory.size > 1) width / (pbHistory.size - 1) else width

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    pbHistory.forEachIndexed { index, entry ->
                        val x = index.toFloat() * stepX
                        val achievement = entry.achievement ?: 0.0
                        val y = height - ((achievement - minAchievement) / (maxAchievement - minAchievement) * height).toFloat()

                        val point = Offset(x, y)
                        points.add(point)

                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }

                    // Draw background grid lines
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val y = height * i / gridLines
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.1f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Path
                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Points
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            center = point,
                            radius = 10.dp.toPx()
                        )
                        drawCircle(
                            color = difficultyColor,
                            center = point,
                            radius = 7.dp.toPx()
                        )
                    }
                }

                // Tooltip / Overlay for selected point
                tooltipData?.let { (entry, offset) ->
                    Surface(
                        modifier = Modifier.offset {
                            IntOffset(
                                (offset.x - 50.dp.toPx()).toInt().coerceIn(0, (canvasSize.width - 100.dp.toPx()).toInt()),
                                (offset.y - 75.dp.toPx()).toInt().coerceAtLeast(0)
                            )
                        },
                        color = difficultyColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Text(
                                text = formatPlayDate(entry.playDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            if (entry.difficultyLevel != "宴" && entry.difficultyLevelJson?.value?.lowercase() != "utage") {
                                val displayRating = entry.rating?.format(2) ?: entry.ratingFormatted
                                if (!displayRating.isNullOrBlank()) {
                                    Text(
                                        text = "Rating : $displayRating",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
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
