package org.arcade.atomcity.ui.game.maimai

import MaimaiScoreItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import org.arcade.atomcity.ui.game.maimai.details.FcApBadge
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.remote.model.scorefetcher.PlayerRankProgression
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
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
    modifier: Modifier = Modifier,
    viewModel: MaimaiViewModel,
    onBackClick: () -> Unit,
    onNavigateToDetails: ((Int) -> Unit)? = null,
) {
    val rankProgressionState by viewModel.rankProgression.collectAsState()
    val isLoading by viewModel.isLoadingRankProgression.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(RankSortOption.RANKS) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }

    var selectedRankTarget by remember { mutableStateOf<Pair<PlayerRankProgression, String>?>(null) }

    val apiKeyManager = koinInject<ApiKeyManager>()
    val maimaiApiKey by apiKeyManager.getApiKeyFlow("maimai").collectAsState(initial = null)
    val userKeyHash = remember(maimaiApiKey) {
        maimaiApiKey?.trim()?.takeIf { it.isNotBlank() }?.let { PlatformUtils.sha256(it) }
    }
    val rankCharts by viewModel.rankCharts.collectAsState()
    val isLoadingRankCharts by viewModel.isLoadingRankCharts.collectAsState()

    LaunchedEffect(selectedDifficulty) {
        viewModel.fetchRankProgression(difficulty = selectedDifficulty)
    }

    val totalGameCharts = rankProgressionState?.effectiveTotalGameCharts ?: 0
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

    val isUpdatingVisibility by viewModel.isUpdatingVisibility.collectAsState()
    val toggledVisibility by viewModel.progressionVisibility.collectAsState()
    val currentPlayer = remember(rankProgressionState, allPlayers, userKeyHash) {
        rankProgressionState?.player ?: allPlayers.find { it.keyHash == userKeyHash }
    }
    val currentUserPosition = remember(filteredAndSortedPlayers, userKeyHash) {
        filteredAndSortedPlayers.indexOfFirst { it.keyHash != null && it.keyHash == userKeyHash } + 1
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Progression des Joueurs",
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                    IconButton(onClick = { viewModel.fetchRankProgression(difficulty = selectedDifficulty) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "difficulties") {
                DifficultyPillRow(
                    selected = selectedDifficulty,
                    onSelect = { key ->
                        selectedDifficulty = key
                        PlatformUtils.hapticImpact()
                    }
                )
            }

            if (!maimaiApiKey.isNullOrBlank() || currentPlayer != null) {
                item(key = "me") {
                    val isPublic = toggledVisibility ?: currentPlayer?.isPublicEffective ?: true
                    ProgressionCard(
                        player = currentPlayer,
                        position = currentUserPosition,
                        playerCount = filteredAndSortedPlayers.size,
                        totalGameCharts = totalGameCharts,
                        totalSongs = rankProgressionState?.totalSongs ?: 0,
                        playedSongs = rankProgressionState?.playedSongs ?: 0,
                        isPublic = isPublic,
                        isUpdatingVisibility = isUpdatingVisibility,
                        onToggleVisibility = {
                            viewModel.toggleProgressionVisibility(wasPublic = isPublic, userKeyHash = userKeyHash)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            when {
                isLoading -> item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filteredAndSortedPlayers.isEmpty() -> item(key = "empty") {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Aucun joueur trouvé pour \"$searchQuery\"" else "Aucune donnée de progression disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(vertical = 64.dp, horizontal = 24.dp)
                    )
                }

                else -> {
                    item(key = "leaderboard-header") {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
                        ) {
                            Text(
                                text = "Classement",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${filteredAndSortedPlayers.size} joueurs",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    itemsIndexed(filteredAndSortedPlayers, key = { _, player -> player.keyHash ?: player.getDisplayName() }) { index, player ->
                        PlayerProgressionCard(
                            player = player,
                            position = index + 1,
                            isCurrentUser = player.keyHash != null && player.keyHash == userKeyHash,
                            totalGameCharts = totalGameCharts,
                            totalSongs = rankProgressionState?.totalSongs ?: 0,
                            playedSongs = rankProgressionState?.playedSongs ?: 0,
                            onRankClick = { p, rankLabel ->
                                selectedRankTarget = p to rankLabel
                                viewModel.fetchChartsForRank(p.keyHash, rankLabel, selectedDifficulty)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        selectedRankTarget?.let { (player, rankLabel) ->
            // Score details are only reachable for the user's own scores.
            val isOwnScores = userKeyHash != null && player.keyHash == userKeyHash
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

            val rankColor = getRankBadgeColor(rankLabel)

            ModalBottomSheet(
                onDismissRequest = { selectedRankTarget = null },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                // No end padding: the scrubber's touch area reaches the screen edge.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                ) {
                    // Header: rank tile, title and player
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, end = 20.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = rankColor,
                            contentColor = if (rankColor.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = rankLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Charts $rankLabel",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = player.getDisplayName(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Stat pills
                    if (!isLoadingRankCharts && rankCharts.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            SheetStatPill(label = "Charts", value = "${rankCharts.size}")
                            SheetStatPill(label = "Meilleur", value = "${maxChartPct.format(2)}%")
                            SheetStatPill(label = "Plus bas", value = "${minChartPct.format(2)}%")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when {
                        isLoadingRankCharts -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = rankColor)
                        }

                        rankCharts.isEmpty() -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune chart trouvée pour le rang $rankLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        else -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 520.dp)
                        ) {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = ACHIEVEMENT_SCRUBBER_WIDTH + 16.dp + 4.dp)
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

                                    val playId = play.id?.takeIf { it > 0 }
                                    MaimaiScoreItem(
                                        play = play,
                                        onClick = if (isOwnScores && playId != null && onNavigateToDetails != null) {
                                            {
                                                selectedRankTarget = null
                                                onNavigateToDetails(playId)
                                            }
                                        } else null,
                                        footer = {
                                            result.fcApText?.takeIf { it.isNotEmpty() }?.let { fcText ->
                                                FcApBadge(text = fcText, modifier = Modifier.padding(bottom = 4.dp))
                                            }
                                        }
                                    )
                                }
                            }

                            AchievementScrubber(
                                minStep = minStep,
                                maxStep = maxStep,
                                currentStep = currentStep,
                                accent = rankColor,
                                onStepChange = { newStep -> onSliderChange(newStep) },
                                onDraggingChange = { dragging -> isUserDraggingSlider = dragging },
                                edgePadding = 16.dp,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

private val ACHIEVEMENT_SCRUBBER_WIDTH = 40.dp

@Composable
private fun SheetStatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Vertical achievement scrubber drawn over the right edge of the chart list (fill [modifier]
 * to the list bounds). Only its right strip takes touches, extended by [edgePadding] so it
 * reaches the screen edge; while dragging, a value bubble floats over the list next to the handle.
 */
@Composable
fun AchievementScrubber(
    minStep: Int,
    maxStep: Int,
    currentStep: Int,
    accent: Color,
    onStepChange: (Int) -> Unit,
    onDraggingChange: (Boolean) -> Unit = {},
    edgePadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    // Pressing and dragging are tracked apart: once a drag starts, the tap detector's press is
    // cancelled, which would otherwise hide the value bubble mid-drag.
    var isPressed by remember { mutableStateOf(false) }
    var isDraggingHandle by remember { mutableStateOf(false) }
    val isDragging = isPressed || isDraggingHandle

    LaunchedEffect(isDragging) {
        onDraggingChange(isDragging)
    }

    val range = (maxStep - minStep).coerceAtLeast(1)
    val fraction = ((currentStep - minStep).toFloat() / range.toFloat()).coerceIn(0f, 1f)

    BoxWithConstraints(modifier = modifier) {
        val verticalMargin = 12.dp
        val trackWidth = 12.dp
        val trackHeight = (maxHeight - verticalMargin * 2).coerceAtLeast(0.dp)
        // Handle center, from the top of the box
        val handleCenterY = verticalMargin + trackHeight * (1f - fraction)

        fun stepAt(y: Float): Int {
            val top = with(density) { verticalMargin.toPx() }
            val height = with(density) { trackHeight.toPx() }.coerceAtLeast(1f)
            val f = 1f - ((y - top) / height).coerceIn(0f, 1f)
            return (minStep + (f * range).roundToInt()).coerceIn(minStep, maxStep)
        }

        fun update(y: Float) {
            val step = stepAt(y)
            if (step != currentStep) {
                onStepChange(step)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }

        // Touch strip, track and handle (drawn in the strip, left of edgePadding)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(ACHIEVEMENT_SCRUBBER_WIDTH + edgePadding)
                .fillMaxHeight()
                .pointerInput(minStep, maxStep) {
                    detectTapGestures(
                        onPress = { pos ->
                            isPressed = true
                            update(pos.y)
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
                .pointerInput(minStep, maxStep) {
                    detectVerticalDragGestures(
                        onDragStart = { pos ->
                            isDraggingHandle = true
                            update(pos.y)
                        },
                        onDragEnd = { isDraggingHandle = false },
                        onDragCancel = { isDraggingHandle = false },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            update(change.position.y)
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(ACHIEVEMENT_SCRUBBER_WIDTH)
                    .fillMaxHeight()
            ) {
            // Inactive track
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(vertical = verticalMargin)
                    .width(trackWidth)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f))
            )
            // Active track, filled from the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = verticalMargin)
                    .width(trackWidth)
                    .height(trackHeight * fraction)
                    .clip(CircleShape)
                    .background(accent)
            )
            // Handle: a bar wider than the track, with a surface-colored gap around it
            Surface(
                shape = CircleShape,
                color = accent,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceContainerLow),
                shadowElevation = if (isDragging) 4.dp else 1.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = handleCenterY - 6.dp)
                    .size(width = 34.dp, height = 12.dp)
            ) {}
            }
        }

        // Value bubble, next to the handle while dragging
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(1f, 0.5f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(1f, 0.5f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -(ACHIEVEMENT_SCRUBBER_WIDTH + edgePadding + 4.dp), y = handleCenterY - 20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shadowElevation = 6.dp,
                modifier = Modifier.height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "${(currentStep / 100.0).format(2)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

private val DIFFICULTY_FILTERS = listOf(
    null to "Toutes",
    "Easy" to "Easy",
    "Basic" to "Basic",
    "Advanced" to "Advanced",
    "Expert" to "Expert",
    "Master" to "Master",
    "Re:Master" to "Re:Master"
)

/**
 * Single scrollable row of pill buttons; the selected one is filled with its difficulty color.
 */
@Composable
private fun DifficultyPillRow(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DIFFICULTY_FILTERS, key = { it.second }) { (key, label) ->
            val isSelected = selected == key
            val accent = key?.let { getJacketBorderColor(it) } ?: MaterialTheme.colorScheme.primary
            val containerColor by animateColorAsState(
                if (isSelected) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "DifficultyPillContainer"
            )
            val contentColor by animateColorAsState(
                when {
                    !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
                    accent.luminance() > 0.5f -> Color.Black
                    else -> Color.White
                },
                label = "DifficultyPillContent"
            )
            Surface(
                onClick = { onSelect(key) },
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier.height(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private const val MAX_ACHIEVEMENT_PERCENT = 101.0

/**
 * Completion of a player as a 0..1 fraction and its display text: the average achievement when
 * available, otherwise the share of charts played.
 */
private fun PlayerRankProgression.completion(
    totalGameCharts: Int,
    totalSongs: Int,
    playedSongs: Int
): Pair<Float, String> {
    averageAchievement?.let { average ->
        return (average / MAX_ACHIEVEMENT_PERCENT).toFloat().coerceIn(0f, 1f) to "${average.format(2)}%"
    }
    val effTotalSongs = this.totalSongs ?: totalSongs
    val effPlayedSongs = this.playedSongs ?: playedSongs
    val fraction = when {
        completionPercentage != null -> (completionPercentage / 100.0).toFloat().coerceIn(0f, 1f)
        effTotalSongs > 0 && effPlayedSongs > 0 -> (effPlayedSongs.toFloat() / effTotalSongs.toFloat()).coerceIn(0f, 1f)
        totalGameCharts > 0 -> (getClearedCount().toFloat() / totalGameCharts.toFloat()).coerceIn(0f, 1f)
        else -> 0f
    }
    val display = completionPercentage?.let { "${it.format(2)}%" } ?: "${(fraction * 100).toInt()}%"
    return fraction to display
}

@Composable
private fun ThickProgressBar(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Highlighted summary of the user's own progression, with the sharing toggle.
 */
@Composable
private fun ProgressionCard(
    player: PlayerRankProgression?,
    position: Int,
    playerCount: Int,
    totalGameCharts: Int,
    totalSongs: Int,
    playedSongs: Int,
    isPublic: Boolean,
    isUpdatingVisibility: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = onContainer
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = "VOTRE PROGRESSION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = onContainer.copy(alpha = 0.7f)
            )

            if (player != null) {
                val (fraction, display) = player.completion(totalGameCharts, totalSongs, playedSongs)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = display,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (position > 0) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "#$position",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "sur $playerCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = onContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                ThickProgressBar(
                    progress = fraction,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = onContainer.copy(alpha = 0.12f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Text(
                    text = "Aucune progression disponible",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider(
                color = onContainer.copy(alpha = 0.12f),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Partager ma progression",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPublic) "Visible par les autres joueurs" else "Masquée aux autres joueurs",
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.7f)
                    )
                }
                if (isUpdatingVisibility) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp,
                        color = onContainer
                    )
                }
                Switch(
                    checked = isPublic,
                    enabled = !isUpdatingVisibility,
                    onCheckedChange = { onToggleVisibility() }
                )
            }
        }
    }
}

/**
 * Leaderboard position, with gold / silver / bronze for the podium.
 */
@Composable
private fun PositionBadge(position: Int, modifier: Modifier = Modifier) {
    val medal = when (position) {
        1 -> Color(0xFFFFC94D)
        2 -> Color(0xFFC9D3DB)
        3 -> Color(0xFFE0A774)
        else -> null
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = medal ?: MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (medal != null) Color(0xFF2B1D00) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$position",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PlayerProgressionCard(
    player: PlayerRankProgression,
    position: Int,
    isCurrentUser: Boolean,
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

    val (progressFraction, percentageDisplay) = player.completion(totalGameCharts, totalSongs, playedSongs)

    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "PlayerCardChevron")

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isCurrentUser) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Position, name and completion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PositionBadge(position = position)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.getDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "VOUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (effTotalSongs > 0 && effPlayedSongs > 0) {
                            "$effPlayedSongs / $effTotalSongs morceaux"
                        } else if (totalGameCharts > 0) {
                            "$clearedCount / $totalGameCharts charts"
                        } else {
                            "$clearedCount charts"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (player.rating != null && player.rating > 0) {
                    MaimaiRatingBadge(
                        rating = player.rating,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Text(
                    text = percentageDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }

            ThickProgressBar(
                progress = progressFraction,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(top = 14.dp)
            )

            // Key Rank Summary Row (always visible)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val topRanks = listOf("SSS+", "SSS", "SS+", "SS", "S+", "S")
                topRanks.forEach { rankKey ->
                    val count = player.rankCount(rankKey, rankCounts)
                    CompactRankBadge(
                        rankLabel = rankKey,
                        count = count,
                        onClick = { onRankClick(player, rankKey) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Réduire" else "Développer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
                    .rotate(chevronRotation)
            )

            // Expanded detail section
            AnimatedVisibility(visible = isExpanded) {
                val totalForPct = if (totalGameCharts > 0) totalGameCharts else clearedCount
                val ranks = RANK_DETAIL_ORDER.mapNotNull { key ->
                    player.rankCount(key, rankCounts).takeIf { it > 0 }?.let { key to it }
                } + rankCounts.filter { (k, v) -> v > 0 && k !in RANK_DETAIL_ORDER && k !in COMBO_DETAIL_ORDER }.toList()
                val combos = COMBO_DETAIL_ORDER.mapNotNull { key ->
                    player.rankCount(key, rankCounts).takeIf { it > 0 }?.let { key to it }
                }
                val maxRankCount = ranks.maxOfOrNull { it.second } ?: 0
                val maxComboCount = combos.maxOfOrNull { it.second } ?: 0

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (ranks.isEmpty() && combos.isEmpty()) {
                        Text(
                            text = "Aucun détail de rang disponible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ranks.isNotEmpty()) {
                        DetailSectionTitle("Rangs")
                        ranks.forEach { (label, count) ->
                            RankBreakdownRow(
                                rankLabel = label,
                                count = count,
                                total = totalForPct,
                                scaleMax = maxRankCount,
                                onClick = { onRankClick(player, label) }
                            )
                        }
                    }
                    if (combos.isNotEmpty()) {
                        DetailSectionTitle("Combos", modifier = Modifier.padding(top = 8.dp))
                        combos.forEach { (label, count) ->
                            RankBreakdownRow(
                                rankLabel = label,
                                count = count,
                                total = totalForPct,
                                scaleMax = maxComboCount,
                                onClick = { onRankClick(player, label) }
                            )
                        }
                    }

                    if (effPlayedSongs > 0 || playedCount > 0 || clearedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Charts jouées",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = (if (clearedCount > 0) clearedCount else if (effPlayedSongs > 0) effPlayedSongs else playedCount).toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
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
    val shape = RoundedCornerShape(14.dp)
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = shape,
        modifier = if (onClick != null) modifier.clip(shape).clickable { onClick() } else modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
        ) {
            Text(
                text = rankLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private val RANK_DETAIL_ORDER = listOf("SSS+", "SSS", "SS+", "SS", "S+", "S", "AAA", "AA", "A", "Autres")
private val COMBO_DETAIL_ORDER = listOf("FC", "FC+", "AP", "AP+")

private fun PlayerRankProgression.rankCount(key: String, rankCounts: Map<String, Int>): Int =
    rankCounts[key] ?: when (key) {
        "SSS+" -> sssPlus
        "SSS" -> sss
        "SS+" -> ssPlus
        "SS" -> ss
        "S+" -> sPlus
        "S" -> s
        "AAA" -> aaa
        "AA" -> aa
        "A" -> a
        "FC" -> fcCount ?: fc
        "FC+" -> fcPlusCount ?: fcp
        "AP" -> apCount ?: ap
        "AP+" -> apPlusCount ?: app
        else -> null
    } ?: 0

@Composable
private fun DetailSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

/**
 * One rank of the detail: colored label, bar scaled to [scaleMax] (the largest count of its
 * group, so differences stay visible), count and share of [total].
 */
@Composable
fun RankBreakdownRow(
    rankLabel: String,
    count: Int,
    total: Int,
    scaleMax: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color = getRankBadgeColor(rankLabel)
    val barFraction = if (scaleMax > 0) (count.toFloat() / scaleMax).coerceIn(0f, 1f) else 0f
    val share = if (total > 0) count * 100.0 / total else 0.0
    val shape = RoundedCornerShape(16.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.16f),
            modifier = Modifier.width(56.dp)
        ) {
            Text(
                text = rankLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(barFraction)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(min = 56.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black
            )
            if (share > 0.0) {
                Text(
                    text = if (share < 0.1) "<0.1%" else "${share.format(1)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
