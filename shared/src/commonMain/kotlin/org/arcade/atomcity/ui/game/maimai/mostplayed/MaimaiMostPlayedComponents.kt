package org.arcade.atomcity.ui.game.maimai.mostplayed

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry
import org.arcade.atomcity.domain.model.LevelInfo
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.ui.game.maimai.MaimaiDifficultyBadge
import org.arcade.atomcity.ui.game.maimai.getDifficultyIndex
import org.arcade.atomcity.utils.PlatformUtils
import org.koin.compose.koinInject

@Composable
fun UserLegend(profiles: Map<String, String>, entries: List<MaimaiMostPlayedEntry>) {
    val activeHashes = entries.flatMap { it.userPlayCounts?.keys ?: emptySet() }.distinct()
    
    if (activeHashes.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            activeHashes.forEach { hash ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getColorForHash(hash))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profiles[hash] ?: "Utilisateur",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

val UserColors = listOf(
    Color(0xFFEF9A9A), Color(0xFFE57373), Color(0xFFEF5350),
    Color(0xFFF48FB1), Color(0xFFF06292), Color(0xFFEC407A),
    Color(0xFFCE93D8), Color(0xFFBA68C8), Color(0xFFAB47BC),
    Color(0xFFB39DDB), Color(0xFF9575CD), Color(0xFF7E57C2),
    Color(0xFF9FA8DA), Color(0xFF7986CB), Color(0xFF5C6BC0),
    Color(0xFF90CAF9), Color(0xFF64B5F6), Color(0xFF42A5F5),
    Color(0xFF81D4FA), Color(0xFF4FC3F7), Color(0xFF29B6F6),
    Color(0xFF80DEEA), Color(0xFF4DD0E1), Color(0xFF26C6DA),
    Color(0xFF80CBC4), Color(0xFF4DB6AC), Color(0xFF26A69A),
    Color(0xFFA5D6A7), Color(0xFF81C784), Color(0xFF66BB6A),
    Color(0xFFC5E1A5), Color(0xFFAED581), Color(0xFF9CCC65),
    Color(0xFFE6EE9C), Color(0xFFDCE775), Color(0xFFD4E157),
    Color(0xFFFFF59D), Color(0xFFFFF176), Color(0xFFFFEE58),
    Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFFFCA28),
    Color(0xFFFFCC80), Color(0xFFFFB74D), Color(0xFFFFA726),
    Color(0xFFFFAB91), Color(0xFFFF8A65), Color(0xFFFF7043),
    Color(0xFFBCAAA4), Color(0xFFA1887F), Color(0xFF8D6E63),
    Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C)
)

fun getColorForHash(hash: String): Color {
    val h = stableHash64(hash)
    val index = (h % UserColors.size.toULong()).toInt()
    val baseColor = UserColors[index]
    
    val hueShift = (((h shr 8) and 0x0Fu).toInt() - 7) / 100f
    val satShift = (((h shr 12) and 0x0Fu).toInt() - 7) / 100f
    
    fun tweak(c: Float, shift: Float) = (c + shift).coerceIn(0.1f, 0.9f)
    
    return Color(
        red = tweak(baseColor.red, hueShift),
        green = tweak(baseColor.green, satShift),
        blue = tweak(baseColor.blue, -hueShift),
        alpha = 1f
    )
}

fun stableHash64(value: String): ULong {
    var hash = 0xcbf29ce484222325uL
    value.forEach { char ->
        hash = (hash xor char.code.toULong()) * 0x100000001b3uL
    }
    return hash
}

@Composable
fun MostPlayedBarChart(topEntries: List<MaimaiMostPlayedEntry>, maxCount: Int) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            topEntries.forEachIndexed { index, entry ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                selectedIndex = index
                                PlatformUtils.hapticImpact()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isSelected = selectedIndex == index
                    val rankText = when (index) {
                        0 -> "1er"
                        else -> "${index + 1}ème"
                    }
                    
                    Text(
                        text = if (isSelected) "${entry.playCount}" else rankText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (index == 0 || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))

                    val fraction = if (maxCount > 0) entry.playCount.toFloat() / maxCount else 0f
                    
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val distribution = entry.userPlayCounts
                        if (!distribution.isNullOrEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(fraction.coerceAtLeast(0.1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            ) {
                                distribution.toList().sortedByDescending { it.second }.forEach { (hash, count) ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(count.toFloat())
                                            .background(getColorForHash(hash))
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(fraction.coerceAtLeast(0.1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = entry.songNameJp ?: entry.songName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MostPlayedItem(entry: MaimaiMostPlayedEntry) {
    val difficultyRepository: IDifficultyRepository = koinInject()
    var levelInfo by remember { mutableStateOf(entry.levelInfo) }

    LaunchedEffect(entry.songName, entry.difficulty) {
        val diffIndex = getDifficultyIndex(entry.difficulty)
        if (diffIndex != -1 && entry.difficulty?.lowercase() != "utage") {
            levelInfo = difficultyRepository.getLevelByDifficulty(
                songId = entry.songJson?.id ?: -1,
                diffIndex = diffIndex + 2,
                songTitle = entry.songNameJp ?: entry.songName,
                altTitle = entry.songNameEn
            )
        } else {
            levelInfo = LevelInfo(level = "", internalLevel = "")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = entry.jacketImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.songNameEn ?: entry.songName ?: "Inconnu",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!entry.userPlayCounts.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        entry.userPlayCounts.toList().sortedByDescending { it.second }.take(4).forEach { (hash, count) ->
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = getColorForHash(hash)
                            )
                        }
                    }
                }
                
                Text(
                    text = "${entry.playCount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val distribution = entry.userPlayCounts
                
                if (!distribution.isNullOrEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        distribution.toList().sortedByDescending { it.second }.forEach { (hash, count) ->
                            Box(
                                modifier = Modifier
                                    .weight(count.toFloat())
                                    .fillMaxHeight()
                                    .background(getColorForHash(hash))
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            if (!entry.difficulty.isNullOrBlank()) {
                MaimaiDifficultyBadge(
                    difficultyValue = entry.difficulty,
                    levelInfo = levelInfo,
                    isCompact = true,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
