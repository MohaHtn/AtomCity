package org.arcade.atomcity.ui.game.maimai.utage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import org.arcade.atomcity.ui.navigation.navigateIfNotCurrent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.maimai.UtageData
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.ui.game.maimai.MaimaiScoreItem
import org.arcade.atomcity.utils.format

@Composable
fun UtageExpressiveItem(
    item: UtageDisplayItem,
    navController: NavController,
    utageData: UtageData? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val attrInfo = remember(item.attribute, utageData) {
        findUtageAttributeInfo(item.attribute, utageData)
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        if (item.score != null) {
            val play = ScorefetcherApiData(
                id = item.score.playId,
                song = item.score.songJson,
                achievementFormatted = "${((item.score.achievement ?: 0.0) / 100.0).format(2)}%",
                rank = item.score.rank,
                difficultyLevel = item.score.difficultyLevelJson,
                rating = item.score.rating,
                playDate = item.score.playDate,
                jacketImageUrl = item.score.jacketImageUrl,
                isHighScore = false
            )

            MaimaiScoreItem(
                play = play,
                onClick = {
                    navController.navigateIfNotCurrent("maimaiScoresDetails/${play.id}")
                },
                footer = {
                    if (item.details != null) {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier
                                .size(28.dp)
                                .offset(y = (-14).dp, x = 4.dp)
                                .background(
                                    color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.Star else Icons.Default.Info,
                                contentDescription = "Guide",
                                modifier = Modifier.size(16.dp),
                                tint = if (expanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        } else {
            EmptyUtageCard(
                title = item.songTitle, 
                attribute = item.attribute,
                hasDetails = item.details != null,
                isExpanded = expanded,
                onExpandClick = { expanded = !expanded }
            )
        }

        AnimatedVisibility(
            visible = expanded && item.details != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (item.details != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp,
                        bottomStart = 24.dp
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .clickable { expanded = false }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (attrInfo?.img != null) {
                            AsyncImage(
                                model = attrInfo.img,
                                contentDescription = attrInfo.attribute,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(2.dp)
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = item.attribute ?: "宴",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val displayAttrTitle = attrInfo?.attribute ?: item.attribute
                            if (!displayAttrTitle.isNullOrBlank()) {
                                Text(
                                    text = displayAttrTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            val parsedComments = remember(item.comment) {
                                parseUtageComment(item.comment)
                            }

                            if (parsedComments.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp,
                                        bottomStart = 4.dp
                                    ),
                                    modifier = Modifier
                                        .padding(bottom = 10.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 4.dp)
                                                )
                                        )

                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            parsedComments.forEachIndexed { index, eraComment ->
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    if (eraComment.jp.isNotEmpty()) {
                                                        Text(
                                                            text = if (parsedComments.size > 1) "• ${eraComment.jp}" else eraComment.jp,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                lineHeight = 18.sp
                                                            ),
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    if (!eraComment.en.isNullOrBlank()) {
                                                        Text(
                                                            text = if (parsedComments.size > 1) "  ${eraComment.en}" else eraComment.en,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                lineHeight = 16.sp
                                                            ),
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                if (index < parsedComments.size - 1) {
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                                        thickness = 0.5.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = item.details,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.25.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            val parsedForcedOptions = remember(item.forcedOptions) {
                                parseForcedOptions(item.forcedOptions)
                            }

                            if (parsedForcedOptions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Options forcées :",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    parsedForcedOptions.forEach { opt ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "• ${opt.optionName} : ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = opt.value,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyUtageCard(
    title: String, 
    attribute: String?,
    hasDetails: Boolean = false,
    isExpanded: Boolean = false,
    onExpandClick: () -> Unit = {}
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(horizontal = 2.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
                drawRoundRect(
                    color = outlineColor,
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(outlineColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attribute ?: "宴",
                    style = MaterialTheme.typography.headlineSmall,
                    color = outlineColor,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "Score non disponible",
                    style = MaterialTheme.typography.labelMedium,
                    color = outlineColor
                )
            }

            if (hasDetails) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Star else Icons.Default.Info,
                        contentDescription = "Guide",
                        modifier = Modifier.size(16.dp),
                        tint = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
