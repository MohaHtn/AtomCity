package org.arcade.atomcity.ui.game.maimai.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.formatPlayDate
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GraphPlaceholder(difficultyColor: Color, message: String? = null) {
    val defaultMessage = "Pour afficher les graphiques de statistiques, faites au moins 3 essais de cette chart !"
    val displayMessage = if (message.isNullOrEmpty()) defaultMessage else message

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
                text = displayMessage,
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    ),
                                    maxLines = 1,
                                    softWrap = false
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
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                softWrap = false
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

                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

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

    val (minAchievement, _, xRange) = remember(dataPoints) {
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    ),
                                    maxLines = 1,
                                    softWrap = false
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
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                softWrap = false
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

    if (pbHistory.size < 2) {
        Text(
            text = "PROGRESSION DES RECORDS PERSONNELS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = difficultyColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp)
        )
        GraphPlaceholder(
            difficultyColor,
            "Il faut au moins 2 PBs pour afficher ce graphique, vous n'en avez qu'un (pour l'instant) !"
        )
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!entry.rank.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.rank,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = difficultyColor
                                    ),
                                    maxLines = 1,
                                    softWrap = false
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
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${((entry.achievement ?: 0.0) / 100.0).format(2)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                softWrap = false
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

                    drawPath(
                        path = path,
                        color = difficultyColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

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
