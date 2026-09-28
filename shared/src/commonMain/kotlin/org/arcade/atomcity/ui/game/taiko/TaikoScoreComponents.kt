package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atomcity.shared.generated.resources.*
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.game.taiko.details.formatTaikoScore
import org.arcade.atomcity.ui.game.taiko.details.getCrownBadgeInfo
import org.arcade.atomcity.ui.game.taiko.details.getScoreRankBadgeInfo
import org.arcade.atomcity.ui.game.taiko.settings.getScoreRankImageUrl
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TaikoScoreItemNight(
    score: TaikoServerHistoryEntry,
    onNavigateToRoute: (String) -> Unit,
    onFavoriteToggle: (Int) -> Unit
) {
    TaikoScoreItem(
        score = score,
        onNavigateToRoute = onNavigateToRoute,
        onFavoriteToggle = onFavoriteToggle,
        isNightMode = true
    )
}

@Composable
fun TaikoScoreItem(
    score: TaikoServerHistoryEntry,
    onNavigateToRoute: (String) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    val difficultyColor = getDifficultyColor(score.difficulty)
    val textColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = if (isNightMode) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth()
            .border(
                1.dp,
                if (isNightMode) difficultyColor.copy(alpha = 0.40f) else difficultyColor.copy(alpha = 0.30f),
                RoundedCornerShape(20.dp)
            )
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
        colors = setDifficultyColorBackground(score.difficulty, isNightMode),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(getDifficultyDrawable(score.difficulty)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(90.dp),
                contentScale = ContentScale.Fit,
                alpha = if (isNightMode) 0.18f else 0.15f
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { score.songId?.let { onFavoriteToggle(it) } },
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (score.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (score.isFavorite == true) Color.Red else if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Title and Difficulty header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = score.musicName ?: "Morceau ${score.songId}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = textColor
                            )
                            if (!score.musicNameEN.isNullOrBlank() && score.musicNameEN != score.musicName) {
                                Text(
                                    text = score.musicNameEN,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (!score.musicArtist.isNullOrBlank()) {
                                Text(
                                    text = score.musicArtist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textSecondaryColor.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val genreInfo = getTaikoGenreInfo(score.genre)
                            if (genreInfo != null) {
                                TaikoGenreBadge(
                                    genre = score.genre,
                                    isNightMode = isNightMode,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = displayDifficultyName(score.difficulty),
                                style = MaterialTheme.typography.labelSmall,
                                color = difficultyColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Score, Badges, Combo and Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = formatTaikoScore(score.score),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = textColor
                            )

                            // Crown and Rank Badges Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                val crownUrl = getCrownImageUrl(score.crown)
                                val crownTitle = getCrownTitle(score.crown)
                                val crownInfo = getCrownBadgeInfo(score.crown, isNightMode)
                                val isFailedCrown = score.crown == 0 || score.crown == null

                                Surface(
                                    color = crownInfo.containerColor,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, crownInfo.borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = crownUrl,
                                            contentDescription = crownTitle,
                                            modifier = Modifier.height(14.dp),
                                            contentScale = ContentScale.Fit,
                                            colorFilter = if (isFailedCrown) ColorFilter.tint(if (isNightMode) Color.White else Color.Black) else null
                                        )
                                        Text(
                                            text = crownTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            color = crownInfo.contentColor
                                        )
                                    }
                                }

                                val rankUrl = getScoreRankImageUrl(score.scoreRank)
                                val rankInfo = getScoreRankBadgeInfo(score.scoreRank, isNightMode)
                                if (rankUrl != null && rankInfo != null) {
                                    Surface(
                                        color = rankInfo.containerColor,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, rankInfo.contentColor.copy(alpha = if (isNightMode) 0.4f else 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = rankUrl,
                                                contentDescription = "Rank",
                                                modifier = Modifier.height(14.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                color = difficultyColor.copy(alpha = if (isNightMode) 0.25f else 0.15f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "★ ${score.stars ?: 0}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (isNightMode) Color.White else difficultyColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = if (isNightMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = formatPlayDate(score.playTime.toString(), isUtc = false),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
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
        1 -> Color(0xFFD64A38) // Kantan (Facile) - Softened Red
        2 -> Color(0xFF7A9A3E) // Futsuu (Normal) - Softened Green
        3 -> Color(0xFF3F5927) // Muzukashii (Difficile) - Softened Forest Green
        4 -> Color(0xFFD4518A) // Oni (Démoniaque) - Softened Magenta
        5 -> Color(0xFF8B3BAF) // Ura Oni (Ultra) - Softened Purple
        else -> Color.Gray
    }
}

fun getDifficultyColorBackgroundLight(difficulty: Int?): Color {
    return when (difficulty) {
        1 -> Color(0xFFFFEBEE) // Kantan (Facile) - Soft Light Pastel Red
        2 -> Color(0xFFF1F8E9) // Futsuu (Normal) - Soft Light Pastel Green
        3 -> Color(0xFFE2EBE2) // Muzukashii (Difficile) - Soft Light Pastel Forest Green
        4 -> Color(0xFFFCE4EC) // Oni (Démoniaque) - Soft Light Pastel Magenta
        5 -> Color(0xFFF3E5F5) // Ura Oni (Ultra) - Soft Light Pastel Purple
        else -> Color(0xFFF5F5F5) // Soft Light Grey
    }
}

fun getDifficultyColorBackgroundDark(difficulty: Int?): Color {
    return when (difficulty) {
        1 -> Color(0xFF2C1215) // Kantan (Facile) - Dark Red
        2 -> Color(0xFF142616) // Futsuu (Normal) - Dark Green
        3 -> Color(0xFF0F1E11) // Muzukashii (Difficile) - Dark Forest Green
        4 -> Color(0xFF2A0E21) // Oni (Démoniaque) - Dark Magenta
        5 -> Color(0xFF220F2D) // Ura Oni (Ultra) - Dark Purple
        else -> Color(0xFF1B1B22) // Dark Neutral Surface
    }
}

@Composable
fun setDifficultyColorBackground(
    difficulty: Int?,
    isNightMode: Boolean = isAppInDarkTheme()
): CardColors {
    val containerColor = if (isNightMode) {
        getDifficultyColorBackgroundDark(difficulty)
    } else {
        getDifficultyColorBackgroundLight(difficulty)
    }
    val contentColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    return CardDefaults.elevatedCardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )
}

data class TaikoGenreInfo(
    val id: Int,
    val name: String,
    val color: Color,
    val textColor: Color
)

fun getTaikoGenreInfo(genre: Int?): TaikoGenreInfo? {
    return when (genre) {
        0 -> TaikoGenreInfo(0, "Pop", Color(0xFF42C0D2), Color.White)
        1 -> TaikoGenreInfo(1, "Anime", Color(0xFFFF90D3), Color.White)
        2 -> TaikoGenreInfo(2, "Enfants", Color(0xFFFEC000), Color.White)
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
    modifier: Modifier = Modifier,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    val genreInfo = getTaikoGenreInfo(genre) ?: return

    val badgeBg = if (isNightMode) {
        genreInfo.color.copy(alpha = 0.22f)
    } else {
        genreInfo.color
    }

    val textColor = if (isNightMode) {
        genreInfo.color
    } else {
        genreInfo.textColor
    }

    val border = if (isNightMode) {
        BorderStroke(1.dp, genreInfo.color.copy(alpha = 0.5f))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isNightMode) 0.dp else 1.dp,
        border = border,
        modifier = modifier
    ) {
        Text(
            text = genreInfo.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = textColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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

