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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // State for the currently selected date
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val today = remember { Calendar.getInstance() }
    
    val isFutureDisabled = remember(calendar, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> {
                calendar.get(Calendar.YEAR) >= today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) >= today.get(Calendar.DAY_OF_YEAR)
            }
            "week" -> {
                calendar.get(Calendar.YEAR) >= today.get(Calendar.YEAR) &&
                calendar.get(Calendar.WEEK_OF_YEAR) >= today.get(Calendar.WEEK_OF_YEAR)
            }
            "month" -> {
                calendar.get(Calendar.YEAR) >= today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) >= today.get(Calendar.MONTH)
            }
            else -> false
        }
    }
    
    // Formatted dates for API
    val apiDate = remember(calendar, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            "week" -> {
                val year = calendar.get(Calendar.YEAR)
                val week = calendar.get(Calendar.WEEK_OF_YEAR)
                "$year-${week.toString().padStart(2, '0')}"
            }
            "month" -> SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            else -> SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        }
    }

    // Formatted date for Display
    val displayDate = remember(calendar, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(calendar.time)
            "week" -> {
                val cal = calendar.clone() as Calendar
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = SimpleDateFormat("dd MMM", Locale.getDefault()).format(cal.time)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                val end = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                
                val locale = Locale.getDefault()
                if (locale.language == "fr") {
                    "Semaine du $start au $end"
                } else {
                    "Week from $start to $end"
                }
            }
            "month" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                .replaceFirstChar { it.uppercase() }
            else -> ""
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
                PeriodChip(
                    label = "Jour",
                    selected = selectedPeriod == "day",
                    onClick = { selectedPeriod = "day" }
                )
                PeriodChip(
                    label = "Semaine",
                    selected = selectedPeriod == "week",
                    onClick = { selectedPeriod = "week" }
                )
                PeriodChip(
                    label = "Mois",
                    selected = selectedPeriod == "month",
                    onClick = { selectedPeriod = "month" }
                )
            }

            // Date Selection (Previous / Current / Next)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = calendar.clone() as Calendar
                    when (selectedPeriod) {
                        "day" -> newCal.add(Calendar.DAY_OF_YEAR, -1)
                        "week" -> newCal.add(Calendar.WEEK_OF_YEAR, -1)
                        "month" -> newCal.add(Calendar.MONTH, -1)
                    }
                    calendar = newCal
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

                IconButton(
                    onClick = {
                        val newCal = calendar.clone() as Calendar
                        when (selectedPeriod) {
                            "day" -> newCal.add(Calendar.DAY_OF_YEAR, 1)
                            "week" -> newCal.add(Calendar.WEEK_OF_YEAR, 1)
                            "month" -> newCal.add(Calendar.MONTH, 1)
                        }
                        calendar = newCal
                    },
                    enabled = !isFutureDisabled
                ) {
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
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rankText = when (index) {
                        0 -> "1er"
                        else -> "${index + 1}ème"
                    }
                    
                    Text(
                        text = rankText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))

                    val fraction = if (maxCount > 0) entry.playCount.toFloat() / maxCount else 0f
                    
                    // Bar container that takes all available space above the text
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
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${entry.playCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = entry.songName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MostPlayedItem(entry: MaimaiMostPlayedEntry, maxCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jacket Image
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
            
            // Bar Chart component (Simple horizontal bar)
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
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }
            
            val difficulty = entry.difficulty
            if (!difficulty.isNullOrBlank()) {
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
