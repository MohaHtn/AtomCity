package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.data.remote.model.scorefetcher.PlayerRankProgression
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.core.AtomCitySearchBar
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.format
import org.koin.compose.koinInject

enum class RankSortOption(val label: String) {
    RATING("Rating"),
    CLEARED("Clears"),
    SSS_PLUS("SSS+"),
    SSS("SSS"),
    PLAYED("Parties")
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
    var selectedSortOption by remember { mutableStateOf(RankSortOption.RATING) }

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
                    RankSortOption.RATING -> (p2.rating ?: 0).compareTo(p1.rating ?: 0)
                    RankSortOption.CLEARED -> p2.getClearedCount().compareTo(p1.getClearedCount())
                    RankSortOption.SSS_PLUS -> {
                        val s1 = p1.getNormalizedRankCounts()["SSS+"] ?: p1.sssPlus ?: 0
                        val s2 = p2.getNormalizedRankCounts()["SSS+"] ?: p2.sssPlus ?: 0
                        s2.compareTo(s1)
                    }
                    RankSortOption.SSS -> {
                        val s1 = p1.getNormalizedRankCounts()["SSS"] ?: p1.sss ?: 0
                        val s2 = p2.getNormalizedRankCounts()["SSS"] ?: p2.sss ?: 0
                        s2.compareTo(s1)
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
            val currentPlayer = remember(rankProgressionState, userKeyHash) {
                rankProgressionState?.player ?: allPlayers.find { it.keyHash == userKeyHash }
            }

            if (!maimaiApiKey.isNullOrBlank() || currentPlayer != null) {
                val isPublic = currentPlayer?.isPublic ?: true
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
                        if (isUpdatingVisibility) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { checked ->
                                    viewModel.updateProgressionVisibility(checked)

                                    viewModel.updateProgressionVisibility(checked)
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
                        PlayerProgressionCard(
                            player = player,
                            totalGameCharts = totalGameCharts,
                            onRankClick = { p, rankLabel ->
                                selectedRankTarget = p to rankLabel
                                viewModel.fetchChartsForRank(p.keyHash, rankLabel)
                            }
                        )
                    }
                }
            }
        }

        selectedRankTarget?.let { (player, rankLabel) ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp)
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
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerProgressionCard(
    player: PlayerRankProgression,
    totalGameCharts: Int,
    onRankClick: (PlayerRankProgression, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val clearedCount = player.getClearedCount()
    val playedCount = player.getPlayedCount()
    val rankCounts = player.getNormalizedRankCounts()

    val progressFraction = if (totalGameCharts > 0) {
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
                    text = "Progression : $clearedCount / ${if (totalGameCharts > 0) totalGameCharts else "?"} charts",
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
                        onClick = { onRankClick(player, rankKey) },
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
                    val allRanksList = listOf(
                        "SSS+" to (rankCounts["SSS+"] ?: player.sssPlus ?: 0),
                        "SSS" to (rankCounts["SSS"] ?: player.sss ?: 0),
                        "SS+" to (rankCounts["SS+"] ?: player.ssPlus ?: 0),
                        "SS" to (rankCounts["SS"] ?: player.ss ?: 0),
                        "S+" to (rankCounts["S+"] ?: player.sPlus ?: 0),
                        "S" to (rankCounts["S"] ?: player.s ?: 0),
                        "AAA" to (rankCounts["AAA"] ?: player.aaa ?: 0),
                        "AA" to (rankCounts["AA"] ?: player.aa ?: 0),
                        "A" to (rankCounts["A"] ?: player.a ?: 0),
                        "FC" to (rankCounts["FC"] ?: player.fc ?: 0),
                        "FC+" to (rankCounts["FC+"] ?: player.fcp ?: 0),
                        "AP" to (rankCounts["AP"] ?: player.ap ?: 0),
                        "AP+" to (rankCounts["AP+"] ?: player.app ?: 0)
                    ).filter { it.second > 0 }

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
                                            totalGameCharts = totalGameCharts,
                                            onClick = { onRankClick(player, label) },
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

                    if (playedCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total de parties jouées :",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$playedCount",
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
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val color = getRankBadgeColor(rankLabel)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.clickable { onClick() }
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
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val color = getRankBadgeColor(rankLabel)
    val pct = if (totalGameCharts > 0) ((count.toFloat() / totalGameCharts.toFloat()) * 100) else 0f

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
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
