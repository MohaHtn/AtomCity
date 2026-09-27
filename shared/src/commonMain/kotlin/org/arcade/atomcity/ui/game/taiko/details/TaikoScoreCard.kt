package org.arcade.atomcity.ui.game.taiko.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.game.taiko.TaikoGenreBadge
import org.arcade.atomcity.ui.game.taiko.displayDifficultyName
import org.arcade.atomcity.ui.game.taiko.getDifficultyColor
import org.arcade.atomcity.ui.game.taiko.getDifficultyDrawable
import org.arcade.atomcity.ui.game.taiko.getTaikoGenreInfo
import org.arcade.atomcity.ui.game.taiko.settings.getRandomName
import org.arcade.atomcity.ui.game.taiko.settings.getScoreRankImageUrl
import org.arcade.atomcity.ui.game.taiko.settings.getSpeedName
import org.arcade.atomcity.ui.game.taiko.stats.RankPanelLegendCard
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.painterResource

data class ScoreRankBadgeInfo(
    val title: String,
    val imageUrl: String?,
    val containerColor: Color,
    val contentColor: Color
)

fun getScoreRankBadgeInfo(scoreRank: Int?): ScoreRankBadgeInfo? {
    val imageUrl = getScoreRankImageUrl(scoreRank) ?: return null
    return when (scoreRank) {
        8 -> ScoreRankBadgeInfo("KIWAMI (極)", imageUrl, Color(0xFFEDE7F6), Color(0xFF512DA8))
        7 -> ScoreRankBadgeInfo("MIYABI PURPLE (雅 紫)", imageUrl, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        6 -> ScoreRankBadgeInfo("MIYABI PINK (雅 桃)", imageUrl, Color(0xFFFCE4EC), Color(0xFFC2185B))
        5 -> ScoreRankBadgeInfo("MIYABI GOLD (雅 金)", imageUrl, Color(0xFFFFF8E1), Color(0xFFF57F17))
        4 -> ScoreRankBadgeInfo("IKI BLUE (粋 青)", imageUrl, Color(0xFFE1F5FE), Color(0xFF0288D1))
        3 -> ScoreRankBadgeInfo("IKI BRONZE (粋 銅)", imageUrl, Color(0xFFEFEBE9), Color(0xFF5D4037))
        1, 2 -> ScoreRankBadgeInfo("IKI WHITE (粋 白)", imageUrl, Color(0xFFFAFAFA), Color(0xFF616161))
        else -> null
    }
}

data class CrownBadgeInfo(
    val title: String,
    val imageUrl: String,
    val containerColor: Color,
    val contentColor: Color
)

fun getCrownBadgeInfo(crown: Int?): CrownBadgeInfo {
    return when (crown) {
        3 -> CrownBadgeInfo(
            title = "DONDERFUL COMBO",
            imageUrl = "https://taiko.farewell.dev/images/crown_Dondaful.webp",
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF2E7D32)
        )
        2 -> CrownBadgeInfo(
            title = "FULL COMBO",
            imageUrl = "https://taiko.farewell.dev/images/crown_Gold.webp",
            containerColor = Color(0xFFFFF8E1),
            contentColor = Color(0xFFF57F17)
        )
        1 -> CrownBadgeInfo(
            title = "CLEAR",
            imageUrl = "https://taiko.farewell.dev/images/crown_Clear.webp",
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF1565C0)
        )
        else -> CrownBadgeInfo(
            title = "FAILED",
            imageUrl = "https://taiko.farewell.dev/images/crown_None.webp",
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFC62828)
        )
    }
}

fun formatTaikoScore(score: Int?): String {
    if (score == null) return "N/A"
    return score.toString().reversed().chunked(3).joinToString(" ").reversed()
}

@Composable
fun TaikoBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
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
fun TaikoBadgeWithImage(
    imageUrl: String?,
    text: String,
    containerColor: Color,
    contentColor: Color,
    onInfoClick: (() -> Unit)? = null
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = text,
                    modifier = Modifier.height(20.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = contentColor
                ),
                maxLines = 1,
                softWrap = false
            )
            if (onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onInfoClick)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Paliers de score",
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaikoScoreBadgeRow(
    entry: TaikoServerHistoryEntry,
    isBestScore: Boolean = false,
    modifier: Modifier = Modifier
) {
    val difficultyColor = getDifficultyColor(entry.difficulty)
    val isLightBackground = difficultyColor.luminance() > 0.5f
    val topContentColor = if (isLightBackground) Color.Black else Color.White

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        if (isBestScore) {
            Surface(
                color = topContentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, topContentColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "MEILLEUR SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = topContentColor
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Left Play Setting Badges (Speed & Inverse if active)
        entry.playSetting?.let { setting ->
            setting.speed?.let { speedVal ->
                val speedStr = getSpeedName(speedVal)
                val speedImgUrl = "https://taiko.farewell.dev/images/Speed/${if (speedVal in 0..14) speedVal else 0}.png"
                TaikoBadgeWithImage(
                    imageUrl = speedImgUrl,
                    text = "VITESSE $speedStr",
                    containerColor = Color(0xFFEDE7F6),
                    contentColor = Color(0xFF512DA8)
                )
            }

            if (setting.isInverseOn == true) {
                TaikoBadgeWithImage(
                    imageUrl = "https://taiko.farewell.dev/images/Mirror.png",
                    text = "INVERSER",
                    containerColor = Color(0xFFFCE4EC),
                    contentColor = Color(0xFFC2185B)
                )
            }
        }
    }
}

@Composable
fun TaikoJudgmentsBreakdown(
    goodCount: Int?,
    okCount: Int?,
    missCount: Int?,
    modifier: Modifier = Modifier
) {
    val g = goodCount ?: 0
    val o = okCount ?: 0
    val m = missCount ?: 0
    val total = g + o + m
    val goodRate = if (total > 0) (g.toFloat() / total) * 100f else 0f

    val isDark = isAppInDarkTheme()
    val goodColor = if (isDark) Color(0xFFFFD700) else Color(0xFFC77700)
    val okColor = if (isDark) Color(0xFFC0C0C0) else Color(0xFF555555)
    val missColor = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DÉTAILS DES NOTES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                JudgmentItem("GOOD (良)", g, goodColor)
                JudgmentItem("OK (可)", o, okColor)
                JudgmentItem("MISS (不可)", m, missColor)
            }

            if (total > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Taux de Good (良)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goodRate.toDouble().format(2)}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = goodColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun JudgmentItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun TaikoMetricsRow(
    comboCount: Int?,
    drumrollCount: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (comboCount != null && comboCount > 0) {
            MetricCard(
                label = "MAX COMBO",
                value = "$comboCount",
                color = Color(0xFFF57F17),
                modifier = Modifier.weight(1f)
            )
        }
        if (drumrollCount != null) {
            MetricCard(
                label = "ROULEMENTS",
                value = "$drumrollCount",
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScoreCard(
    entry: TaikoServerHistoryEntry,
    isBestScore: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showScoreThresholdsSheet by remember { mutableStateOf(false) }

    val difficultyColor = getDifficultyColor(entry.difficulty)
    val isLightBackground = difficultyColor.luminance() > 0.5f
    val topContentColor = if (isLightBackground) Color.Black else Color.White

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = difficultyColor),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, difficultyColor.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Badges + Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    TaikoScoreBadgeRow(
                        entry = entry,
                        isBestScore = isBestScore,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = topContentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = formatPlayDate(entry.playTime.toString(), isUtc = false),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = topContentColor
                                ),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Right Play Setting Badges (Vanish & Random if active) under the date
                        entry.playSetting?.let { setting ->
                            if (setting.isVanishOn == true) {
                                TaikoBadgeWithImage(
                                    imageUrl = "https://taiko.farewell.dev/images/Doron.png",
                                    text = "DISPARITION",
                                    containerColor = Color(0xFFE0F7FA),
                                    contentColor = Color(0xFF00838F)
                                )
                            }

                            val randomVal = setting.randomType ?: 0
                            if (randomVal == 1 || randomVal == 2) {
                                val randomImg = if (randomVal == 2) "Random_Messy.png" else "Random_Whimsical.png"
                                val container = if (randomVal == 1) Color(0xFFFFF3E0) else Color(0xFFFFE0B2)
                                val content = if (randomVal == 1) Color(0xFFE65100) else Color(0xFFBF360C)
                                TaikoBadgeWithImage(
                                    imageUrl = "https://taiko.farewell.dev/images/$randomImg",
                                    text = getRandomName(randomVal).uppercase(),
                                    containerColor = container,
                                    contentColor = content
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Difficulty Image + Level Badge
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        topContentColor.copy(alpha = 0.2f),
                                        topContentColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    Image(
                        painter = painterResource(getDifficultyDrawable(entry.difficulty)),
                        contentDescription = displayDifficultyName(entry.difficulty),
                        modifier = Modifier
                            .size(130.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Rank Panel & Star Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = topContentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, topContentColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = displayDifficultyName(entry.difficulty),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = topContentColor
                            )
                            Text(
                                text = "★ ${entry.stars ?: 0}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Song Titles
                val primaryName = entry.musicNameEN.takeIf { !it.isNullOrBlank() } ?: entry.musicName ?: "Inconnu"
                val secondaryName = if (!entry.musicName.isNullOrBlank() && entry.musicName != primaryName) entry.musicName else null

                Text(
                    text = primaryName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = topContentColor
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (secondaryName != null) {
                    Text(
                        text = secondaryName,
                        style = MaterialTheme.typography.titleMedium.copy(color = topContentColor.copy(alpha = 0.8f)),
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (!entry.musicArtist.isNullOrBlank()) {
                    Text(
                        text = entry.musicArtist,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = topContentColor.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Main Score Display
                    Text(
                        text = formatTaikoScore(entry.score),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    val crownInfo = getCrownBadgeInfo(entry.crown)
                    val rankInfo = getScoreRankBadgeInfo(entry.scoreRank)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Crown Badge (Clear badge)
                        TaikoBadgeWithImage(
                            imageUrl = crownInfo.imageUrl,
                            text = crownInfo.title,
                            containerColor = crownInfo.containerColor,
                            contentColor = crownInfo.contentColor,
                            onInfoClick = if (rankInfo == null) { { showScoreThresholdsSheet = true } } else null
                        )

                        // Rank Badge (if present)
                        if (rankInfo != null) {
                            TaikoBadgeWithImage(
                                imageUrl = rankInfo.imageUrl,
                                text = rankInfo.title,
                                containerColor = rankInfo.containerColor,
                                contentColor = rankInfo.contentColor,
                                onInfoClick = { showScoreThresholdsSheet = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Hit Judgments Breakdown
                    TaikoJudgmentsBreakdown(
                        goodCount = entry.goodCount,
                        okCount = entry.okCount,
                        missCount = entry.missCount
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Row (Combo, Drumroll)
                    TaikoMetricsRow(
                        comboCount = entry.comboCount,
                        drumrollCount = entry.drumrollCount
                    )
                }
            }
        }
    }

    if (showScoreThresholdsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScoreThresholdsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Rangs de score",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                RankPanelLegendCard(showTitle = false)
            }
        }
    }
}
