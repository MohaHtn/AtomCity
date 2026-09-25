package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.datetime.*
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.game.maimai.mostplayed.*
import org.arcade.atomcity.utils.PlatformUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MaimaiMostPlayedChart(
    onBackClick: () -> Unit,
    navController: NavHostController,
    maimaiViewModel: MaimaiViewModel,
) {
    val mostPlayedCharts by maimaiViewModel.mostPlayedCharts.collectAsState()
    val isLoading by maimaiViewModel.isLoadingMostPlayed.collectAsState()

    var isGlobal by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("month") }
    
    val todayLocal = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var currentDate by remember { mutableStateOf(todayLocal) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val apiDate = remember(currentDate, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> currentDate.toString() // yyyy-mm-dd
            "week" -> {
                val dayOfYear = currentDate.dayOfYear
                val dayOfWeek = currentDate.dayOfWeek.isoDayNumber // 1=Mon, 7=Sun
                val weekNumber = (dayOfYear - dayOfWeek + 10) / 7
                val finalWeek = if (weekNumber < 1) 52 else if (weekNumber > 53) 1 else weekNumber
                val finalYear = when {
                    weekNumber < 1 -> currentDate.year - 1
                    weekNumber >= 52 && currentDate.month == Month.JANUARY -> currentDate.year - 1
                    (weekNumber == 1 && currentDate.month == Month.DECEMBER) -> currentDate.year + 1
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
            Month.JANUARY -> "janvier"
            Month.FEBRUARY -> "février"
            Month.MARCH -> "mars"
            Month.APRIL -> "avril"
            Month.MAY -> "mai"
            Month.JUNE -> "juin"
            Month.JULY -> "juillet"
            Month.AUGUST -> "août"
            Month.SEPTEMBER -> "septembre"
            Month.OCTOBER -> "octobre"
            Month.NOVEMBER -> "novembre"
            Month.DECEMBER -> "décembre"
        }

        when (selectedPeriod) {
            "day" -> "${currentDate.day} ${currentDate.month.toFrench()} ${currentDate.year}"
            "week" -> {
                val daysToSubtract = currentDate.dayOfWeek.ordinal
                val startOfWeek = currentDate.minus(DatePeriod(days = daysToSubtract))
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                if (startOfWeek.month == endOfWeek.month) {
                    "Semaine du ${startOfWeek.day} au ${endOfWeek.day} ${endOfWeek.month.toFrench()} ${endOfWeek.year}"
                } else {
                    "Semaine du ${startOfWeek.day} ${startOfWeek.month.toFrench()} au ${endOfWeek.day} ${endOfWeek.month.toFrench()} ${endOfWeek.year}"
                }
            }
            "month" -> "${currentDate.month.toFrench()} ${currentDate.year}"
            else -> "${currentDate.month.toFrench()} ${currentDate.year}"
        }
    }

    LaunchedEffect(isGlobal, selectedPeriod, apiDate) {
        maimaiViewModel.fetchMostPlayedCharts(isGlobal, selectedPeriod, apiDate, groupByHashkey = isGlobal)
    }

    LaunchedEffect(isGlobal) {
        if (isGlobal) {
            maimaiViewModel.fetchProfiles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Charts les plus jouées",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isGlobal) "Statistiques globales" else "Mes statistiques",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Scope Switcher: Global vs Personnel
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = isGlobal,
                    onClick = {
                        isGlobal = true
                        PlatformUtils.hapticImpact()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Global", fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = !isGlobal,
                    onClick = {
                        isGlobal = false
                        PlatformUtils.hapticImpact()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Personnel", fontWeight = FontWeight.SemiBold)
                }
            }

            // Period Selector
            val periods = listOf(
                "day" to "Jour",
                "week" to "Semaine",
                "month" to "Mois",
                "alltime" to "Tout"
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                periods.forEachIndexed { index, (key, label) ->
                    SegmentedButton(
                        selected = selectedPeriod == key,
                        onClick = {
                            selectedPeriod = key
                            PlatformUtils.hapticImpact()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedPeriod == key) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Date Navigation Bar
            if (selectedPeriod != "alltime") {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                currentDate = when (selectedPeriod) {
                                    "day" -> currentDate.minus(DatePeriod(days = 1))
                                    "week" -> currentDate.minus(DatePeriod(days = 7))
                                    "month" -> currentDate.minus(DatePeriod(months = 1))
                                    else -> currentDate.minus(DatePeriod(months = 1))
                                }
                                PlatformUtils.hapticImpact()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Précédent")
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .then(
                                    if (selectedPeriod == "day") {
                                        Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                showDatePicker = true
                                                PlatformUtils.hapticImpact()
                                            }
                                    } else Modifier
                                ),
                            color = if (selectedPeriod == "day") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = displayDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (selectedPeriod == "day") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedPeriod == "day") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Sélectionner une date",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                currentDate = when (selectedPeriod) {
                                    "day" -> currentDate.plus(DatePeriod(days = 1))
                                    "week" -> currentDate.plus(DatePeriod(days = 7))
                                    "month" -> currentDate.plus(DatePeriod(months = 1))
                                    else -> currentDate.plus(DatePeriod(months = 1))
                                }
                                PlatformUtils.hapticImpact()
                            },
                            enabled = when (selectedPeriod) {
                                "day" -> currentDate < todayLocal
                                "week" -> {
                                    val currentStartOfWeek = currentDate.minus(DatePeriod(days = currentDate.dayOfWeek.ordinal))
                                    val todayStartOfWeek = todayLocal.minus(DatePeriod(days = todayLocal.dayOfWeek.ordinal))
                                    currentStartOfWeek < todayStartOfWeek
                                }
                                "month" -> currentDate.year < todayLocal.year || (currentDate.year == todayLocal.year && currentDate.month < todayLocal.month)
                                else -> false
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Suivant")
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cross-Platform iOS & Android DatePicker Dialog
            if (showDatePicker && selectedPeriod == "day") {
                val initialUtcMillis = remember(currentDate) {
                    currentDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                }
                val todayDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialUtcMillis,
                    selectableDates = remember(todayDate) {
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                                    .toLocalDateTime(TimeZone.UTC).date
                                return date <= todayDate
                            }
                        }
                    }
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { selectedMillis ->
                                    currentDate = Instant.fromEpochMilliseconds(selectedMillis)
                                        .toLocalDateTime(TimeZone.UTC).date
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Annuler")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Content Area
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chargement ...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (mostPlayedCharts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune donnée disponible",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aucune partie enregistrée pour cette période.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val maxCount = mostPlayedCharts.maxOfOrNull { it.playCount } ?: 1
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                "Top 5",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        MostPlayedBarChart(mostPlayedCharts.take(5), maxCount)
                        
                        if (isGlobal) {
                            val profiles by maimaiViewModel.profiles.collectAsState()
                            Spacer(modifier = Modifier.height(8.dp))
                            UserLegend(profiles, mostPlayedCharts.take(5))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Les 30 premières charts les plus jouées",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(mostPlayedCharts) { entry ->
                        MostPlayedItem(entry)
                    }
                }
            }
        }
    }
}
