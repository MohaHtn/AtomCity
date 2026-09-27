package org.arcade.atomcity.ui.game.taiko.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.datetime.*
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.getDifficultyColor
import org.arcade.atomcity.ui.game.taiko.getDifficultyDrawable
import org.arcade.atomcity.ui.game.taiko.setDifficultyColorBackground
import org.arcade.atomcity.utils.PlatformUtils
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TaikoMostPlayedChart(
    onBackClick: () -> Unit,
    navController: NavHostController,
    taikoViewModel: TaikoViewModel,
) {
    val mostPlayedCharts by taikoViewModel.mostPlayedCharts.collectAsState()
    val isLoading by taikoViewModel.isLoadingStats.collectAsState()

    var isGlobal by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("month") }

    val todayLocal = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var currentDate by remember { mutableStateOf(todayLocal) }
    var showDatePicker by remember { mutableStateOf(false) }

    val apiDate = remember(currentDate, selectedPeriod) {
        when (selectedPeriod) {
            "day" -> currentDate.toString() // yyyy-MM-dd
            "week" -> {
                val daysToSubtract = currentDate.dayOfWeek.ordinal
                val startOfWeek = currentDate.minus(DatePeriod(days = daysToSubtract))
                startOfWeek.toString()
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
        taikoViewModel.fetchMostPlayedCharts(isGlobal, selectedPeriod, apiDate)
    }

    LaunchedEffect(isGlobal) {
        if (isGlobal) {
            taikoViewModel.fetchCommunityScores()
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

            // DatePicker Dialog
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

            // Content
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
                                text = if (isGlobal) "Personne n'a encore joué pendant cette période." else "Jouez des musiques pour voir vos statistiques !",
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
                        TaikoMostPlayedBarChart(mostPlayedCharts.take(5), maxCount)

                        if (isGlobal) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TaikoUserLegend(mostPlayedCharts.take(5))
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
                        TaikoMostPlayedItem(
                            entry = entry,
                            isGlobal = isGlobal,
                            onClick = if (!isGlobal || entry.hasCurrentUserPlayed) {
                                {
                                    val route = if (entry.difficulty != null) {
                                        "taikoScoresDetails/${entry.songId}?difficulty=${entry.difficulty}"
                                    } else {
                                        "taikoScoresDetails/${entry.songId}"
                                    }
                                    navController.navigate(route)
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun CompactDifficultyBadge(
    difficulty: Int?,
    stars: Int? = null,
    modifier: Modifier = Modifier
) {
    if (difficulty == null || difficulty <= 0) return

    val label = when (difficulty) {
        1 -> "Kantan"
        2 -> "Futsuu"
        3 -> "Muzukashii"
        4 -> "Oni"
        5 -> "Ura Oni"
        else -> "Inconnu"
    }
    val color = getDifficultyColor(difficulty)
    val text = if (stars != null && stars > 0) "★ $stars" else label

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Image(
                painter = painterResource(getDifficultyDrawable(difficulty)),
                contentDescription = label,
                modifier = Modifier.size(10.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun TaikoMostPlayedBarChart(topEntries: List<TaikoMostPlayedEntry>, maxCount: Int) {
    var selectedIndex by remember { mutableStateOf(-1) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else rankBg,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (isSelected) "${entry.playCount} essai(s)" else rankText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else rankTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
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
                                distribution.toList().sortedByDescending { it.second }.forEach { (user, count) ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(count.toFloat())
                                            .background(getColorForUser(user))
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

                    Spacer(modifier = Modifier.height(4.dp))

                    val displayName = if (entry.musicName.isNullOrBlank()) "Morceau #${entry.songId}" else entry.musicName

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    if (entry.difficulty != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDifficultyBadge(difficulty = entry.difficulty, stars = entry.stars)
                    }
                }
            }
        }
    }
}

@Composable
fun TaikoUserLegend(entries: List<TaikoMostPlayedEntry>) {
    val activeUsers = entries.flatMap { it.userPlayCounts?.keys ?: emptySet() }.distinct()

    if (activeUsers.isNotEmpty()) {
        OutlinedCard(
            shape = RoundedCornerShape(24.dp),
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
                activeUsers.forEach { user ->
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
                                    .background(getColorForUser(user))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = user,
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
fun TaikoMostPlayedItem(
    entry: TaikoMostPlayedEntry,
    isGlobal: Boolean,
    onClick: (() -> Unit)? = null
) {
    val displayName = if (entry.musicName.isNullOrBlank()) "Morceau #${entry.songId}" else entry.musicName
    val displayArtist = entry.musicArtist ?: ""
    val hasDiff = entry.difficulty != null

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = if (hasDiff) setDifficultyColorBackground(entry.difficulty) else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (hasDiff) {
                Image(
                    painter = painterResource(getDifficultyDrawable(entry.difficulty)),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(80.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.22f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasDiff) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(getDifficultyDrawable(entry.difficulty)),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (entry.stars != null && entry.stars > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.35f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "★",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFFFFF),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${entry.stars}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hasDiff) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!entry.musicNameEN.isNullOrBlank() && entry.musicNameEN != displayName) {
                        Text(
                            text = entry.musicNameEN,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (hasDiff) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (displayArtist.isNotBlank() || (!entry.musicArtistEN.isNullOrBlank() && entry.musicArtistEN != displayArtist)) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (displayArtist.isNotBlank()) {
                        Text(
                            text = displayArtist,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasDiff) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!entry.musicArtistEN.isNullOrBlank() && entry.musicArtistEN != displayArtist) {
                        Text(
                            text = entry.musicArtistEN,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasDiff) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isGlobal && !entry.userPlayCounts.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            entry.userPlayCounts.toList().sortedByDescending { it.second }.forEach { (user, count) ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasDiff) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(getColorForUser(user))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$user ($count)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (hasDiff) Color.Black.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        val distribution = entry.userPlayCounts
                        if (!distribution.isNullOrEmpty()) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                distribution.toList().sortedByDescending { it.second }.forEach { (user, count) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(count.toFloat())
                                            .fillMaxHeight()
                                            .background(getColorForUser(user))
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (hasDiff) Color.White.copy(alpha = 0.85f)
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = if (hasDiff) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "${entry.playCount} essai(s)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (hasDiff) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private val UserColors = listOf(
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
    Color(0xFFC5E1A5), Color(0xFFAED581), Color(0xFF9CCC65)
)

private fun getColorForUser(userLabel: String): Color {
    var hash = 0xcbf29ce484222325uL
    userLabel.forEach { char ->
        hash = (hash xor char.code.toULong()) * 0x100000001b3uL
    }
    val index = (hash % UserColors.size.toULong()).toInt()
    return UserColors[index]
}
