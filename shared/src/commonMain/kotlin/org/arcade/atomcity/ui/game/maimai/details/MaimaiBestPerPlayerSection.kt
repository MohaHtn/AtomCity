package org.arcade.atomcity.ui.game.maimai.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.formatPlayDate

@Composable
fun BestPerPlayerItem(b: BestPerPlayerResponse) {
    val difficultyColor = getJacketBorderColor(b.difficultyLevelJson?.value?.lowercase())
    val label = b.difficultyLevelJson?.label
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatPlayDate(b.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = b.playerName ?: "",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        val bDiffJson = b.difficultyLevelJson
                        Text(
                            text = if (label == "宴") {
                                bDiffJson.label?.let { "$it (Utage)" } ?: ""
                            } else {
                                "${bDiffJson?.label ?: ""} ${b.difficultyLevel}"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (label != "宴") {
                            val displayRating = b.rating?.format(2) ?: b.ratingFormatted ?: ""
                            if (displayRating.isNotEmpty()) {
                                Text(
                                    text = displayRating,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.alpha(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = b.rank ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "${((b.achievement ?: 0.0) / 100.0).format(2)}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
