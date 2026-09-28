package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.game.taiko.details.*
import org.arcade.atomcity.ui.game.taiko.settings.getRandomName
import org.arcade.atomcity.ui.game.taiko.settings.getScoreRankImageUrl
import org.arcade.atomcity.ui.game.taiko.settings.getSpeedName
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScoresDetails(
    songId: Int,
    targetDifficultyArg: Int? = null,
    targetScoreArg: Int? = null,
    targetPlayTimeArg: String? = null,
    taikoViewModel: TaikoViewModel,
    onNavigateToRoute: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val scoresData by taikoViewModel.scoresData.collectAsState()
    val communityScores by taikoViewModel.communityScores.collectAsState()
    val taikoUsers by taikoViewModel.taikoUsers.collectAsState()
    val isLoading by taikoViewModel.isLoading.collectAsState()
    val favoriteSongIds by taikoViewModel.favoriteSongIds.collectAsState()

    val isFavorite = favoriteSongIds.contains(songId)

    var sortByScore by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(false) }

    val filteredScores = remember(scoresData, songId, targetDifficultyArg) {
        val allSongScores = scoresData?.songHistoryData?.filter { it.songId == songId } ?: emptyList()
        if (targetDifficultyArg != null) {
            allSongScores.filter { it.difficulty == targetDifficultyArg }
        } else {
            allSongScores
        }
    }

    val targetEntry = remember(filteredScores, targetScoreArg, targetPlayTimeArg) {
        val normalizedTargetTime = targetPlayTimeArg?.replace("T", " ")?.replace(" ", "")
        
        filteredScores.find { entry ->
            val matchesScore = targetScoreArg != null && entry.score == targetScoreArg
            val matchesTime = if (!normalizedTargetTime.isNullOrBlank() && entry.playTime != null) {
                entry.playTime.replace("T", " ").replace(" ", "") == normalizedTargetTime
            } else false
            
            if (targetScoreArg != null && !normalizedTargetTime.isNullOrBlank()) {
                matchesScore && matchesTime
            } else {
                matchesScore || matchesTime
            }
        } ?: if (targetScoreArg != null) {
            filteredScores.find { it.score == targetScoreArg }
        } else if (!normalizedTargetTime.isNullOrBlank()) {
            filteredScores.find { entry ->
                entry.playTime != null && entry.playTime.replace("T", " ").replace(" ", "") == normalizedTargetTime
            }
        } else null
    }

    val bestScoreEntry = remember(filteredScores) {
        filteredScores.maxByOrNull { it.score ?: 0 }
    }

    var selectedScore by remember(filteredScores, targetEntry, bestScoreEntry) {
        mutableStateOf(targetEntry ?: bestScoreEntry)
    }

    LaunchedEffect(filteredScores, targetEntry, bestScoreEntry) {
        selectedScore = targetEntry ?: bestScoreEntry
    }

    val sortedHistory = remember(filteredScores, sortByScore, sortAscending) {
        if (sortByScore) {
            if (sortAscending) {
                filteredScores.sortedBy { it.score }
            } else {
                filteredScores.sortedByDescending { it.score }
            }
        } else {
            if (sortAscending) {
                filteredScores.sortedBy { it.playTime }
            } else {
                filteredScores.sortedByDescending { it.playTime }
            }
        }
    }

    val isNightMode = isAppInDarkTheme()

    val songInfo = selectedScore ?: filteredScores.firstOrNull() ?: scoresData?.songHistoryData?.firstOrNull { it.songId == songId }
    val targetDifficulty = targetDifficultyArg ?: songInfo?.difficulty

    val communityBestScores = remember(communityScores, songId, targetDifficulty) {
        communityScores.mapNotNull { (baid, history) ->
            val bestScore = history.songHistoryData
                .filter { it.songId == songId && (targetDifficulty == null || it.difficulty == targetDifficulty) }
                .maxByOrNull { it.score ?: 0 }
            
            if (bestScore != null) {
                baid to bestScore
            } else {
                null
            }
        }.sortedByDescending { it.second.score ?: 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        val titleText = songInfo?.musicNameEN.takeIf { !it.isNullOrBlank() } ?: songInfo?.musicName ?: "Détails de la musique"
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!songInfo?.musicName.isNullOrBlank() && songInfo.musicName != titleText) {
                            Text(
                                text = songInfo.musicName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!songInfo?.musicArtist.isNullOrBlank()) {
                            Text(
                                text = songInfo.musicArtist,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    val musicDetailsData by taikoViewModel.musicDetailsData.collectAsState()
                    val songGenre = songInfo?.genre ?: musicDetailsData?.get(songId.toString())?.genre
                    if (getTaikoGenreInfo(songGenre) != null) {
                        TaikoGenreBadge(
                            genre = songGenre,
                            isNightMode = isNightMode,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(onClick = { taikoViewModel.toggleFavorite(songId) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (filteredScores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Aucun historique trouvé pour cette musique.", modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Featured Score Card
                item {
                    selectedScore?.let { score ->
                        TaikoScoreCard(
                            entry = score,
                            isBestScore = (score.score == bestScoreEntry?.score && score.playTime == bestScoreEntry?.playTime),
                            modifier = Modifier.padding(bottom = 8.dp),
                            isNightMode = isNightMode
                        )
                    }
                }

                if (communityBestScores.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "MEILLEURS SCORES D'ATOM CITY DE CETTE CHART",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = getDifficultyColor(songInfo?.difficulty)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp, start = 8.dp)
                        )
                    }

                    items(communityBestScores) { (baid, score) ->
                        val user = taikoUsers.find { it.baid == baid }
                        val diffColor = getDifficultyColor(score.difficulty)
                        val cardTextColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
                        val cardTextSecondary = if (isNightMode) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        
                        LaunchedEffect(baid, user?.nickname) {
                            if (user?.nickname == null) {
                                taikoViewModel.fetchUserNickname(baid)
                            }
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .border(
                                    1.dp,
                                    if (isNightMode) diffColor.copy(alpha = 0.35f) else diffColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(24.dp)
                                ),
                            colors = setDifficultyColorBackground(score.difficulty, isNightMode),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = user?.nickname ?: "Joueur $baid",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = cardTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatTaikoScore(score.score),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = cardTextColor,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = displayDifficultyName(score.difficulty),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = diffColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        TaikoPlaySettingRow(entry = score, isNightMode = isNightMode)

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if ((score.comboCount ?: 0) > 0) {
                                            Text(
                                                text = "MAX COMBO ${score.comboCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = cardTextColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString(), isUtc = false),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cardTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredScores.size < 3) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, getDifficultyColor(songInfo?.difficulty).copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Pour afficher les graphiques de statistiques, faites au moins 3 essais de cette chart !",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = getDifficultyColor(songInfo?.difficulty).copy(alpha = 0.7f)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val history = filteredScores.sortedByDescending { it.score }.take(15).sortedBy { it.score }
                        TaikoDetailGraph(
                            title = "MEILLEURS SCORES (TOP 15)",
                            history = history,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineColor = getDifficultyColor(songInfo?.difficulty)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val history = filteredScores.sortedBy { it.playTime }.takeLast(15)
                        TaikoDetailGraph(
                            title = "PROGRESSION DES SCORES (15 DERNIERS)",
                            history = history,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineColor = getDifficultyColor(songInfo?.difficulty)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "HISTORIQUE DES SCORES • ${filteredScores.size} ESSAI(S)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = getDifficultyColor(songInfo?.difficulty)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, start = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trier par :",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        FilterChip(
                            selected = !sortByScore,
                            onClick = {
                                if (!sortByScore) {
                                    sortAscending = !sortAscending
                                } else {
                                    sortByScore = false
                                    sortAscending = false
                                }
                            },
                            label = {
                                Text(
                                    text = if (!sortByScore) {
                                        if (sortAscending) "Date (Ancien)" else "Date (Récents)"
                                    } else "Date"
                                )
                            },
                            leadingIcon = if (!sortByScore) {
                                {
                                    Icon(
                                        imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )

                        FilterChip(
                            selected = sortByScore,
                            onClick = {
                                if (sortByScore) {
                                    sortAscending = !sortAscending
                                } else {
                                    sortByScore = true
                                    sortAscending = false
                                }
                            },
                            label = {
                                Text(
                                    text = if (sortByScore) {
                                        if (sortAscending) "Score (Croissant)" else "Score (Décroissant)"
                                    } else "Score"
                                )
                            },
                            leadingIcon = if (sortByScore) {
                                {
                                    Icon(
                                        imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                    items(sortedHistory) { score ->
                        val diffColor = getDifficultyColor(score.difficulty)
                        val cardTextColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
                        val cardTextSecondary = if (isNightMode) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(
                                    1.dp,
                                    if (isNightMode) diffColor.copy(alpha = 0.35f) else diffColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    if (score == selectedScore) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                        PlatformUtils.hapticImpact()
                                    } else {
                                        val route = buildTaikoScoreRoute(score)
                                        if (route != null) {
                                            onNavigateToRoute(route)
                                            PlatformUtils.hapticImpact()
                                        }
                                    }
                                },
                            colors = setDifficultyColorBackground(score.difficulty, isNightMode),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(getDifficultyDrawable(score.difficulty)),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.BottomEnd).size(80.dp),
                                    contentScale = ContentScale.Fit,
                                    alpha = if (isNightMode) 0.18f else 0.15f
                                )
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = displayDifficultyName(score.difficulty),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = diffColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "★ ${score.stars ?: 0}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Black
                                                    ),
                                                    color = cardTextColor
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Crown Badge
                                                val crownUrl = getCrownImageUrl(score.crown)
                                                val crownTitle = getCrownTitle(score.crown)
                                                val crownInfo = getCrownBadgeInfo(score.crown, isNightMode)
                                                val isFailedCrown = score.crown == 0 || score.crown == null

                                                Surface(
                                                    color = crownInfo.containerColor,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, crownInfo.borderColor)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        AsyncImage(
                                                            model = crownUrl,
                                                            contentDescription = crownTitle,
                                                            modifier = Modifier.height(18.dp),
                                                            contentScale = ContentScale.Fit,
                                                            colorFilter = if (isFailedCrown) ColorFilter.tint(if (isNightMode) Color.White else Color.Black) else null
                                                        )
                                                        Text(
                                                            text = crownTitle,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp
                                                            ),
                                                            color = crownInfo.contentColor
                                                        )
                                                    }
                                                }

                                                // Rank Badge
                                                val rankUrl = getScoreRankImageUrl(score.scoreRank)
                                                val rankInfo = getScoreRankBadgeInfo(score.scoreRank, isNightMode)
                                                if (rankUrl != null && rankInfo != null) {
                                                    Surface(
                                                        color = rankInfo.containerColor,
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(1.dp, rankInfo.contentColor.copy(alpha = if (isNightMode) 0.4f else 0.3f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AsyncImage(
                                                                model = rankUrl,
                                                                contentDescription = "Rank",
                                                                modifier = Modifier.height(18.dp),
                                                                contentScale = ContentScale.Fit
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // TOP RIGHT CORNER: PlaySetting badges
                                        TaikoPlaySettingRow(entry = score, isNightMode = isNightMode)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = formatTaikoScore(score.score),
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = cardTextColor,
                                                fontWeight = FontWeight.Black
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                MiniScoreBadge("G", score.goodCount, Color(0xFFFFD700), isNightMode)
                                                MiniScoreBadge("O", score.okCount, Color(0xFFC0C0C0), isNightMode)
                                                MiniScoreBadge("M", score.missCount, Color(0xFFE57373), isNightMode)
                                            }
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            if ((score.comboCount ?: 0) > 0) {
                                                Text(
                                                    text = "MAX COMBO ${score.comboCount}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = cardTextColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = formatPlayDate(score.playTime.toString(), isUtc = false),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = cardTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun TaikoPlaySettingRow(
    entry: TaikoServerHistoryEntry,
    modifier: Modifier = Modifier,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    val setting = entry.playSetting ?: return

    val speedVal = setting.speed ?: 0
    val speedText = getSpeedName(speedVal)
    val speedImgUrl = "https://taiko.farewell.dev/images/Speed/${if (speedVal in 0..14) speedVal else 0}.png"

    val isVanishOn = setting.isVanishOn == true
    val isInverseOn = setting.isInverseOn == true
    val randomVal = setting.randomType ?: 0
    val isRandomOn = randomVal == 1 || randomVal == 2

    val hasSpeed = setting.speed != null

    val activeBadges = mutableListOf<@Composable () -> Unit>()

    if (hasSpeed) {
        activeBadges.add {
            val container = if (isNightMode) Color(0xFF512DA8).copy(alpha = 0.45f) else null
            val content = if (isNightMode) Color(0xFFD1C4E9) else null
            TaikoPlaySettingMiniBadge(
                imageUrl = speedImgUrl,
                text = "Vitesse $speedText",
                isNightMode = isNightMode,
                containerColor = container,
                contentColor = content
            )
        }
    }
    if (isVanishOn) {
        activeBadges.add {
            val container = if (isNightMode) Color(0xFF00838F).copy(alpha = 0.45f) else null
            val content = if (isNightMode) Color(0xFFB2EBF2) else null
            TaikoPlaySettingMiniBadge(
                imageUrl = "https://taiko.farewell.dev/images/Doron.png",
                text = "Disparition",
                isNightMode = isNightMode,
                containerColor = container,
                contentColor = content
            )
        }
    }
    if (isInverseOn) {
        activeBadges.add {
            val container = if (isNightMode) Color(0xFFC2185B).copy(alpha = 0.45f) else null
            val content = if (isNightMode) Color(0xFFF8BBD0) else null
            TaikoPlaySettingMiniBadge(
                imageUrl = "https://taiko.farewell.dev/images/Mirror.png",
                text = "Inverser",
                isNightMode = isNightMode,
                containerColor = container,
                contentColor = content
            )
        }
    }
    if (isRandomOn) {
        val randomImg = if (randomVal == 2) "Random_Messy.png" else "Random_Whimsical.png"
        val container = if (isNightMode) Color(0xFFE65100).copy(alpha = 0.45f) else null
        val content = if (isNightMode) Color(0xFFFFCC80) else null
        activeBadges.add {
            TaikoPlaySettingMiniBadge(
                imageUrl = "https://taiko.farewell.dev/images/$randomImg",
                text = getRandomName(randomVal),
                isNightMode = isNightMode,
                containerColor = container,
                contentColor = content
            )
        }
    }

    if (activeBadges.isEmpty()) return

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        activeBadges.chunked(2).forEach { rowBadges ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowBadges.forEach { badge -> badge() }
            }
        }
    }
}

@Composable
private fun TaikoPlaySettingMiniBadge(
    imageUrl: String?,
    text: String,
    isNightMode: Boolean = isAppInDarkTheme(),
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val bg = containerColor ?: if (isNightMode) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f)
    val textAndBorderColor = contentColor ?: if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val border = BorderStroke(1.dp, textAndBorderColor.copy(alpha = if (isNightMode) 0.35f else 0.12f))

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = text,
                    modifier = Modifier.height(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = textAndBorderColor
            )
        }
    }
}

fun buildTaikoScoreRoute(entry: TaikoServerHistoryEntry): String? {
    val id = entry.songId ?: return null
    val diff = entry.difficulty
    val scoreVal = entry.score
    val time = entry.playTime
    return buildString {
        append("taikoScoresDetails/$id")
        val params = mutableListOf<String>()
        if (diff != null) params.add("difficulty=$diff")
        if (scoreVal != null) params.add("score=$scoreVal")
        if (!time.isNullOrBlank()) {
            val encodedTime = time.replace(" ", "T")
            params.add("playTime=$encodedTime")
        }
        if (params.isNotEmpty()) {
            append("?")
            append(params.joinToString("&"))
        }
    }
}
