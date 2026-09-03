package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.domain.model.LevelInfo
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.*
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.utils.formatPlayDate
import org.koin.compose.koinInject

import org.arcade.atomcity.ui.game.common.isAppInDarkTheme

@Composable
fun MaimaiScoreItemNight(
    play: ScorefetcherApiData,
    onClick: () -> Unit,
    footer: @Composable () -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    MaimaiScoreItem(
        play = play,
        onClick = onClick,
        isNightMode = true,
        footer = footer,
        overlay = overlay
    )
}

@Composable
fun MaimaiScoreItem(
    play: ScorefetcherApiData,
    onClick: () -> Unit,
    isNightMode: Boolean = isAppInDarkTheme(),
    footer: @Composable () -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val difficultyColor = getJacketBorderColor(play.difficultyLevel?.value)
    val difficultyRepository: IDifficultyRepository? = if (LocalInspectionMode.current) null else koinInject()
    
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }

    LaunchedEffect(play.song?.id, play.difficultyLevel?.key) {
        if (play.song?.id != null && play.difficultyLevel?.key != null) {
            levelInfo = difficultyRepository?.getLevelByDifficulty(
                songId = play.song!!.id!!, 
                diffIndex = play.difficultyLevel!!.key!!,
                songTitle = play.song?.name?.jp,
                altTitle = play.song?.name?.en
            )
        }
    }

    val cardColors = getDifficultyColorBackground(play.difficultyLevel?.value)

    val textColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = if (isNightMode) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isNightMode) Modifier.border(1.dp, difficultyColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                else Modifier
            ),
        onClick = onClick,
        colors = cardColors,
        shape = RoundedCornerShape(24.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 340.dp
            
            Text(
                text = play.difficultyLevel?.label?.uppercase() ?: "",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isNarrow) 36.sp else 48.sp
                ),
                color = difficultyColor.copy(alpha = if (isNightMode) 0.12f else 0.05f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 5.dp, y = 15.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isNarrow) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(if (isNarrow) 60.dp else 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(if (isNarrow) 56.dp else 64.dp)
                                .background(
                                    difficultyColor.copy(alpha = if (isNightMode) 0.25f else 0.15f),
                                    CircleShape
                                )
                                .then(
                                    if (isNightMode) Modifier.border(1.dp, difficultyColor.copy(alpha = 0.4f), CircleShape)
                                    else Modifier
                                )
                        )
                        AsyncImage(
                            model = play.jacketImageUrl,
                            contentDescription = "${play.song?.name} artwork",
                            modifier = Modifier
                                .size(if (isNarrow) 48.dp else 54.dp)
                                .clip(CircleShape)
                                .border(2.dp, difficultyColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Best score chip
                    if (play.isHighScore == true) {
                        Surface(
                            color = if (isNightMode) Color(0xFF332A00) else Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(6.dp),
                            border = if (isNightMode) BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = if (isNarrow) (-14).dp else (-18).dp),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = "Meilleur score",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isNarrow) 7.sp else 8.6.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isNightMode) Color(0xFFFFD700) else Color(0xFFFBC02D)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(if (isNarrow) 10.dp else 16.dp))

                // Song & Artist Info
                Column(modifier = Modifier.weight(1f)) {
                    val songNameJp = play.song?.name?.jp
                    val songNameEn = play.song?.name?.en
                    
                    Text(
                        text = songNameJp ?: songNameEn ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = if (isNarrow) 15.sp else 17.sp,
                            lineHeight = if (isNarrow) 18.sp else 20.sp,
                            color = textColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (songNameEn != null && songNameEn != songNameJp) {
                        Text(
                            text = songNameEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isNarrow) 9.sp else 11.sp,
                                color = textSecondaryColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Artist name, which is printed only if it is defined.
                    val artistJp = play.song?.artist?.jp
                    val artistEn = play.song?.artist?.en
                    val displayArtist = if (artistJp != null && artistEn != null && artistJp != artistEn) {
                        "$artistJp / $artistEn"
                    } else {
                        artistJp ?: artistEn ?: ""
                    }

                    Text(
                        text = displayArtist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = if (isNarrow) 11.sp else 13.sp,
                            color = textSecondaryColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(if (isNarrow) 4.dp else 6.dp))

                    // Difficulty / Level info chip.
                    MaimaiDifficultyBadge(
                        difficultyValue = play.difficultyLevel?.value,
                        levelInfo = levelInfo,
                        rating = play.ratingFormatted,
                        isCompact = isNarrow,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = if (isNarrow) 10.sp else 12.sp
                        )
                    )
                }

                // Achievement Column
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    footer()
                    if (play.rank != null) {
                        Text(
                            text = play.rank!!,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 18.sp else 24.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = play.achievementFormattedFixed?.replace("%", "") ?: "0.00",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 18.sp else 24.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
                        )

                        Text(
                            text = "%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isNarrow) 10.sp else 12.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    play.ratingFormatted?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 14.sp else 18.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    
                    Text(
                        text = formatPlayDate(play.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (isNarrow) 9.sp else 11.sp,
                            color = textSecondaryColor
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            overlay()
        }
    }
}
