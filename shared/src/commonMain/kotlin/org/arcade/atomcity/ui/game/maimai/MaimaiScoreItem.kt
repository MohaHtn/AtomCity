package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.DifficultyRepository
import org.arcade.atomcity.data.LevelInfo
import org.arcade.atomcity.model.maitea.playsResponse.*
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.utils.formatPlayDate
import org.koin.compose.koinInject

@Composable
fun MaimaiScoreItem(
    play: MaiteaApiData,
    onClick: () -> Unit
) {
    val difficultyColor = getJacketBorderColor(play.difficultyLevel?.value)
    val difficultyRepository: DifficultyRepository = koinInject()
    
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }
    val scope = rememberCoroutineScope()

    // Getting level info at the start of the page.
    LaunchedEffect(play.song?.id, play.difficultyLevel?.key) {
        if (play.song?.id != null && play.difficultyLevel?.key != null) {
            levelInfo = difficultyRepository.getLevelByDifficulty(play.song!!.id!!.toString(), play.difficultyLevel!!.key!!.toString())
        }
    }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = getDifficultyColorBackground(play.difficultyLevel?.value),
        shape = RoundedCornerShape(24.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 340.dp
            
            Text(
                text = play.difficultyLevel?.label?.uppercase() ?: "",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isNarrow) 48.sp else 60.sp
                ),
                color = difficultyColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 20.dp)
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
                                .background(difficultyColor.copy(alpha = 0.15f), CircleShape)
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
                            color = Color(0xFFFFF9C4), // Pastel Yellow
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = if (isNarrow) (-2).dp else (-5).dp), // Position it above the jacket halo
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                //TODO: i18n
                                text = "Meilleur score",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isNarrow) 7.sp else 8.6.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFFFBC02D)
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
                            lineHeight = if (isNarrow) 18.sp else 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (songNameEn != null && songNameEn != songNameJp) {
                        Text(
                            text = songNameEn,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isNarrow) 9.sp else 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.alpha(0.5f)
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
                            fontSize = if (isNarrow) 11.sp else 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp).alpha(0.7f)
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
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    if (play.rank != null) {
                        Text(
                            text = play.rank!!,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 18.sp else 24.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = play.achievementFormatted?.replace("%", "") ?: "0.00",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 18.sp else 24.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )


                        Text(
                            text = "%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isNarrow) 10.sp else 12.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = formatPlayDate(play.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isNarrow) 9.sp else 11.sp),
                        modifier = Modifier.alpha(0.5f).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
