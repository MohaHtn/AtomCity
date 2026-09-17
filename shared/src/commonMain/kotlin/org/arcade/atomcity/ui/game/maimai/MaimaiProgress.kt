package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.remote.model.scorefetcher.PlayerRankProgression
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.format
import org.koin.compose.koinInject
import kotlin.math.roundToInt

enum class RankSortOption(val label: String) {
    RANKS("Rangs (SSS+ → A)"),
    RATING("Rating"),
    CLEARED("Clears"),
    SSS_PLUS("SSS+"),
    SSS("SSS"),
    PLAYED("Parties")
}

fun comparePlayersByRanks(p1: PlayerRankProgression, p2: PlayerRankProgression): Int {
    val r1 = p1.getNormalizedRankCounts()
    val r2 = p2.getNormalizedRankCounts()

    val rankKeys = listOf("SSS+", "SSS", "SS+", "SS", "S+", "S", "AAA", "AA", "A")
    for (key in rankKeys) {
        val count1 = r1[key] ?: when (key) {
            "SSS+" -> p1.sssPlus ?: 0
            "SSS" -> p1.sss ?: 0
            "SS+" -> p1.ssPlus ?: 0
            "SS" -> p1.ss ?: 0
            "S+" -> p1.sPlus ?: 0
            "S" -> p1.s ?: 0
            "AAA" -> p1.aaa ?: 0
            "AA" -> p1.aa ?: 0
            "A" -> p1.a ?: 0
            else -> 0
        }
        val count2 = r2[key] ?: when (key) {
            "SSS+" -> p2.sssPlus ?: 0
            "SSS" -> p2.sss ?: 0
            "SS+" -> p2.ssPlus ?: 0
            "SS" -> p2.ss ?: 0
            "S+" -> p2.sPlus ?: 0
            "S" -> p2.s ?: 0
            "AAA" -> p2.aaa ?: 0
            "AA" -> p2.aa ?: 0
            "A" -> p2.a ?: 0
            else -> 0
        }
        if (count1 != count2) {
            return count2.compareTo(count1)
        }
    }
    val rating1 = p1.rating ?: 0
    val rating2 = p2.rating ?: 0
    if (rating1 != rating2) {
        return rating2.compareTo(rating1)
    }
    return p2.getPlayedCount().compareTo(p1.getPlayedCount())
}

fun getRankBadgeColor(rank: String): Color {
    return when (rank.uppercase().trim()) {
        "SSS+", "SSS PLUS" -> Color(0xFFFFB300)
        "SSS" -> Color(0xFFFDD835)
        "SS+", "SS PLUS" -> Color(0xFF00ACC1)
        "SS" -> Color(0xFF039BE5)
        "S+", "S PLUS" -> Color(0xFFAB47BC)
        "S" -> Color(0xFF8E24AA)
        "AAA" -> Color(0xFF00897B)
        "AA" -> Color(0xFF43A047)
        "A" -> Color(0xFF1E88E5)
        "AUTRES", "OTHER", "OTHERS" -> Color(0xFF757575)
        "FC", "FC+" -> Color(0xFFFB8C00)
        "AP", "AP+" -> Color(0xFFE53935)
        else -> Color(0xFF757575)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiProgress(
    viewModel: MaimaiViewModel,
    onBackClick: () -> Unit,
    onNavigateToDetails: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val rankProgressionState by viewModel.rankProgression.collectAsState()
    val isLoading by viewModel.isLoadingRankProgression.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(RankSortOption.RANKS) }

    var selectedRankTarget by remember { mutableStateOf<Pair<PlayerRankProgression, String>?>(null) }
    val rankCharts by viewModel.rankCharts.collectAsState()
    val isLoadingRankCharts by viewModel.isLoadingRankCharts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchRankProgression()
    }

    val totalGameCharts = rankProgressionState?.totalGameCharts ?: 0
    val allPlayers = remember(rankProgressionState) {
        rankProgressionState?.getAllPlayers() ?: emptyList()
    }

    val filteredAndSortedPlayers = remember(allPlayers, searchQuery, selectedSortOption) {
        allPlayers
            .filter { player ->
                searchQuery.isBlank() || player.getDisplayName().contains(searchQuery, ignoreCase = true)
            }
            .sortedWith { p1, p2 ->
                when (selectedSortOption) {
                    RankSortOption.RANKS -> comparePlayersByRanks(p1, p2)
                    RankSortOption.RATING -> (p2.rating ?: 0).compareTo(p1.rating ?: 0)
                    RankSortOption.CLEARED -> p2.getClearedCount().compareTo(p1.getClearedCount())
                    RankSortOption.SSS_PLUS -> {
                        val s1 = p1.getNormalizedRankCounts()["SSS+"] ?: p1.sssPlus ?: 0
                        val s2 = p2.getNormalizedRankCounts()["SSS+"] ?: p2.sssPlus ?: 0
                        if (s1 != s2) s2.compareTo(s1) else comparePlayersByRanks(p1, p2)
                    }
                    RankSortOption.SSS -> {
                        val s1 = p1.getNormalizedRankCounts()["SSS"] ?: p1.sss ?: 0
                        val s2 = p2.getNormalizedRankCounts()["SSS"] ?: p2.sss ?: 0
                        if (s1 != s2) s2.compareTo(s1) else comparePlayersByRanks(p1, p2)
                    }
                    RankSortOption.PLAYED -> p2.getPlayedCount().compareTo(p1.getPlayedCount())
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Progression des Joueurs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
                actions = {
                    IconButton(onClick = { viewModel.fetchRankProgression() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Sort Option Chips
            /*Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Trier par :",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RankSortOption.entries.toTypedArray()) { option ->
                        FilterChip(
                            selected = selectedSortOption == option,
                            onClick = { selectedSortOption = option },
                            label = { Text(option.label) }
                        )
                    }
                }
            }*/

            // Progression Sharing Visibility Toggle Card
            val apiKeyManager = koinInject<ApiKeyManager>()
            val maimaiApiKey by apiKeyManager.getApiKeyFlow("maimai").collectAsState(initial = null)
            val isUpdatingVisibility by viewModel.isUpdatingVisibility.collectAsState()

            val userKeyHash = remember(maimaiApiKey) {
                maimaiApiKey?.trim()?.takeIf { it.isNotBlank() }?.let { PlatformUtils.sha256(it) }
            }
            val currentPlayer = remember(rankProgressionState, allPlayers, userKeyHash) {
                rankProgressionState?.player ?: allPlayers.find { it.keyHash == userKeyHash }
            }

            if (!maimaiApiKey.isNullOrBlank() || currentPlayer != null) {
                val isPublic = currentPlayer?.isPublicEffective ?: true
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Partager ma progression",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPublic) "Votre progression est visible par les autres joueurs" else "Votre progression est masquée",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isUpdatingVisibility) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Switch(
                                checked = isPublic,
                                enabled = !isUpdatingVisibility,
                                onCheckedChange = { checked ->
                                    viewModel.updateProgressionVisibility(checked, userKeyHash)
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredAndSortedPlayers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Aucun joueur trouvé pour \"$searchQuery\"" else "Aucune donnée de progression disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSortedPlayers, key = { player -> player.keyHash ?: player.getDisplayName() }) { player ->
                        val isCurrentUser = remember(player.keyHash, userKeyHash) {
                            userKeyHash == null || player.keyHash == userKeyHash
                        }
                        PlayerProgressionCard(
                            player = player,
                            isCurrentUser = isCurrentUser,
                            totalGameCharts = totalGameCharts,
                            totalSongs = rankProgressionState?.totalSongs ?: 0,
                            playedSongs = rankProgressionState?.playedSongs ?: 0,
                            onRankClick = { p, rankLabel ->
                                if (isCurrentUser) {
                                    selectedRankTarget = p to rankLabel
                                    viewModel.fetchChartsForRank(p.keyHash, rankLabel)
                                }
                            }
                        )
                    }
                }
            }
        }

        selectedRankTarget?.let { (player, rankLabel) ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            val minChartPct = remember(rankCharts) {
                rankCharts.minOfOrNull { (it.achievement ?: 0.0) / 100.0 } ?: 0.0
            }
            val maxChartPct = remember(rankCharts) {
                rankCharts.maxOfOrNull { (it.achievement ?: 0.0) / 100.0 } ?: 101.0
            }

            var rawMinStep = (minChartPct * 100).roundToInt()
            var rawMaxStep = (maxChartPct * 100).roundToInt()
            if (rawMinStep >= rawMaxStep) {
                rawMinStep = (rawMinStep - 50).coerceAtLeast(0)
                rawMaxStep = rawMinStep + 100
            }

            val minStep = rawMinStep
            val maxStep = rawMaxStep

            var currentStep by remember(rankCharts) { mutableIntStateOf(maxStep) }
            var isUserDraggingSlider by remember { mutableStateOf(false) }

            val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
            LaunchedEffect(firstVisibleIndex, rankCharts) {
                if (!isUserDraggingSlider && firstVisibleIndex in rankCharts.indices) {
                    val visibleAch = ((rankCharts[firstVisibleIndex].achievement ?: 0.0) / 100.0)
                    currentStep = (visibleAch * 100).roundToInt().coerceIn(minStep, maxStep)
                }
            }

            fun onSliderChange(newStep: Int) {
                currentStep = newStep
                val targetPct = newStep / 100.0
                val targetIndex = rankCharts.indexOfFirst { ((it.achievement ?: 0.0) / 100.0) <= targetPct }
                    .takeIf { it >= 0 } ?: (rankCharts.size - 1)
                if (targetIndex in rankCharts.indices) {
                    coroutineScope.launch {
                        listState.scrollToItem(targetIndex)
                    }
                }
            }

            ModalBottomSheet(
                onDismissRequest = { selectedRankTarget = null },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Charts $rankLabel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${player.getDisplayName()} • ${if (isLoadingRankCharts) "Chargement..." else "${rankCharts.size} chart(s)"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val badgeColor = getRankBadgeColor(rankLabel)
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = rankLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    if (isLoadingRankCharts) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (rankCharts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune chart trouvée pour le rang $rankLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                items(rankCharts, key = { it.playId ?: it.hashCode() }) { result ->
                                    val play = ScorefetcherApiData(
                                        id = result.playId,
                                        song = result.songJson,
                                        achievementFormatted = "${((result.achievement ?: 0.0) / 100.0).format(2)}%",
                                        rank = result.rank,
                                        difficultyLevel = result.difficultyLevelJson,
                                        rating = result.rating,
                                        playDate = result.playDate,
                                        jacketImageUrl = result.jacketImageUrl,
                                        isHighScore = false
                                    )

                                    MaimaiScoreItem(
                                        play = play,
                                        onClick = {
                                            if (play.id != null && play.id!! > 0 && onNavigateToDetails != null) {
                                                selectedRankTarget = null
                                                onNavigateToDetails(play.id!!)
                                            }
                                        }
                                    )
                                }
                            }

                            SideAchievementSlider(
                                minStep = minStep,
                                maxStep = maxStep,
                                currentStep = currentStep,
                                onStepChange = { newStep -> onSliderChange(newStep) },
                                onDraggingChange = { dragging -> isUserDraggingSlider = dragging },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SideAchievementSlider(
    minStep: Int,
    maxStep: Int,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onDraggingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(isDragging) {
        onDraggingChange(isDragging)
    }

    val range = (maxStep - minStep).coerceAtLeast(1)
    val fraction = ((currentStep - minStep).toFloat() / range.toFloat()).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(100.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        val totalHeight = maxHeight
        val topMargin = 12.dp
        val bottomMargin = 16.dp
        val thumbHeight = 28.dp

        val trackHeight = (totalHeight - topMargin - bottomMargin).coerceAtLeast(0.dp)
        val travelDistance = (trackHeight - thumbHeight).coerceAtLeast(0.dp)
        val thumbYOffset = -(bottomMargin + travelDistance * fraction)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(minStep, maxStep) {
                    detectTapGestures(
                        onPress = { pos ->
                            isDragging = true
                            val height = size.height.toFloat()
                            if (height > 0) {
                                val y = pos.y.coerceIn(0f, height)
                                val f = 1f - (y / height)
                                val step = (minStep + (f * range).roundToInt()).coerceIn(minStep, maxStep)
                                if (step != currentStep) {
                                    onStepChange(step)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            tryAwaitRelease()
                            isDragging = false
                        }
                    )
                }
                .pointerInput(minStep, maxStep) {
                    detectVerticalDragGestures(
                        onDragStart = { pos ->
                            isDragging = true
                            val height = size.height.toFloat()
                            if (height > 0) {
                                val y = pos.y.coerceIn(0f, height)
                                val f = 1f - (y / height)
                                val step = (minStep + (f * range).roundToInt()).coerceIn(minStep, maxStep)
                                if (step != currentStep) {
                                    onStepChange(step)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val height = size.height.toFloat()
                            if (height > 0) {
                                val y = change.position.y.coerceIn(0f, height)
                                val f = 1f - (y / height)
                                val step = (minStep + (f * range).roundToInt()).coerceIn(minStep, maxStep)
                                if (step != currentStep) {
                                    onStepChange(step)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            // Right-aligned Track Column with 12dp right padding (never cut off)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, top = topMargin, bottom = bottomMargin)
                    .width(16.dp)
                    .height(trackHeight),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Track background line
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                )

                // Active track fill line
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(travelDistance * fraction + thumbHeight / 2)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Floating Tooltip Badge + Centered Thumb Handle Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = thumbYOffset)
                    .padding(end = 12.dp)
            ) {
                // Tooltip Badge on the left
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isDragging) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    shadowElevation = if (isDragging) 6.dp else 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "${(currentStep / 100.0).format(2)}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Centered Thumb Handle Pill (16dp container centered over 4dp track line)
                Box(
                    modifier = Modifier.width(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = if (isDragging) 6.dp else 2.dp,
                        modifier = Modifier.size(width = 12.dp, height = thumbHeight)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun PlayerProgressionCard(
    player: PlayerRankProgression,
    isCurrentUser: Boolean = true,
    totalGameCharts: Int,
    totalSongs: Int = 0,
    playedSongs: Int = 0,
    onRankClick: (PlayerRankProgression, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val clearedCount = player.getClearedCount()
    val playedCount = player.getPlayedCount()
    val rankCounts = player.getNormalizedRankCounts()

    val effTotalSongs = player.totalSongs ?: totalSongs
    val effPlayedSongs = player.playedSongs ?: playedSongs

    val progressFraction = if (effTotalSongs > 0 && effPlayedSongs > 0) {
        (effPlayedSongs.toFloat() / effTotalSongs.toFloat()).coerceIn(0f, 1f)
    } else if (totalGameCharts > 0) {
        (clearedCount.toFloat() / totalGameCharts.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percentage = (progressFraction * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Player Info Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = player.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (player.rating != null && player.rating > 0) {
                    MaimaiRatingBadge(
                        rating = player.rating,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Réduire" else "Développer"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Cleared Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (effTotalSongs > 0 && effPlayedSongs > 0) {
                        "Morceaux joués : $effPlayedSongs / $effTotalSongs"
                    } else if (totalGameCharts > 0) {
                        "Progression : $clearedCount / $totalGameCharts charts"
                    } else {
                        "Charts jouées : $clearedCount"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Key Rank Summary Row (always visible)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val topRanks = listOf("SSS+", "SSS", "SS+", "SS", "S+", "S")
                topRanks.forEach { rankKey ->
                    val count = rankCounts[rankKey] ?: when (rankKey) {
                        "SSS+" -> player.sssPlus ?: 0
                        "SSS" -> player.sss ?: 0
                        "SS+" -> player.ssPlus ?: 0
                        "SS" -> player.ss ?: 0
                        "S+" -> player.sPlus ?: 0
                        "S" -> player.s ?: 0
                        else -> 0
                    }
                    CompactRankBadge(
                        rankLabel = rankKey,
                        count = count,
                        onClick = if (isCurrentUser) { { onRankClick(player, rankKey) } } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Expanded detail section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Text(
                        text = "Détail des Rangs & Combos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Complete Rank Breakdown
                    val predefinedOrder = listOf(
                        "SSS+", "SSS", "SS+", "SS", "S+", "S",
                        "AAA", "AA", "A", "Autres",
                        "FC", "FC+", "AP", "AP+"
                    )
                    val definedRanksList = predefinedOrder.mapNotNull { rankKey ->
                        val count = rankCounts[rankKey] ?: when (rankKey) {
                            "SSS+" -> player.sssPlus ?: 0
                            "SSS" -> player.sss ?: 0
                            "SS+" -> player.ssPlus ?: 0
                            "SS" -> player.ss ?: 0
                            "S+" -> player.sPlus ?: 0
                            "S" -> player.s ?: 0
                            "AAA" -> player.aaa ?: 0
                            "AA" -> player.aa ?: 0
                            "A" -> player.a ?: 0
                            "FC" -> player.fc ?: 0
                            "FC+" -> player.fcp ?: 0
                            "AP" -> player.ap ?: 0
                            "AP+" -> player.app ?: 0
                            else -> 0
                        }
                        if (count > 0) rankKey to count else null
                    }
                    val extraRanks = rankCounts.filter { (k, v) -> v > 0 && k !in predefinedOrder }.map { (k, v) -> k to v }
                    val allRanksList = definedRanksList + extraRanks

                    val totalForPct = if (totalGameCharts > 0) totalGameCharts else clearedCount

                    if (allRanksList.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            allRanksList.chunked(3).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { (label, count) ->
                                        DetailedRankItem(
                                            rankLabel = label,
                                            count = count,
                                            totalGameCharts = totalForPct,
                                            onClick = if (isCurrentUser) { { onRankClick(player, label) } } else null,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Aucun détail de rang disponible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (effPlayedSongs > 0 || playedCount > 0 || clearedCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total des charts/parties jouées :",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = (if (clearedCount > 0) clearedCount else if (effPlayedSongs > 0) effPlayedSongs else playedCount).toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactRankBadge(
    rankLabel: String,
    count: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = getRankBadgeColor(rankLabel)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Text(
                text = rankLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DetailedRankItem(
    rankLabel: String,
    count: Int,
    totalGameCharts: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = getRankBadgeColor(rankLabel)
    val pct = if (totalGameCharts > 0) ((count.toFloat() / totalGameCharts.toFloat()) * 100) else 0f

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = rankLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                if (pct > 0f) {
                    Text(
                        text = "${(pct * 10).toInt() / 10.0}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
