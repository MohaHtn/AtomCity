package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerHistoryEntry
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.details.*
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.formatPlayDate
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScoresDetails(
    songId: Int,
    taikoViewModel: TaikoViewModel,
    onBackClick: () -> Unit
) {
    val scoresData by taikoViewModel.scoresData.collectAsState()
    val communityScores by taikoViewModel.communityScores.collectAsState()
    val taikoUsers by taikoViewModel.taikoUsers.collectAsState()
    val isLoading by taikoViewModel.isLoading.collectAsState()
    val favoriteSongIds by taikoViewModel.favoriteSongIds.collectAsState()

    val isFavorite = favoriteSongIds.contains(songId)

    val filteredScores = remember(scoresData, songId) {
        scoresData?.songHistoryData?.filter { it.songId == songId } ?: emptyList()
    }

    val communityBestScores = remember(communityScores, songId) {
        communityScores.mapNotNull { (baid, history) ->
            val bestScore = history.songHistoryData
                .filter { it.songId == songId }
                .maxByOrNull { it.score ?: 0 }
            
            if (bestScore != null) {
                baid to bestScore
            } else {
                null
            }
        }.sortedByDescending { it.second.score ?: 0 }
    }

    val songInfo = filteredScores.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = songInfo?.musicName ?: "Détails de la musique",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!songInfo?.musicNameEN.isNullOrBlank() && songInfo.musicNameEN != songInfo.musicName) {
                            Text(
                                text = songInfo.musicNameEN,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = songInfo?.musicArtist ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredScores.size >= 3) {
                    item {
                        val history = filteredScores.sortedByDescending { it.score }.take(15).sortedBy { it.score }
                        TaikoDetailGraph(
                            title = "MEILLEURS SCORES (TOP 15)",
                            history = history,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineColor = getDifficultyColor(history.lastOrNull()?.difficulty)
                        )
                    }
                    item {
                        val history = filteredScores.sortedBy { it.playTime }.takeLast(15)
                        TaikoDetailGraph(
                            title = "PROGRESSION DES SCORES (15 DERNIERS)",
                            history = history,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineColor = getDifficultyColor(history.lastOrNull()?.difficulty)
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Pour afficher les graphiques, faites au moins 3 essais de cette chart !",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "HISTORIQUE DES TENTATIVES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredScores) { score ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = setDifficultyColorBackground(score.difficulty),
                        shape = RoundedCornerShape(16.dp)
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
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${displayDifficultyName(score.difficulty)} (${score.stars ?: 0})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "★".repeat(score.stars ?: 0),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Yellow
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = score.score.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            MiniScoreBadge("G", score.goodCount, Color(0xFFFFD700))
                                            MiniScoreBadge("O", score.okCount, Color(0xFFC0C0C0))
                                            MiniScoreBadge("M", score.missCount, Color(0xFFE57373))
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        if ((score.comboCount ?: 0) > 0) {
                                            Text(
                                                text = "COMBO ${score.comboCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString()),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (communityBestScores.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SCORES DES AUTRES JOUEURS D'ATOM CITY",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(communityBestScores) { (baid, score) ->
                        val user = taikoUsers.find { it.baid == baid }
                        
                        LaunchedEffect(baid, user?.nickname) {
                            if (user?.nickname == null) {
                                taikoViewModel.fetchUserNickname(baid)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = setDifficultyColorBackground(score.difficulty).copy(containerColor = getDifficultyColor(score.difficulty).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = user?.nickname ?: "Joueur $baid",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = score.score.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = displayDifficultyName(score.difficulty),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if ((score.comboCount ?: 0) > 0) {
                                            Text(
                                                text = "COMBO ${score.comboCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = formatPlayDate(score.playTime.toString()),
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

