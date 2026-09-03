package org.arcade.atomcity.ui.game.maimai.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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

@Composable
fun MaimaiScoreBadgeRow(scoreEntry: ScorefetcherApiData, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(rememberScrollState())
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
                contentColor = if (scoreEntry.fullCombo == 1) Color(0xFF1976D2) else Color(0xFFC99A2E)
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
