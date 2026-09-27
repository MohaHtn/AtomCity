package org.arcade.atomcity.ui.game.taiko.dan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.taikoserver.dan.DanCourseData
import org.arcade.atomcity.data.remote.model.taikoserver.dan.DanOdaiBorder
import org.arcade.atomcity.data.remote.model.taikoserver.dan.DanOdaiSong
import org.arcade.atomcity.data.remote.model.taikoserver.dan.TaikoDanBestCourseData
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetails
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.getDifficultyDrawable
import org.arcade.atomcity.ui.game.taiko.setDifficultyColorBackground
import org.arcade.atomcity.utils.PlatformUtils
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import atomcity.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun TaikoDanScreen(
    onBackClick: () -> Unit,
    taikoViewModel: TaikoViewModel
) {
    val danCourses by taikoViewModel.danCourses.collectAsState()
    val userDanBestData by taikoViewModel.userDanBestData.collectAsState()
    val musicDetailsData by taikoViewModel.musicDetailsData.collectAsState()
    val isLoadingDan by taikoViewModel.isLoadingDan.collectAsState()

    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        taikoViewModel.fetchDanData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Dan Dojo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Épreuves de qualification",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoadingDan && danCourses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (danCourses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aucune donnée de Dan disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val coroutineScope = rememberCoroutineScope()
                val haptic = LocalHapticFeedback.current

                val pagerState = rememberPagerState(
                    initialPage = selectedIndex.coerceIn(danCourses.indices),
                    pageCount = { danCourses.size }
                )

                LaunchedEffect(pagerState.currentPage) {
                    if (selectedIndex != pagerState.currentPage) {
                        selectedIndex = pagerState.currentPage
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        PlatformUtils.hapticImpact()
                    }
                }

                // Course Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex.coerceIn(danCourses.indices),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    danCourses.forEachIndexed { index, course ->
                        val isSelected = index == selectedIndex
                        val courseBestData = userDanBestData?.danBestDataList?.find { it.danId == course.danId }
                        val clearState = courseBestData?.clearState ?: 0

                        Tab(
                            selected = isSelected,
                            onClick = {
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    PlatformUtils.hapticImpact()
                                }
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize().padding(2.dp)
                                        ) {
                                            AsyncImage(
                                                model = getDanClearStateImageUrl(clearState),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatDanTitle(course.title),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val currentCourse = danCourses[page]
                    val userBestCourse = userDanBestData?.danBestDataList?.find { it.danId == currentCourse.danId }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Section 1: Details
                        DanDetailsSection(
                            course = currentCourse,
                            bestData = userBestCourse
                        )

                        // Section 2: Musiques
                        DanSongsSection(
                            songs = currentCourse.aryOdaiSong,
                            musicDetailsData = musicDetailsData,
                            danId = currentCourse.danId
                        )

                        // Section 3: Conditions
                        DanConditionsSection(
                            borders = currentCourse.aryOdaiBorder,
                            bestData = userBestCourse
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DanDetailsSection(
    course: DanCourseData,
    bestData: TaikoDanBestCourseData?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Résultats
            val clearState = bestData?.clearState ?: 0
            val (statusText, statusColor) = when (clearState) {
                1 -> "Réussi (Rouge)" to Color(0xFFE53935)
                2 -> "Full Combo (Rouge)" to Color(0xFFE53935)
                3 -> "Parfait (Rouge)" to Color(0xFFE53935)
                4 -> "Réussi (Or)" to Color(0xFFFFB300)
                5 -> "Full Combo (Or)" to Color(0xFFFFB300)
                6 -> "Parfait (Or)" to Color(0xFFFFB300)
                else -> "Non validé" to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }

            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Résultats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        ) {
                            AsyncImage(
                                model = getDanClearStateImageUrl(clearState),
                                contentDescription = statusText,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Card 2 & 3: Score & Totaux
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Score Card
                val totalScore = bestData?.danBestStageDataList?.sumOf { it.highScore ?: 0L } ?: 0L

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatNumberWithSpaces(totalScore),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Totaux Card
                val totalGood = bestData?.danBestStageDataList?.sumOf { it.goodCount ?: 0 } ?: 0
                val totalOk = bestData?.danBestStageDataList?.sumOf { it.okCount ?: 0 } ?: 0
                val totalBad = bestData?.danBestStageDataList?.sumOf { it.badCount ?: 0 } ?: 0
                val totalRoll = bestData?.danBestStageDataList?.sumOf { it.drumrollCount ?: 0 } ?: 0
                val totalCombo = bestData?.comboCountTotal ?: 0
                val totalHits = bestData?.danBestStageDataList?.sumOf { it.totalHitCount ?: 0 } ?: 0

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Totaux",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricColumn("Good", totalGood)
                            MetricColumn("OK", totalOk)
                            MetricColumn("Bad", totalBad)
                            MetricColumn("Drumroll", totalRoll)
                            MetricColumn("Combo", totalCombo)
                            MetricColumn("Total", totalHits)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DanSongsSection(
    songs: List<DanOdaiSong>,
    musicDetailsData: Map<String, TaikoServerMusicDetails>?,
    danId: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Musiques",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        songs.forEachIndexed { index, song ->
            val detail = musicDetailsData?.get(song.songNo.toString())

            val diffLevel = (song.level ?: 4).coerceIn(1, 5)
            val starCount = when (diffLevel) {
                1 -> detail?.starEasy
                2 -> detail?.starNormal
                3 -> detail?.starHard
                4 -> detail?.starOni
                5 -> detail?.starUra
                else -> detail?.starOni
            } ?: 0

            val mainTitle = detail?.songName?.takeIf { it.isNotBlank() } ?: "Morceau #${song.songNo}"
            val englishTitle = detail?.songNameEN?.takeIf { it.isNotBlank() && it != mainTitle }
            val mainArtist = detail?.artistName?.takeIf { it.isNotBlank() } ?: ""
            val englishArtist = detail?.artistNameEN?.takeIf { it.isNotBlank() && it != mainArtist }

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = setDifficultyColorBackground(diffLevel),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(getDifficultyDrawable(diffLevel)),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(100.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.25f
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.width(36.dp)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(getDifficultyDrawable(diffLevel)),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "★",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "$starCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mainTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!englishTitle.isNullOrBlank()) {
                                Text(
                                    text = englishTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (mainArtist.isNotBlank()) {
                                Text(
                                    text = mainArtist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!englishArtist.isNullOrBlank()) {
                                Text(
                                    text = englishArtist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DanConditionsSection(
    borders: List<DanOdaiBorder>,
    bestData: TaikoDanBestCourseData?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Conditions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        borders.forEach { border ->
            val conditionName = getOdaiTypeName(border.odaiType)
            val isPercent = border.odaiType == 1

            val userValue = when (border.odaiType) {
                1 -> bestData?.soulGaugeTotal ?: 0
                2 -> bestData?.danBestStageDataList?.sumOf { it.goodCount ?: 0 } ?: 0
                3 -> bestData?.danBestStageDataList?.sumOf { it.okCount ?: 0 } ?: 0
                4 -> bestData?.danBestStageDataList?.sumOf { it.badCount ?: 0 } ?: 0
                5 -> bestData?.danBestStageDataList?.sumOf { it.highScore ?: 0L }?.toInt() ?: 0
                6 -> bestData?.danBestStageDataList?.sumOf { it.drumrollCount ?: 0 } ?: 0
                7 -> bestData?.comboCountTotal ?: 0
                8 -> bestData?.danBestStageDataList?.sumOf { it.totalHitCount ?: 0 } ?: 0
                else -> 0
            }

            val hasAttempted = bestData != null
            val progressFraction = if (border.goldBorderTotal > 0) {
                (userValue.toFloat() / border.goldBorderTotal).coerceIn(0f, 1f)
            } else 0f

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = conditionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Card pour résultats
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Résultats",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (hasAttempted) (if (isPercent) "$userValue%" else "$userValue") else "N/A",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { if (hasAttempted) progressFraction else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        }
                    }

                    // Card pour conditions
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Conditions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val symbol = if (border.borderType == 2 || border.odaiType == 4) "<" else ">"
                                val redVal = if (isPercent) "${border.redBorderTotal}%" else "${border.redBorderTotal}"
                                val goldVal = if (isPercent) "${border.goldBorderTotal}%" else "${border.goldBorderTotal}"

                                Column {
                                    Text(
                                        text = "Red Clear",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$symbol $redVal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Gold Clear",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$symbol $goldVal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
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
private fun MitsudomoeIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        val innerRadius = radius * 0.42f

        // Draw outer ring
        drawCircle(
            color = tint,
            radius = radius * 0.95f,
            style = Stroke(width = radius * 0.1f)
        )

        // Draw 3 tomoe swirl commas rotated by 120 degrees
        for (i in 0..2) {
            rotate(degrees = i * 120f, pivot = center) {
                val headCenter = Offset(center.x, center.y - innerRadius * 0.5f)
                drawCircle(
                    color = tint,
                    radius = innerRadius * 0.45f,
                    center = headCenter
                )
                val path = Path().apply {
                    moveTo(headCenter.x - innerRadius * 0.45f, headCenter.y)
                    cubicTo(
                        headCenter.x - innerRadius * 0.2f, center.y + innerRadius * 0.6f,
                        center.x + innerRadius * 0.5f, center.y + innerRadius * 0.2f,
                        center.x, center.y
                    )
                    cubicTo(
                        center.x - innerRadius * 0.2f, center.y - innerRadius * 0.2f,
                        headCenter.x + innerRadius * 0.45f, headCenter.y + innerRadius * 0.2f,
                        headCenter.x + innerRadius * 0.45f, headCenter.y
                    )
                    close()
                }
                drawPath(path = path, color = tint)
            }
        }
    }
}



fun getDanClearStateImageUrl(clearState: Int?): String {
    val filename = when (clearState) {
        1 -> "dani_RedNormalClear.webp"
        2 -> "dani_RedFullComboClear.webp"
        3 -> "dani_RedPerfectClear.webp"
        4 -> "dani_GoldNormalClear.webp"
        5 -> "dani_GoldFullComboClear.webp"
        6 -> "dani_GoldPerfectClear.webp"
        else -> "dani_NotClear.webp"
    }
    return "https://taiko.farewell.dev/images/$filename"
}

fun formatDanTitle(rawTitle: String): String {
    val lower = rawTitle.lowercase()
    return when {
        lower.endsWith("kyuu") -> {
            val num = lower.removeSuffix("kyuu")
            val intNum = num.toIntOrNull() ?: 1
            if (intNum == 1) "1ER KYUU" else "${intNum}ÈME KYUU"
        }
        lower.endsWith("dan") -> {
            val num = lower.removeSuffix("dan")
            val intNum = num.toIntOrNull() ?: 1
            when (intNum) {
                in 1..10 -> {
                    if (intNum == 1) "1ER DAN" else "${intNum}ÈME DAN"
                }
                11 -> "KUROTO"
                12 -> "MEIJIN"
                13 -> "CHOJIN"
                14 -> "TATSUJIN"
                else -> "${intNum}ÈME DAN"
            }
        }
        else -> rawTitle.uppercase()
    }
}

fun getOdaiTypeName(type: Int): String {
    return when (type) {
        1 -> "Jauge d'âme"
        2 -> "Good"
        3 -> "OK"
        4 -> "Bad"
        5 -> "Score"
        6 -> "Drumroll"
        7 -> "Combo MAX"
        8 -> "Total"
        else -> "Condition #$type"
    }
}

fun formatNumberWithSpaces(number: Long): String {
    val str = number.toString()
    val sb = StringBuilder()
    for (i in str.indices) {
        if (i > 0 && (str.length - i) % 3 == 0) {
            sb.append(' ')
        }
        sb.append(str[i])
    }
    return sb.toString()
}
