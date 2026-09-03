package org.arcade.atomcity.ui.game.taiko.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.formatPlayDate
import kotlin.math.roundToInt

@Composable
fun TaikoDetailGraph(
    title: String,
    history: List<TaikoServerHistoryEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFCF2C00)
) {
    val density = LocalDensity.current
    
    val maxScore = history.maxOfOrNull { it.score ?: 0 }?.toFloat() ?: 1000000f
    val minScore = history.minOfOrNull { it.score ?: 0 }?.toFloat() ?: 0f
    val scoreRange = (maxScore - minScore).coerceAtLeast(10000f)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipData by remember { mutableStateOf<Pair<TaikoServerHistoryEntry, Offset>?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(history) {
                            detectTapGestures { offset ->
                                val stepX = size.width.toFloat() / (history.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, history.size - 1)
                                val entry = history[index]
                                val x = index.toFloat() * stepX
                                val y = size.height.toFloat() - ((entry.score?.toFloat() ?: 0f) - minScore) / scoreRange * size.height.toFloat()
                                tooltipData = entry to Offset(x, y)
                                PlatformUtils.hapticImpact()
                            }
                        }
                        .pointerInput(history) {
                            detectHorizontalDragGestures { change, _ ->
                                val stepX = size.width.toFloat() / (history.size - 1).coerceAtLeast(1)
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, history.size - 1)
                                val entry = history[index]
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
                    val stepX = width / (history.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val points = mutableListOf<Offset>()

                    history.forEachIndexed { index, entry ->
                        val x = index * stepX
                        val y = height - ((entry.score?.toFloat() ?: 0f) - minScore) / scoreRange * height
                        val point = Offset(x, y)
                        points.add(point)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.forEach { point ->
                        drawCircle(Color.White, radius = 5.dp.toPx(), center = point)
                        drawCircle(lineColor, radius = 2.5.dp.toPx(), center = point)
                    }
                }

                tooltipData?.let { (entry, offset) ->
                    val xOffset = with(density) { (offset.x - 40.dp.toPx()).toInt() }
                    val yOffset = with(density) { (offset.y - 45.dp.toPx()).toInt() }
                    
                    Surface(
                        modifier = Modifier.offset {
                            IntOffset(
                                xOffset.coerceIn(0, (canvasSize.width - with(density) { 80.dp.toPx() }.toInt())),
                                yOffset.coerceAtLeast(0)
                            )
                        },
                        color = lineColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${entry.score}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                            Text(formatPlayDate(entry.playTime.toString()), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
