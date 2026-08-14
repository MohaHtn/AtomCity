package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry
import org.arcade.atomcity.utils.PlatformUtils
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.arcade.atomcity.data.DifficultyRepository
import org.arcade.atomcity.data.LevelInfo
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MaimaiMostPlayedChart(
    onBackClick: () -> Unit,
    navController: NavHostController,
    maiteaViewModel: MaiteaViewModel,
) {
    val mostPlayedCharts by maiteaViewModel.mostPlayedCharts.collectAsState()
    val isLoading by maiteaViewModel.isLoadingMostPlayed.collectAsState()

    var isGlobal by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("month") }
    
    var currentDate by remember { 
        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) 
    }
    
    val apiDate = remember(currentDate, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> currentDate.toString() // yyyy-mm-dd
            "week" -> {
                val dayOfYear = currentDate.dayOfYear
                val dayOfWeek = currentDate.dayOfWeek.isoDayNumber // 1=Mon, 7=Sun
                val weekNumber = (dayOfYear - dayOfWeek + 10) / 7
                // Simplified ISO week number calculation
                val finalWeek = if (weekNumber < 1) 52 else if (weekNumber > 53) 1 else weekNumber
                val finalYear = when {
                    weekNumber < 1 -> currentDate.year - 1
                    weekNumber >= 52 && currentDate.month == Month.JANUARY -> currentDate.year - 1
                    weekNumber == 1 && currentDate.month == Month.DECEMBER -> currentDate.year + 1
                    else -> currentDate.year
                }
                "$finalYear-${finalWeek.toString().padStart(2, '0')}"
            }
            "month" -> "${currentDate.year}-${currentDate.month.number.toString().padStart(2, '0')}"
            "alltime" -> null
            else -> "${currentDate.year}-${currentDate.month.number.toString().padStart(2, '0')}"
        }
    }

    val displayDate = remember(currentDate, selectedPeriod) {
        fun Month.toFrench(): String = when (this) {
            Month.JANUARY -> "Janvier"
            Month.FEBRUARY -> "Février"
            Month.MARCH -> "Mars"
            Month.APRIL -> "Avril"
            Month.MAY -> "Mai"
            Month.JUNE -> "Juin"
            Month.JULY -> "Juillet"
            Month.AUGUST -> "Août"
            Month.SEPTEMBER -> "Septembre"
            Month.OCTOBER -> "Octobre"
            Month.NOVEMBER -> "Novembre"
            Month.DECEMBER -> "Décembre"
        }

        when (selectedPeriod) {
            "day" -> "${currentDate.day} ${currentDate.month.toFrench()} ${currentDate.year}"
            "week" -> {
                val daysToSubtract = currentDate.dayOfWeek.ordinal
                val startOfWeek = currentDate.minus(DatePeriod(days = daysToSubtract))
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                "Semaine du ${startOfWeek.day} ${startOfWeek.month.toFrench()} au ${endOfWeek.day} ${endOfWeek.month.toFrench()} ${endOfWeek.year}"
            }
            "month" -> "${currentDate.month.toFrench()} ${currentDate.year}"
            else -> "${currentDate.month.toFrench()} ${currentDate.year}"
        }
    }

    LaunchedEffect(isGlobal, selectedPeriod, apiDate) {
        maiteaViewModel.fetchMostPlayedCharts(isGlobal, selectedPeriod, apiDate, groupByHashkey = isGlobal)
    }

    LaunchedEffect(isGlobal) {
        if (isGlobal) {
            maiteaViewModel.fetchProfiles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charts les plus jouées") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Toggle Global / Personal
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = isGlobal,
                    onClick = { isGlobal = true },
                    label = { Text("Global") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = !isGlobal,
                    onClick = { isGlobal = false },
                    label = { Text("Personnel") }
                )
            }

            // Period Selection
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeriodChip(label = "Jour", selected = selectedPeriod == "day", onClick = { selectedPeriod = "day" })
                PeriodChip(label = "Semaine", selected = selectedPeriod == "week", onClick = { selectedPeriod = "week" })
                PeriodChip(label = "Mois", selected = selectedPeriod == "month", onClick = { selectedPeriod = "month" })
                PeriodChip(label = "Tout", selected = selectedPeriod == "alltime", onClick = { selectedPeriod = "alltime" })
            }

            if (selectedPeriod != "alltime") {
                // Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentDate = when (selectedPeriod) {
                            "day" -> currentDate.minus(DatePeriod(days = 1))
                            "week" -> currentDate.minus(DatePeriod(days = 7))
                            "month" -> currentDate.minus(DatePeriod(months = 1))
                            else -> currentDate.minus(DatePeriod(months = 1))
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Précédent")
                    }

                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        currentDate = when (selectedPeriod) {
                            "day" -> currentDate.plus(DatePeriod(days = 1))
                            "week" -> currentDate.plus(DatePeriod(days = 7))
                            "month" -> currentDate.plus(DatePeriod(months = 1))
                            else -> currentDate.plus(DatePeriod(months = 1))
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Suivant")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (mostPlayedCharts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aucune donnée disponible pour cette période",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxCount = mostPlayedCharts.maxOfOrNull { it.playCount } ?: 1
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            "Top 5",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        MostPlayedBarChart(mostPlayedCharts.take(5), maxCount)
                        
                        if (isGlobal) {
                            val profiles by maiteaViewModel.profiles.collectAsState()
                            UserLegend(profiles, mostPlayedCharts.take(5))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Toutes les charts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(mostPlayedCharts) { entry ->
                        MostPlayedItem(entry)
                    }
                }
            }
        }
    }
}

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

private val UserColors = listOf(
    Color(0xFFF06292), // Soft Pink
    Color(0xFFBA68C8), // Soft Purple
    Color(0xFF9575CD), // Soft Deep Purple
    Color(0xFF7986CB), // Soft Indigo
    Color(0xFF64B5F6), // Soft Blue
    Color(0xFF4FC3F7), // Soft Light Blue
    Color(0xFF4DD0E1), // Soft Cyan
    Color(0xFF4DB6AC), // Soft Teal
    Color(0xFF81C784), // Soft Green
    Color(0xFFAED581), // Soft Light Green
    Color(0xFFFFD54F), // Soft Amber
    Color(0xFFFFB74D)  // Soft Orange
)

private fun getColorForHash(hash: String): Color {
    val index = (hash.hashCode().let { if (it < 0) -it else it }) % UserColors.size
    return UserColors[index]
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
    val difficultyRepository: DifficultyRepository = koinInject()
    var levelInfo by remember { mutableStateOf(entry.levelInfo) }

    LaunchedEffect(entry.songName, entry.difficulty) {
        val diffIndex = getDifficultyIndex(entry.difficulty)
        if (diffIndex != -1 && entry.difficulty?.lowercase() != "utage") {
            levelInfo = difficultyRepository.getLevelByDifficulty(
                songId = entry.songJson?.id ?: -1,
                diffIndex = diffIndex + 2, // fuck, it works idc anymore
                songTitle = entry.songNameJp ?: entry.songName,
                altTitle = entry.songNameEn
            )
        }

        else {
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
