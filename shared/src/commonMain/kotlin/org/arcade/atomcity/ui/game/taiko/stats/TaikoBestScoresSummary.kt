package org.arcade.atomcity.ui.game.taiko.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.ui.game.taiko.getDifficultyColor
import org.arcade.atomcity.ui.game.taiko.getDifficultyDrawable
import org.arcade.atomcity.ui.game.taiko.settings.getScoreRankImageUrl
import org.arcade.atomcity.utils.getCurrentFormattedDate
import org.jetbrains.compose.resources.painterResource

@Composable
fun TaikoBestScoresSummary(
    playerName: String?,
    scores: List<TaikoServerHistoryEntry>,
    modifier: Modifier = Modifier,
    isCapture: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = playerName ?: "Joueur",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Taiko no Tatsujin · Meilleurs scores",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 30 scores (6 columns x 5 rows)
        val columns = 5
        val rows = 6

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (c in 0 until columns) {
                        val index = r * columns + c
                        if (index < scores.size) {
                            val score = scores[index]
                            Box(modifier = Modifier.weight(1f)) {
                                TaikoSummaryScoreItem(
                                    score = score,
                                    isCapture = isCapture,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Généré par Atom City, le ${getCurrentFormattedDate()}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.alpha(0.5f).align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun TaikoSummaryScoreItem(
    score: TaikoServerHistoryEntry,
    isCapture: Boolean = false,
) {
    val difficultyColor = getDifficultyColor(score.difficulty)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        Box {
            // Difficulty background
            Surface(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, difficultyColor, RoundedCornerShape(8.dp)),
                color = difficultyColor
            ) {
                Image(
                    painter = painterResource(getDifficultyDrawable(score.difficulty)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.5f),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "${score.stars ?: 0}★",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCapture) 12.sp else 7.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(topStart = 8.dp, bottomEnd = 4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "${score.comboCount ?: 0}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isCapture) 10.sp else 9.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "${score.score}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isCapture) 20.sp else 10.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        val songTitle = score.musicNameEN.takeIf { !it.isNullOrBlank() } ?: score.musicName ?: "Inconnu"

        Text(
            text = songTitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCapture) 13.sp else 10.sp
            ),
            color = difficultyColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        val rankUrl = getScoreRankImageUrl(score.scoreRank)
        if (rankUrl != null) {
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = rankUrl,
                        contentDescription = null,
                        modifier = Modifier.height(if (isCapture) 20.dp else 11.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}