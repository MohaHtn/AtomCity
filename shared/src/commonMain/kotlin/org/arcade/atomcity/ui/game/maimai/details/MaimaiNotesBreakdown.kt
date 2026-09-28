package org.arcade.atomcity.ui.game.maimai.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme

@Composable
fun MaimaiScoreBadgeRow(
    scoreEntry: ScorefetcherApiData,
    modifier: Modifier = Modifier,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        if (scoreEntry.isHighScore == true) {
            val container = if (isNightMode) Color(0xFF332A00) else Color(0xFFFFF9C4)
            val content = if (isNightMode) Color(0xFFFFD700) else Color(0xFFFBC02D)
            ScoreBadge(
                text = "MEILLEUR SCORE",
                containerColor = container,
                contentColor = content,
                isNightMode = isNightMode
            )
        }
        if (scoreEntry.fullCombo != 0 && scoreEntry.isAllPerfect != true) {
            val isFc1 = scoreEntry.fullCombo == 1
            val container = if (isNightMode) {
                if (isFc1) Color(0xFF1565C0).copy(alpha = 0.45f) else Color(0xFF332A00)
            } else {
                if (isFc1) Color(0xFFE3F2FD) else Color(0xFFFFF9C4)
            }
            val content = if (isNightMode) {
                if (isFc1) Color(0xFF90CAF9) else Color(0xFFFFD54F)
            } else {
                if (isFc1) Color(0xFF1976D2) else Color(0xFFC99A2E)
            }
            ScoreBadge(
                text = if (isFc1) "FULL COMBO" else "FULL COMBO +",
                containerColor = container,
                contentColor = content,
                isNightMode = isNightMode
            )
        }
        if (scoreEntry.isAllPerfect == true) {
            val maxScore = scoreEntry.theoreticalMaxScore ?: (if (scoreEntry.maxScore != null && scoreEntry.maxScore!! > 110.0) scoreEntry.maxScore else null)
            val isApPlus = when {
                scoreEntry.fullComboLabel == "AP+" -> true
                scoreEntry.fullComboLabel == "AP" -> false
                scoreEntry.score != null && maxScore != null -> scoreEntry.score!! >= maxScore
                else -> {
                    val maxPct = scoreEntry.theoreticalMaxPercent
                    val achPct = scoreEntry.achievement?.let { it.toDouble() / 100.0 }
                    if (maxPct != null && achPct != null && achPct > 0.0) {
                        achPct >= (maxPct - 0.005)
                    } else {
                        false
                    }
                }
            }
            val container = if (isNightMode) Color(0xFF00897B).copy(alpha = 0.45f) else Color(0xFFE0F2F1)
            val content = if (isNightMode) Color(0xFF80CBC4) else Color(0xFF00897B)
            
            ScoreBadge(
                text = if (isApPlus) "ALL PERFECT +" else "ALL PERFECT",
                containerColor = container,
                contentColor = content,
                isNightMode = isNightMode
            )
        }
        if (scoreEntry.isTrackSkip == true) {
            val container = if (isNightMode) Color(0xFFE53935).copy(alpha = 0.45f) else Color(0xFFFFEBEE)
            val content = if (isNightMode) Color(0xFFEF9A9A) else Color(0xFFE53935)
            ScoreBadge(
                text = "TRACK SKIP",
                containerColor = container,
                contentColor = content,
                isNightMode = isNightMode
            )
        }
    }
}

@Composable
fun ScoreBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isNightMode) 0.dp else 2.dp,
        border = BorderStroke(1.dp, if (isNightMode) contentColor.copy(alpha = 0.4f) else contentColor.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = contentColor
            ),
            maxLines = 1,
            softWrap = false,
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
