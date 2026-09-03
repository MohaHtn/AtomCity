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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.BorderStroke
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
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.domain.model.LevelInfo
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.*
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.game.maimai.details.*
import org.arcade.atomcity.utils.formatPlayDate
import org.arcade.atomcity.utils.format
import org.koin.compose.koinInject
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScoresDetails(
    scoreEntry: ScorefetcherApiData? = null,
    maimaiViewModel: MaimaiViewModel? = null,
    onBackClick: () -> Unit,
    onHistoryClick: (Int) -> Unit = {}
) {
    val song = scoreEntry?.song
    val songName = song?.name
    val songArtist = song?.artist
    val difficultyColor = getJacketBorderColor(scoreEntry?.difficultyLevel?.value)
    val isDark = isAppInDarkTheme()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    
    val difficultyRepository: IDifficultyRepository = koinInject()
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }
    var showScoreInfoSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val chartHistory by maimaiViewModel?.chartHistory?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val bestPerPlayer by maimaiViewModel?.bestPerPlayer?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val isLoading by maimaiViewModel?.isLoadingDetails?.collectAsState() ?: remember { mutableStateOf(false) }
    val isLoadingPlayById by maimaiViewModel?.isLoadingPlayById?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(scoreEntry?.id, scoreEntry?.song?.id, scoreEntry?.difficultyLevel?.key) {

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
            maimaiViewModel?.fetchChartHistory(it, diffLevel?.value)
            maimaiViewModel?.fetchBestPerPlayer(it, diffLevel?.value)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = songName?.jp ?: "Détails du Score",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (songName?.en != null && songName.en != songName.jp) {
                            Text(
                                text = songName.en ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val artistJp = songArtist?.jp
                        val artistEn = songArtist?.en
                        Text(
                            text = artistJp ?: artistEn ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
            val diffLevel = scoreEntry.difficultyLevel
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
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, difficultyColor.copy(alpha = 0.25f), RoundedCornerShape(32.dp)),
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
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = formatPlayDate(scoreEntry.playDate),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Jacket
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            difficultyColor.copy(alpha = 0.25f),
                                            difficultyColor.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(1.dp, difficultyColor.copy(alpha = 0.2f), CircleShape)
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
                            textAlign = TextAlign.Center,
                            color = textColor
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (songName?.en != null && songName.en != songName.jp) {
                        Text(
                            text = songName.en ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(color = textSecondaryColor),
                            modifier = Modifier.padding(top = 4.dp),
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
                            textAlign = TextAlign.Center,
                            color = textSecondaryColor
                        )
                    )
                    
                    if (artistEn != null && artistEn != artistJp) {
                        Text(
                            text = artistEn,
                            style = MaterialTheme.typography.bodyMedium.copy(color = textSecondaryColor.copy(alpha = 0.5f)),
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
                                letterSpacing = (-2).sp,
                                color = textColor
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

                        val maxAchievement = scoreEntry.theoreticalMaxPercent?.let { "${it.format(2)}%" }
                            ?: scoreEntry.maxScoreFormattedFixed 
                            ?: scoreEntry.maxScore?.let { if (it <= 110.0) "${it.format(2)}%" else null }
                        if (!maxAchievement.isNullOrBlank()) {
                            val formattedMaxAch = if (maxAchievement.endsWith("%")) maxAchievement else "$maxAchievement%"
                            Surface(
                                color = difficultyColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(bottom = 14.dp, start = 8.dp)
                            ) {
                                Text(
                                    text = "MAX $formattedMaxAch",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = difficultyColor,
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    val currentScoreStr = scoreEntry.scoreFormattedFixed
                    val maxScoreStr = scoreEntry.theoreticalMaxScoreFormatted
                        ?: if (scoreEntry.maxScore != null && scoreEntry.maxScore!! > 110.0) scoreEntry.maxScoreFormattedFixed else null

                    if (currentScoreStr != null || maxScoreStr != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (currentScoreStr != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "SCORE ACTUEL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp,
                                                color = textSecondaryColor
                                            )
                                        )
                                        Text(
                                            text = currentScoreStr,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = textColor
                                            )
                                        )
                                    }
                                }

                                if (currentScoreStr != null && maxScoreStr != null) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = textSecondaryColor.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }

                                if (maxScoreStr != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "SCORE MAX POSSIBLE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp,
                                                color = textSecondaryColor
                                            )
                                        )
                                        Text(
                                            text = maxScoreStr,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = difficultyColor
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showScoreInfoSheet = true },
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Détails du calcul",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                    val columnHeaders = listOf(
                        "PERFECT" to Color(0xFFDCA632),
                        "GREAT" to Color(0xFFFF4081),
                        "GOOD" to Color(0xFF00E676),
                        "MISS" to Color(0xFFE57373)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                        columnHeaders.forEach { (title, color) ->
                            Surface(
                                color = color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = color
                                    ),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
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

    if (showScoreInfoSheet && scoreEntry != null) {
        MaimaiScoreInfoSheet(
            scoreEntry = scoreEntry,
            difficultyColor = difficultyColor,
            onDismissRequest = { showScoreInfoSheet = false }
        )
    }
}
}

