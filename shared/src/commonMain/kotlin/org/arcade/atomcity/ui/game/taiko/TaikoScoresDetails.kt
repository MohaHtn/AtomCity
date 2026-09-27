package org.arcade.atomcity.ui.game.taiko

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
                            modifier = Modifier.padding(bottom = 8.dp)
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
                        
                        LaunchedEffect(baid, user?.nickname) {
                            if (user?.nickname == null) {
                                taikoViewModel.fetchUserNickname(baid)
                            }
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = setDifficultyColorBackground(score.difficulty),
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
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatTaikoScore(score.score),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = displayDifficultyName(score.difficulty),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        TaikoPlaySettingRow(entry = score)

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if ((score.comboCount ?: 0) > 0) {
                                            Text(
                                                text = "MAX COMBO ${score.comboCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString(), isUtc = false),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
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
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
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
                        colors = setDifficultyColorBackground(score.difficulty),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(getDifficultyDrawable(score.difficulty)),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.BottomEnd).size(80.dp),
                                contentScale = ContentScale.Fit,
                                alpha = 0.2f
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
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "★ ${score.stars ?: 0}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Black
                                                ),
                                                color = Color.White
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Crown Badge
                                            val crownUrl = getCrownImageUrl(score.crown)
                                            val crownTitle = getCrownTitle(score.crown)
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(6.dp)
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
                                                        contentScale = ContentScale.Fit
                                                    )
                                                    Text(
                                                        text = crownTitle,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            // Rank Badge
                                            val rankUrl = getScoreRankImageUrl(score.scoreRank)
                                            if (rankUrl != null) {
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(6.dp)
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
                                    TaikoPlaySettingRow(entry = score)
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
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            MiniScoreBadge("G", score.goodCount, Color(0xFFFFD700))
                                            MiniScoreBadge("O", score.okCount, Color(0xFFC0C0C0))
                                            MiniScoreBadge("M", score.missCount, Color(0xFFE57373))
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        if ((score.comboCount ?: 0) > 0) {
                                            Text(
                                                text = "MAX COMBO ${score.comboCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString(), isUtc = false),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
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
    modifier: Modifier = Modifier
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
            TaikoPlaySettingMiniBadge(speedImgUrl, "Vitesse $speedText")
        }
    }
    if (isVanishOn) {
        activeBadges.add {
            TaikoPlaySettingMiniBadge("https://taiko.farewell.dev/images/Doron.png", "Disparition")
        }
    }
    if (isInverseOn) {
        activeBadges.add {
            TaikoPlaySettingMiniBadge("https://taiko.farewell.dev/images/Mirror.png", "Inverser")
        }
    }
    if (isRandomOn) {
        val randomImg = if (randomVal == 2) "Random_Messy.png" else "Random_Whimsical.png"
        activeBadges.add {
            TaikoPlaySettingMiniBadge("https://taiko.farewell.dev/images/$randomImg", getRandomName(randomVal))
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
    text: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.35f),
        shape = RoundedCornerShape(6.dp)
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
                color = Color.White
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
