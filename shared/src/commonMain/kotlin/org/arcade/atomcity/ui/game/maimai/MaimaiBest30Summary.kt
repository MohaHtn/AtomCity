package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.ui.game.common.rememberMaimaiLevel
import org.arcade.atomcity.ui.game.common.getDifficultyIndex
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.arcade.atomcity.utils.format
import androidx.compose.ui.draw.alpha
import org.arcade.atomcity.data.DifficultyRepository
import org.arcade.atomcity.utils.getCurrentFormattedDate

@Composable
fun MaimaiBest30Summary(
    playerName: String?,
    rating: Int?,
    scores: List<PlayerBest30Response>,
    iconUrl: String? = null,
    bannerUrl: String? = null,
    title: String? = null,
    modifier: Modifier = Modifier,
    repository: DifficultyRepository,
    isCapture: Boolean = false
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner
        if (bannerUrl != null) {
            AsyncImage(
                model = bannerUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillWidth
            )
            Spacer(modifier = Modifier.height(15.dp))
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (iconUrl != null) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = playerName ?: "Joueur",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontSize = if (isCapture) 16.sp else 14.sp
                            )
                        )
                    }
                    Text(
                        text = "maimai FiNALE · 30 meilleurs scores",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RATING",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                MaimaiRatingBadge(
                    rating = (rating ?: 0),
                    fontSize = 16.sp
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
                                SummaryScoreItem(
                                    score,
                                    repository = repository,
                                    isCapture = isCapture
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
fun SummaryScoreItem(
    score: PlayerBest30Response,
    repository: DifficultyRepository,
    isCapture: Boolean = false
) {
    val difficultyColor = getJacketBorderColor(score.difficultyLevelJson?.value)
    val levelValue = rememberMaimaiLevel(
        songId = score.songJson?.id ?: -1,
        diffIndex = score.difficultyLevelJson?.key ?: getDifficultyIndex(score.difficultyLevelJson?.value),
        songTitle = score.songJson?.name?.jp,
        repository = repository
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            AsyncImage(
                model = score.jacketImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, difficultyColor, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "${score.difficultyLevelJson?.label ?: ""} $levelValue",
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
                        difficultyColor,
                        RoundedCornerShape(topStart = 8.dp, bottomEnd = 4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            if (score.rating != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = score.rating.format(2),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isCapture) 10.sp else 9.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        Text(
            text = "${((score.achievement ?: 0.0) / 100.0).format(2)}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isCapture) 24.sp else 10.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Text (
            text = score.songJson?.name?.en ?: "Titre inconnu",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCapture) 13.sp else 10.sp
            ),
            color = difficultyColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        if (score.rank != null) {
            Text(
                text = score.rank,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = difficultyColor,
                    fontSize = if (isCapture) 24.sp else 10.sp
                )
            )
        }
    }
}

