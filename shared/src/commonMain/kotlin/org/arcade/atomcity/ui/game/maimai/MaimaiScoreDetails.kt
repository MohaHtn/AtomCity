package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
    val isDark = isSystemInDarkTheme()
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
        val detail = scoreEntry.scoreDetail
        val tapCount = (detail?.tap?.perfect ?: 0) + (detail?.tap?.great ?: 0) +
                (detail?.tap?.good ?: 0) + (detail?.tap?.bad ?: 0)
        val holdCount = (detail?.hold?.perfect ?: 0) + (detail?.hold?.great ?: 0) +
                (detail?.hold?.good ?: 0) + (detail?.hold?.bad ?: 0)
        val slideCount = (detail?.slide?.perfect ?: 0) + (detail?.slide?.great ?: 0) +
                (detail?.slide?.good ?: 0) + (detail?.slide?.bad ?: 0)
        val breakCount = (detail?.breakk?.perfect ?: 0) + (detail?.breakk?.great ?: 0) +
                (detail?.breakk?.good ?: 0) + (detail?.breakk?.bad ?: 0)

        val tapPts = tapCount * 500
        val holdPts = holdCount * 1000
        val slidePts = slideCount * 1500
        val breakPts = (breakCount * 2500 * 1.04).toLong()
        val totalPts = tapPts + holdPts + slidePts + breakPts

        val basePts = (tapCount * 500) + (holdCount * 1000) + (slideCount * 1500) + (breakCount * 2500)

        val breakBonusPts = breakCount * 2500 * 0.04

        ModalBottomSheet(
            onDismissRequest = { showScoreInfoSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = difficultyColor
                    )
                    Text(
                        text = "Calcul du score et du pourcentage maximal possible",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Le calcul du score se fait de la manière suivante :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "($tapCount × 500) + ($holdCount × 1000) + ($slideCount × 1500) + ($breakCount × 2500 × 1,04)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DÉTAIL PAR TYPE DE NOTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val items = listOf(
                    "Taps ($tapCount)" to "$tapCount × 500 = ${formatScoreValue(tapPts.toDouble())}",
                    "Holds ($holdCount)" to "$holdCount × 1000 = ${formatScoreValue(holdPts.toDouble())}",
                    "Slides ($slideCount)" to "$slideCount × 1500 = ${formatScoreValue(slidePts.toDouble())}",
                    "Breaks ($breakCount)" to "$breakCount × 2500 × 1,04 = ${formatScoreValue(breakPts.toDouble())}"
                )

                items.forEach { (label, calc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = calc,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "maimai donne 4% de bonus sur chaque BREAK, ce qui donne 100 points lors que la note est tapée parfaitement (soit 2500 points), d'ou le 1.04.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))


                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score MAX Théorique",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = formatScoreValue(totalPts.toDouble()),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = difficultyColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CALCUL DU POURCENTAGE MAXIMAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Le pourcentage maximal est le score avec les 4% de bonus BREAK sur le score moins ces 4% de bonus BREAK, " +
                                    "ramené à 100, soit une différence de +${formatScoreValue(breakBonusPts)} points pour cette chart " +
                                    "($breakCount × 100 pts). \n\nOn enlève ensuite 0,0045%, afin d'éviter des décimales infinies pendant le calcul en pourcentage " +
                                    "(marge de troncature).",
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val maxPercent = scoreEntry.theoreticalMaxPercent

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "( ",
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Score avec bonus BREAK de 4% (${formatScoreValue(totalPts.toDouble())})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp).width(220.dp),
                                    thickness = 1.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "Score sans bonus BREAK de 4% (${formatScoreValue(basePts.toDouble())})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Text(
                                text = " × 100 )",
                                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }

                        Text(
                            text = "- 0,0045%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pourcentage Max Théorique",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "${maxPercent?.format(2) ?: "0.00"}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = difficultyColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatPlayDate(historyEntry.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatPlayDate(b.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
        if (scoreEntry.fullCombo != 0) {
            ScoreBadge(
                text = if (scoreEntry.fullCombo == 1) "FULL COMBO" else "FULL COMBO +",
                containerColor = if (scoreEntry.fullCombo == 1) Color(0xFFE3F2FD) else Color(0xFFFFF9C4),
                contentColor = if (scoreEntry.fullCombo == 1) Color(0xFF1976D2) else Color(
                    0xFFC99A2E
                )
            )
        }
        if (scoreEntry.isAllPerfect == true) {
            val maxScore = scoreEntry.theoreticalMaxScore ?: (if (scoreEntry.maxScore != null && scoreEntry.maxScore!! > 110.0) scoreEntry.maxScore else null)
            val isApPlus = when {
                scoreEntry.score != null && maxScore != null -> scoreEntry.score!! >= maxScore
                else -> (scoreEntry.scoreDetail?.breakk?.great ?: 0) == 0 &&
                        (scoreEntry.scoreDetail?.breakk?.good ?: 0) == 0 &&
                        (scoreEntry.scoreDetail?.breakk?.bad ?: 0) == 0
            }
            
            ScoreBadge(
                text = if (isApPlus) "ALL PERFECT +" else "ALL PERFECT",
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
            .padding(vertical = 6.dp),
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
        
        listOf(
            p to Color(0xFFDBA532),
            gr to Color(0xFFFF4081),
            gd to Color(0xFF00E676),
            m to Color(0xFFE57373)
        ).forEach { (count, color) ->
            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (count > 0) {
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
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
            .padding(vertical = 8.dp)
            .border(1.dp, difficultyColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Pour afficher les graphiques de statistiques, faites au moins 3 essais de cette chart !",
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

    var selectedIndex by remember(sortedHistory) { mutableStateOf(sortedHistory.lastIndex) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, difficultyColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "PROGRESSION DU SCORE SUR LE TEMPS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Systematic Top Tooltip
            if (selectedIndex in sortedHistory.indices) {
                val entry = sortedHistory[selectedIndex]
                Surface(
                    color = difficultyColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formatPlayDate(entry.playDate),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank!!,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val displayRating = entry.rating?.format(2) ?: entry.ratingFormatted ?: ""
                            if (displayRating.isNotEmpty() && entry.difficultyLevel != "宴" && entry.difficultyLevelJson?.value?.lowercase() != "utage") {
                                Text(
                                    text = "Rating $displayRating  •  ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(sortedHistory) {
                            detectTapGestures { offset ->
                                val stepX = if (sortedHistory.size > 1) size.width.toFloat() / (sortedHistory.size - 1) else size.width.toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        .pointerInput(sortedHistory) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = if (sortedHistory.size > 1) size.width.toFloat() / (sortedHistory.size - 1) else size.width.toFloat()
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, sortedHistory.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

                    // Draw Area Gradient Fill
                    if (points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    difficultyColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    // Draw Path
                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw vertical guide line for selected point
                    if (selectedIndex in points.indices) {
                        val selectedPoint = points[selectedIndex]
                        drawLine(
                            color = difficultyColor.copy(alpha = 0.6f),
                            start = Offset(selectedPoint.x, 0f),
                            end = Offset(selectedPoint.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    // Draw Points
                    points.forEachIndexed { index, point ->
                        if (index == selectedIndex) {
                            drawCircle(color = Color.White, center = point, radius = 9.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 7.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                        } else {
                            drawCircle(color = Color.White, center = point, radius = 5.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 3.5.dp.toPx())
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

    val (minAchievement, maxAchievement, xRange) = remember(dataPoints) {
        val min = dataPoints.minOf { it.achievement!! }
        val max = dataPoints.maxOf { it.achievement!! }
        val diff = max - min
        val padding = if (diff == 0.0) (if (max == 0.0) 100.0 else max * 0.1) else diff * 0.15
        val minAdj = (min - padding).coerceAtLeast(0.0)
        val maxAdj = max + padding
        val range = if (maxAdj == minAdj) 100.0 else (maxAdj - minAdj)
        Triple(minAdj, maxAdj, range)
    }

    val (minRating, maxRating, yRange) = remember(dataPoints) {
        val isUtage = dataPoints.any { it.difficultyLevel == "宴" || it.difficultyLevelJson?.value?.lowercase() == "utage" }
        if (isUtage) {
            Triple(0.0, 1.0, 1.0)
        } else {
            val min = dataPoints.minOf { it.rating!! }
            val max = dataPoints.maxOf { it.rating!! }
            val diff = max - min
            val padding = if (diff == 0.0) (if (max == 0.0) 1.0 else max * 0.1) else diff * 0.15
            val minAdj = (min - padding).coerceAtLeast(0.0)
            val maxAdj = max + padding
            val range = if (maxAdj == minAdj) 1.0 else (maxAdj - minAdj)
            Triple(minAdj, maxAdj, range)
        }
    }

    var selectedIndex by remember(dataPoints) { mutableStateOf(dataPoints.lastIndex) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, difficultyColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "PROGRESSION DU SCORE PARMI LES ESSAIS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Systematic Top Tooltip
            if (selectedIndex in dataPoints.indices) {
                val entry = dataPoints[selectedIndex]
                Surface(
                    color = difficultyColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formatPlayDate(entry.playDate),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val displayRating = entry.rating?.format(2) ?: entry.ratingFormatted ?: ""
                            if (displayRating.isNotEmpty() && entry.difficultyLevel != "宴" && entry.difficultyLevelJson?.value?.lowercase() != "utage") {
                                Text(
                                    text = "Rating $displayRating  •  ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val nearestIndex = dataPoints.indices.minByOrNull { i ->
                                    val x = ((dataPoints[i].achievement!! - minAchievement) / xRange * size.width).toFloat()
                                    abs(offset.x - x)
                                } ?: selectedIndex
                                if (selectedIndex != nearestIndex) {
                                    selectedIndex = nearestIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        .pointerInput(dataPoints) {
                            detectHorizontalDragGestures { change, _ ->
                                val nearestIndex = dataPoints.indices.minByOrNull { i ->
                                    val x = ((dataPoints[i].achievement!! - minAchievement) / xRange * size.width).toFloat()
                                    abs(change.position.x - x)
                                } ?: selectedIndex
                                if (selectedIndex != nearestIndex) {
                                    selectedIndex = nearestIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

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

                    val points = dataPoints.map { entry ->
                        val x = ((entry.achievement!! - minAchievement) / xRange * width).toFloat()
                        val y = (height - (((entry.rating ?: 0.0) - minRating) / yRange * height)).toFloat()
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    difficultyColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )

                        drawPath(
                            path = path,
                            color = difficultyColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw vertical guide line for selected point
                    if (selectedIndex in points.indices) {
                        val selectedPoint = points[selectedIndex]
                        drawLine(
                            color = difficultyColor.copy(alpha = 0.6f),
                            start = Offset(selectedPoint.x, 0f),
                            end = Offset(selectedPoint.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    // Draw Points
                    points.forEachIndexed { index, point ->
                        if (index == selectedIndex) {
                            drawCircle(color = Color.White, center = point, radius = 9.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 7.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                        } else {
                            drawCircle(color = Color.White, center = point, radius = 5.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 3.5.dp.toPx())
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

    if (pbHistory.size < 3) {
        GraphPlaceholder(difficultyColor)
        return
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

    var selectedIndex by remember(pbHistory) { mutableStateOf(pbHistory.lastIndex) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, difficultyColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "PROGRESSION DES RECORDS PERSONNELS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = difficultyColor
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Systematic Top Tooltip
            if (selectedIndex in pbHistory.indices) {
                val entry = pbHistory[selectedIndex]
                Surface(
                    color = difficultyColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formatPlayDate(entry.playDate),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val displayRating = entry.rating?.format(2) ?: entry.ratingFormatted ?: ""
                            if (displayRating.isNotEmpty() && entry.difficultyLevel != "宴" && entry.difficultyLevelJson?.value?.lowercase() != "utage") {
                                Text(
                                    text = "Rating $displayRating  •  ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(pbHistory) {
                            detectTapGestures { offset ->
                                val stepX = if (pbHistory.size > 1) size.width.toFloat() / (pbHistory.size - 1) else size.width.toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, pbHistory.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        .pointerInput(pbHistory) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = if (pbHistory.size > 1) size.width.toFloat() / (pbHistory.size - 1) else size.width.toFloat()
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, pbHistory.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

                    // Draw Area Gradient Fill
                    if (points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    difficultyColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    // Draw Path
                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw vertical guide line for selected point
                    if (selectedIndex in points.indices) {
                        val selectedPoint = points[selectedIndex]
                        drawLine(
                            color = difficultyColor.copy(alpha = 0.6f),
                            start = Offset(selectedPoint.x, 0f),
                            end = Offset(selectedPoint.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    // Draw Points
                    points.forEachIndexed { index, point ->
                        if (index == selectedIndex) {
                            drawCircle(color = Color.White, center = point, radius = 9.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 7.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                        } else {
                            drawCircle(color = Color.White, center = point, radius = 5.dp.toPx())
                            drawCircle(color = difficultyColor, center = point, radius = 3.5.dp.toPx())
                        }
                    }
                }
            }
        }
    }
}
