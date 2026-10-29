package org.arcade.atomcity.ui.game.maimai

import MaimaiScoreItem
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.datetime.*
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.game.maimai.details.MaimaiScoreBadgeRow
import org.arcade.atomcity.ui.navigation.navigateIfNotCurrent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MaimaiPersonalBestPerSession(
    onBackClick: () -> Unit,
    navController: NavHostController,
    maimaiViewModel: MaimaiViewModel
) {
    val sessionPbsList by maimaiViewModel.sessionPbs.collectAsState()
    val isLoading by maimaiViewModel.isLoadingSessionPbs.collectAsState()
    val savedDate by maimaiViewModel.selectedSessionPbDate.collectAsState()

    val todayLocal = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var currentDate by remember { mutableStateOf(savedDate ?: todayLocal) }
    var isCalendarExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentDate) {
        maimaiViewModel.setSelectedSessionPbDate(currentDate)
    }

    LaunchedEffect(Unit) {
        maimaiViewModel.fetchPbsBySession(date = null)
    }

    val pbDates = remember(sessionPbsList) {
        sessionPbsList.filter { it.pbCount > 0 || it.pbs.isNotEmpty() }.map { it.sessionDate }.toSet()
    }

    val sessionPb = remember(sessionPbsList, currentDate) {
        sessionPbsList.firstOrNull { it.sessionDate.startsWith(currentDate.toString()) }
    }

    val pbCount = sessionPb?.pbCount ?: sessionPb?.pbs?.size ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Records personnels par jour",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (pbCount == 0) formatFrenchDate(currentDate) else "${formatFrenchDate(currentDate)} • $pbCount Record${if (pbCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        val pbs = sessionPb?.pbs ?: emptyList()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CalendarDropdown(
                expanded = isCalendarExpanded,
                onToggle = { isCalendarExpanded = !isCalendarExpanded },
                currentDate = currentDate,
                todayLocal = todayLocal,
                pbDates = pbDates,
                onDateSelected = { selectedDate ->
                    currentDate = selectedDate
                    isCalendarExpanded = false
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Main Content Area
            when {
                isLoading -> SessionPbsLoading(Modifier.fillMaxSize())
                pbs.isEmpty() -> SessionPbsEmpty(Modifier.fillMaxSize().padding(24.dp))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pbs) { score ->
                        val play = remember(score) { score.toScorefetcherApiData() }
                        MaimaiScoreItem(
                            play = play,
                            onClick = {
                                score.playId?.let { id ->
                                    navController.navigateIfNotCurrent("maimaiScoresDetails/$id")
                                }
                            },
                            footer = {
                                MaimaiScoreBadgeRow(
                                    scoreEntry = play,
                                    compact = true,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fixed header showing the selected day, which expands to the monthly calendar.
 */
@Composable
private fun CalendarDropdown(
    expanded: Boolean,
    onToggle: () -> Unit,
    currentDate: LocalDate,
    todayLocal: LocalDate,
    pbDates: Set<String>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "CalendarChevronRotation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = onToggle,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formatFrenchDate(currentDate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Masquer le calendrier" else "Afficher le calendrier",
                    modifier = Modifier.rotate(chevronRotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            MaimaiMonthlyCalendar(
                currentDate = currentDate,
                todayLocal = todayLocal,
                pbDates = pbDates,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SessionPbsLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement ...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionPbsEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucun record personnel trouvé.",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aucun nouveau record personnel n'a été réalisé ce jour.\nPeut-être le jour suivant, qui sait... !",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MaimaiMonthlyCalendar(
    currentDate: LocalDate,
    todayLocal: LocalDate,
    pbDates: Set<String>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedYear by remember(currentDate) { mutableStateOf(currentDate.year) }
    var displayedMonth by remember(currentDate) { mutableStateOf(currentDate.month) }

    LaunchedEffect(currentDate) {
        displayedYear = currentDate.year
        displayedMonth = currentDate.month
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Month Header with Navigation & Integrated "Today" Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (displayedMonth == Month.JANUARY) {
                            displayedMonth = Month.DECEMBER
                            displayedYear--
                        } else {
                            displayedMonth = Month.entries[displayedMonth.ordinal - 1]
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mois précédent",
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedContent(
                    targetState = currentDate != todayLocal,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { it / 4 } togetherWith fadeOut() + slideOutHorizontally { -it / 4 }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    label = "HeaderStateTransition"
                ) { showBtn ->
                    if (showBtn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${getMonthName(displayedMonth)} $displayedYear",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = { onDateSelected(todayLocal) },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.height(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Aujourd'hui",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${getMonthName(displayedMonth)} $displayedYear",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val isNextDisabled = displayedYear > todayLocal.year || (displayedYear == todayLocal.year && displayedMonth >= todayLocal.month)

                IconButton(
                    onClick = {
                        if (displayedMonth == Month.DECEMBER) {
                            displayedMonth = Month.JANUARY
                            displayedYear++
                        } else {
                            displayedMonth = Month.entries[displayedMonth.ordinal + 1]
                        }
                    },
                    enabled = !isNextDisabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Mois suivant",
                        modifier = Modifier.size(20.dp),
                        tint = if (!isNextDisabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Days Grid
            val firstDayOfMonth = LocalDate(displayedYear, displayedMonth, 1)
            val daysInMonth = when (displayedMonth) {
                Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
                Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                Month.FEBRUARY -> if (displayedYear % 4 == 0 && (displayedYear % 100 != 0 || displayedYear % 400 == 0)) 29 else 28
            }
            val paddingDays = firstDayOfMonth.dayOfWeek.ordinal

            val totalCells = paddingDays + daysInMonth
            val weeks = (totalCells + 6) / 7

            for (week in 0 until weeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (dayOfWeekIdx in 0 until 7) {
                        val cellIndex = week * 7 + dayOfWeekIdx
                        val dayNumber = cellIndex - paddingDays + 1

                        if (cellIndex >= paddingDays && dayNumber <= daysInMonth) {
                            val date = LocalDate(displayedYear, displayedMonth, dayNumber)
                            val dateString = date.toString()
                            val hasPb = pbDates.contains(dateString)
                            val isSelected = date == currentDate
                            val isToday = date == todayLocal
                            val isFuture = date > todayLocal

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = !isFuture) {
                                        onDateSelected(date)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                when {
                                                    hasPb -> if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                    else -> Color.Transparent
                                                }
                                            )
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

private fun getMonthName(month: Month): String {
    return when (month) {
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
}

private fun formatFrenchDate(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lundi"
        DayOfWeek.TUESDAY -> "Mardi"
        DayOfWeek.WEDNESDAY -> "Mercredi"
        DayOfWeek.THURSDAY -> "Jeudi"
        DayOfWeek.FRIDAY -> "Vendredi"
        DayOfWeek.SATURDAY -> "Samedi"
        DayOfWeek.SUNDAY -> "Dimanche"
    }
    val month = when (date.month) {
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
    return "$dayOfWeek ${date.day} $month ${date.year}"
}
