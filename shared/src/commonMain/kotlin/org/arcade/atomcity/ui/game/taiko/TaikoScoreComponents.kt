package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atomcity.shared.generated.resources.*
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.ui.game.taiko.settings.getScoreRankImageUrl
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TaikoScoreItem(
    score: TaikoServerHistoryEntry,
    onNavigateToRoute: (String) -> Unit,
    onFavoriteToggle: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                score.songId?.let { id ->
                    val diff = score.difficulty
                    val scoreVal = score.score
                    val time = score.playTime
                    val route = buildString {
                        append("taikoScoresDetails/$id")
                        val params = mutableListOf<String>()
                        if (diff != null) params.add("difficulty=$diff")
                        if (scoreVal != null) params.add("score=$scoreVal")
                        if (!time.isNullOrBlank()) {
                            val encodedTime = time.replace(" ", "T")
                            params.add("playTime=$encodedTime")
                        }
                        if (params.isNotEmpty()) {
                            append("?")
                            append(params.joinToString("&"))
                        }
                    }
                    onNavigateToRoute(route)
                }
            },
        colors = setDifficultyColorBackground(score.difficulty),
        shape = RoundedCornerShape(24.dp)
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
            IconButton(
                onClick = { score.songId?.let { onFavoriteToggle(it) } },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (score.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (score.isFavorite == true) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(start = 40.dp)
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
                        val genreInfo = getTaikoGenreInfo(score.genre)
                        if (genreInfo != null) {
                            TaikoGenreBadge(
                                genre = score.genre,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = displayDifficultyName(score.difficulty),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Text(
                            text = "★ ${score.stars ?: 0}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = score.score.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White
                        )

                        // Crown and Rank Badges Row below score
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            // Crown Badge
                            val crownUrl = getCrownImageUrl(score.crown)
                            val crownTitle = getCrownTitle(score.crown)
                            Surface(
                                color = Color.Black.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AsyncImage(
                                        model = crownUrl,
                                        contentDescription = crownTitle,
                                        modifier = Modifier.height(16.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = crownTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Rank Badge
                            val rankUrl = getScoreRankImageUrl(score.scoreRank)
                            if (rankUrl != null) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = rankUrl,
                                            contentDescription = "Rank",
                                            modifier = Modifier.height(16.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }

                        BoxWithConstraints {
                            if (maxWidth < 200.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    ScoreBadge("GOOD", score.goodCount, Color(0xFFFFD700))
                                    ScoreBadge("OK", score.okCount, Color(0xFFC0C0C0))
                                    ScoreBadge("MISS", score.missCount, Color(0xFFE57373))
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ScoreBadge("GOOD", score.goodCount, Color(0xFFFFD700))
                                    ScoreBadge("OK", score.okCount, Color(0xFFC0C0C0))
                                    ScoreBadge("MISS", score.missCount, Color(0xFFE57373))
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if ((score.comboCount ?: 0) > 0) {
                            Text(
                                text = "MAX COMBO ${score.comboCount}",
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
        color = Color.Black.copy(alpha = 0.35f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = count?.toString() ?: "0",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
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

data class TaikoGenreInfo(
    val id: Int,
    val name: String,
    val color: Color,
    val textColor: Color
)

fun getTaikoGenreInfo(genre: Int?): TaikoGenreInfo? {
    return when (genre) {
        0 -> TaikoGenreInfo(0, "Pop", Color(0xFF42C0D2), Color.Black)
        1 -> TaikoGenreInfo(1, "Anime", Color(0xFFFF90D3), Color.Black)
        2 -> TaikoGenreInfo(2, "Enfants", Color(0xFFFEC000), Color.Black)
        3 -> TaikoGenreInfo(3, "Vocaloid", Color(0xFFDDDDDD), Color.Black)
        4 -> TaikoGenreInfo(4, "Musique de jeu", Color(0xFFCC8AEA), Color.White)
        5 -> TaikoGenreInfo(5, "Original NAMCO", Color(0xFFFF7027), Color.White)
        6 -> TaikoGenreInfo(6, "Varieté", Color(0xFF1DC83B), Color.White)
        7 -> TaikoGenreInfo(7, "Classique", Color(0xFFBFA356), Color.White)
        else -> null
    }
}

@Composable
fun TaikoGenreBadge(
    genre: Int?,
    modifier: Modifier = Modifier
) {
    val genreInfo = getTaikoGenreInfo(genre) ?: return
    Surface(
        color = genreInfo.color,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = genreInfo.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = genreInfo.textColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun getCrownImageUrl(crown: Int?): String {
    val filename = when (crown) {
        3 -> "crown_Dondaful.webp"
        2 -> "crown_Gold.webp"
        1 -> "crown_Clear.webp"
        else -> "crown_None.webp"
    }
    return "https://taiko.farewell.dev/images/$filename"
}

fun getCrownTitle(crown: Int?): String {
    return when (crown) {
        3 -> "DONDERFUL"
        2 -> "FULL COMBO"
        1 -> "CLEAR"
        else -> "FAILED"
    }
}

