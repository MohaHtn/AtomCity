package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import atomcity.shared.generated.resources.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TaikoScoreItem(
    score: TaikoServerHistoryEntry,
    onNavigateToRoute: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                score.songId?.let { id ->
                    onNavigateToRoute("taikoScoresDetails/$id")
                }
            },
        colors = setDifficultyColorBackground(score.difficulty),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(getDifficultyDrawable(score.difficulty)),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .size(120.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.3f
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = score.musicName ?: "Song ${score.songId}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        if (!score.musicNameEN.isNullOrBlank() && score.musicNameEN != score.musicName) {
                            Text(
                                text = score.musicNameEN,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = score.musicArtist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${displayDifficultyName(score.difficulty)} (${score.stars ?: 0})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Text(
                            text = "★".repeat(score.stars ?: 0),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Yellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = score.score.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ScoreBadge("GOOD", score.goodCount, Color(0xFFFFD700))
                            ScoreBadge("OK", score.okCount, Color(0xFFC0C0C0))
                            ScoreBadge("MISS", score.missCount, Color(0xFFE57373))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if ((score.comboCount ?: 0) > 0) {
                            Text(
                                text = "COMBO ${score.comboCount}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = formatPlayDate(score.playTime.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBadge(label: String, count: Int?, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = count?.toString() ?: "0",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

fun displayDifficultyName(difficulty: Int?): String {
    return when (difficulty) {
        1 -> "Kantan (Facile)"
        2 -> "Futsuu (Normal)"
        3 -> "Muzukashii (Difficile)"
        4 -> "Oni (Démoniaque)"
        5 -> "Ura Oni (Ultra Démoniaque)"
        else -> "Inconnu"
    }
}

fun getDifficultyDrawable(difficulty: Int?): DrawableResource {
    return when (difficulty) {
        1 -> Res.drawable.taiko_difficulty_easy
        2 -> Res.drawable.taiko_difficulty_normal
        3 -> Res.drawable.taiko_difficulty_hard
        4 -> Res.drawable.taiko_difficulty_evil
        5 -> Res.drawable.taiko_difficulty_uraoni
        else -> Res.drawable.taiko_difficulty_easy
    }
}

fun getDifficultyColor(difficulty: Int?): Color {
    return when (difficulty) {
        1 -> Color(0xFFCF2C00)
        2 -> Color(0xFF657E25)
        3 -> Color(0xFF223004)
        4 -> Color(0xFFCE2D76)
        5 -> Color(0xFF6B1D8C)
        else -> Color.Gray
    }
}

@Composable
fun setDifficultyColorBackground(difficulty: Int?): CardColors {
    return CardDefaults.cardColors(containerColor = getDifficultyColor(difficulty))
}
