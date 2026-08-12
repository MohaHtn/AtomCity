package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                val daysToSubtract = currentDate.dayOfWeek.ordinal
                val startOfWeek = currentDate.minus(DatePeriod(days = daysToSubtract))
                startOfWeek.toString()
            }
            "month" -> "${currentDate.year}-${currentDate.month.number.toString().padStart(2, '0')}"
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
                "Semaine du ${startOfWeek.day} ${startOfWeek.month.toFrench()} au ${endOfWeek.day} ${endOfWeek.month.toFrench()}"
            }
            "month" -> "${currentDate.month.toFrench()} ${currentDate.year}"
            else -> "${currentDate.month.toFrench()} ${currentDate.year}"
        }
    }

    LaunchedEffect(isGlobal, selectedPeriod, apiDate) {
        maiteaViewModel.fetchMostPlayedCharts(isGlobal, selectedPeriod, apiDate)
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
            }

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
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Toutes les charts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(mostPlayedCharts) { entry ->
                        MostPlayedItem(entry, maxCount)
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
        label = { Text(label) }
    )
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = entry.songName ?: "",
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
fun MostPlayedItem(entry: MaimaiMostPlayedEntry, maxCount: Int) {
    val difficultyRepository: DifficultyRepository = koinInject()
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }

    val difficultyKey = remember(entry.difficulty) {
        when (entry.difficulty?.lowercase()) {
            "basic" -> 0
            "advanced" -> 1
            "expert" -> 2
            "master" -> 3
            "remaster" -> 4
            else -> null
        }
    }

    LaunchedEffect(entry.songJson?.id, difficultyKey) {
        if (entry.songJson?.id != null && difficultyKey != null) {
            levelInfo = difficultyRepository.getLevelByDifficulty(entry.songJson.id!!, difficultyKey)
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
                    text = entry.songName ?: "Inconnu",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.playCount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val fraction = if (maxCount > 0) entry.playCount.toFloat() / maxCount else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
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
