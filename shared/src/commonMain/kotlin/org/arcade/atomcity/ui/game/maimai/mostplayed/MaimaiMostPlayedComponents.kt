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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeHashes.forEach { hash ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getColorForHash(hash))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
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
    }
}

@Composable
fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
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
    Color(0xFFFFCC80), Color(0xFFFFA726), Color(0xFFFF8A65),
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            topEntries.forEachIndexed { index, entry ->
                val isSelected = selectedIndex == index
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                selectedIndex = if (selectedIndex == index) -1 else index
                                PlatformUtils.hapticImpact()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (rankBg, rankText) = when (index) {
                        0 -> Color(0xFFFFD700) to "1er"
                        1 -> Color(0xFFC0C0C0) to "2e"
                        2 -> Color(0xFFCD7F32) to "3e"
                        else -> MaterialTheme.colorScheme.primaryContainer to "${index + 1}e"
                    }
                    val rankTextColor = when (index) {
                        0, 1, 2 -> Color.Black
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }

                    // Rank Pill / Play count badge
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else rankBg,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (isSelected) "${entry.playCount} essais" else rankText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else rankTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    val fraction = if (maxCount > 0) entry.playCount.toFloat() / maxCount else 0f
                    
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val distribution = entry.userPlayCounts
                        if (!distribution.isNullOrEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .fillMaxHeight(fraction.coerceAtLeast(0.12f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
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
                                    .fillMaxWidth(0.75f)
                                    .fillMaxHeight(fraction.coerceAtLeast(0.12f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = entry.songNameEn ?: entry.songName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
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

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.jacketImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${entry.playCount} essais",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (!entry.userPlayCounts.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entry.userPlayCounts.toList().sortedByDescending { it.second }.take(4).forEach { (hash, count) ->
                            Surface(
                                shape = CircleShape,
                                color = getColorForHash(hash).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(getColorForHash(hash))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = getColorForHash(hash)
                                    )
                                }
                            }
                        }
                    }
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
                        Row(modifier = Modifier.fillMaxSize()) {
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
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                if (!entry.difficulty.isNullOrBlank()) {
                    MaimaiDifficultyBadge(
                        difficultyValue = entry.difficulty,
                        levelInfo = levelInfo,
                        isCompact = true,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
