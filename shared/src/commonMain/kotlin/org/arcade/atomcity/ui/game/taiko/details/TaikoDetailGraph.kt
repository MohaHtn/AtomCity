package org.arcade.atomcity.ui.game.taiko.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.utils.formatPlayDate
import kotlin.math.roundToInt

@Composable
fun TaikoDetailGraph(
    title: String,
    history: List<TaikoServerHistoryEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFCF2C00)
) {
    if (history.size < 2) return

    val maxScore = remember(history) {
        val maxVal = history.maxOfOrNull { it.score ?: 0 }?.toFloat() ?: 1000000f
        val minVal = history.minOfOrNull { it.score ?: 0 }?.toFloat() ?: 0f
        val padding = if (maxVal == minVal) 10000f else (maxVal - minVal) * 0.1f
        maxVal + padding
    }
    
    val minScore = remember(history) {
        val maxVal = history.maxOfOrNull { it.score ?: 0 }?.toFloat() ?: 1000000f
        val minVal = history.minOfOrNull { it.score ?: 0 }?.toFloat() ?: 0f
        val padding = if (maxVal == minVal) 10000f else (maxVal - minVal) * 0.1f
        (minVal - padding).coerceAtLeast(0f)
    }

    var selectedIndex by remember(history) { mutableStateOf(history.lastIndex) }
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, lineColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = lineColor
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedIndex in history.indices) {
                val entry = history[selectedIndex]
                Surface(
                    color = lineColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, lineColor.copy(alpha = 0.25f)),
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
                                    text = formatPlayDate(entry.playTime.toString()),
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

                            if ((entry.comboCount ?: 0) > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MAX COMBO ${entry.comboCount}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = lineColor
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val scoreStr = entry.score.toString()
                            Text(
                                text = scoreStr,
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
                        .pointerInput(history) {
                            detectTapGestures { offset ->
                                val stepX = if (history.size > 1) size.width.toFloat() / (history.size - 1) else size.width.toFloat()
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, history.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        .pointerInput(history) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = if (history.size > 1) size.width.toFloat() / (history.size - 1) else size.width.toFloat()
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, history.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = if (history.size > 1) width / (history.size - 1) else width

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    history.forEachIndexed { index, entry ->
                        val x = index.toFloat() * stepX
                        val y = height - ((entry.score?.toFloat() ?: 0f) - minScore) / (maxScore - minScore) * height

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
                                    lineColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (selectedIndex in points.indices) {
                        val selectedPoint = points[selectedIndex]
                        drawLine(
                            color = lineColor.copy(alpha = 0.6f),
                            start = Offset(selectedPoint.x, 0f),
                            end = Offset(selectedPoint.x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }

                    points.forEachIndexed { index, point ->
                        if (index == selectedIndex) {
                            drawCircle(color = Color.White, center = point, radius = 9.dp.toPx())
                            drawCircle(color = lineColor, center = point, radius = 7.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                        } else {
                            drawCircle(color = Color.White, center = point, radius = 5.dp.toPx())
                            drawCircle(color = lineColor, center = point, radius = 3.5.dp.toPx())
                        }
                    }
                }
            }
        }
    }
}
