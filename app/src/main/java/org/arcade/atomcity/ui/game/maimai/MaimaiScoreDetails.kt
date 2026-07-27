package org.arcade.atomcity.ui.game.maimai

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import kotlin.math.hypot
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.LevelInfo
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.playsResponse.*
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.formatPlayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScoresDetails(
    scoreEntry: MaiteaApiData? = null,
    maiteaViewModel: MaiteaViewModel? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val difficultyColor = getJacketBorderColor(scoreEntry?.difficultyLevel?.value)
    
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }
    val scope = rememberCoroutineScope()

    val chartHistory by maiteaViewModel?.chartHistory?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val bestPerPlayer by maiteaViewModel?.bestPerPlayer?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    LaunchedEffect(scoreEntry?.song?.id, scoreEntry?.difficultyLevel?.key) {
        if (scoreEntry?.song?.id != null && scoreEntry.difficultyLevel?.key != null) {
            scope.launch {
                levelInfo = getMaimaiLevelInfo(context, scoreEntry.song!!.id!!, scoreEntry.difficultyLevel!!.key!!)
            }
        }
        
        scoreEntry?.song?.name?.en?.let {
            maiteaViewModel?.fetchChartHistory(it, scoreEntry.difficultyLevel?.value)
            maiteaViewModel?.fetchBestPerPlayer(it)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Détails du Score",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                colors = getDifficultyColorBackground(scoreEntry?.difficultyLevel?.value),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Badges + Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (scoreEntry?.isHighScore == true) {
                                ScoreBadge(
                                    text = "MEILLEUR SCORE",
                                    containerColor = Color(0xFFFFF9C4),
                                    contentColor = Color(0xFFFBC02D)
                                )
                            }
                            if (scoreEntry?.isAllPerfect == true) {
                                ScoreBadge(
                                    text = "ALL PERFECT",
                                    containerColor = Color(0xFFE0F2F1),
                                    contentColor = Color(0xFF00897B)
                                )
                            }
                            if (scoreEntry?.isTrackSkip == true) {
                                ScoreBadge(
                                    text = "TRACK SKIP",
                                    containerColor = Color(0xFFFFEBEE),
                                    contentColor = Color(0xFFE53935)
                                )
                            }
                        }
                        
                        Text(
                            text = formatPlayDate(scoreEntry?.playDate),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Jacket
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(difficultyColor.copy(alpha = 0.1f), CircleShape)
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(scoreEntry?.jacketImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = scoreEntry?.song?.name?.jp,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(4.dp, difficultyColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Song Info
                    Text(
                        text = scoreEntry?.song?.name?.jp ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (scoreEntry?.song?.name?.en != null && scoreEntry.song?.name?.en != scoreEntry.song?.name?.jp) {
                        Text(
                            text = scoreEntry.song?.name?.en ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.alpha(0.5f).padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val artistJp = scoreEntry?.song?.artist?.jp
                    val artistEn = scoreEntry?.song?.artist?.en
                    
                    Text(
                        text = artistJp ?: artistEn ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.alpha(0.8f)
                    )
                    
                    if (artistEn != null && artistEn != artistJp) {
                        Text(
                            text = artistEn,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.alpha(0.4f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Achievement Display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (scoreEntry?.rank != null) {
                            Text(
                                text = scoreEntry.rank!!,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = difficultyColor,
                                    fontSize = 32.sp
                                ),
                                modifier = Modifier.padding(bottom = 12.dp, end = 8.dp)
                            )
                        }

                        Text(
                            text = scoreEntry?.achievementFormatted?.replace("%", "") ?: "0.00",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 56.sp,
                                letterSpacing = (-2).sp
                            )
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = difficultyColor
                            ),
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Difficulty Badge with Level Info
                    MaimaiDifficultyBadge(
                        difficultyValue = scoreEntry?.difficultyLevel?.value,
                        levelInfo = levelInfo,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed Stats Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DETAILS DU SCORE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = difficultyColor
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                        listOf("PERFECT", "GREAT", "GOOD", "MISS").forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )

                    val detail = scoreEntry?.scoreDetail

                    detail?.hits?.let {
                        DetailRow("Total", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }

                    detail?.tap?.let {
                        DetailRow("Taps", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.hold?.let { 
                        DetailRow("Holds", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.slide?.let { 
                        DetailRow("Slides", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                    detail?.breakk?.let {
                        DetailRow("Breaks", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                }
            }

            // SCOREFETCHER: best-per-player results
            if (bestPerPlayer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MEILLEURS SCORES D'ATOM CITY DE CETTE CHART",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = difficultyColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 8.dp)
                )

                bestPerPlayer.forEach { b ->
                    BestPerPlayerItem(b)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (chartHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "HISTORIQUE DES SCORES • ${chartHistory.size} ESSAI(S)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = difficultyColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 8.dp)
                )

                chartHistory.forEach { historyEntry ->
                    ChartHistoryItem(historyEntry)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ChartHistoryItem(historyEntry: ChartHistoryResponse) {
    val difficultyColor = getJacketBorderColor(historyEntry.difficulty?.lowercase())
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatPlayDate(historyEntry.playDate),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${historyEntry.difficulty} ${historyEntry.difficultyLevel ?: ""}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = historyEntry.rank ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
                Text(
                    text = String.format("%.2f%%", (historyEntry.achievement ?: 0.0) / 100.0),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun BestPerPlayerItem(b: BestPerPlayerResponse) {
    val difficultyColor = getJacketBorderColor(b.difficulty?.lowercase())
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatPlayDate(b.playDate),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.alpha(0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = b.playerName ?: "",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${b.difficulty ?: ""} ${b.difficultyLevel ?: ""}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = b.rank ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
                Text(
                    text = String.format("%.2f%%", (b.achievement ?: 0.0) / 100.0),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun ScoreBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = contentColor
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailRow(label: String, p: Int, gr: Int, gd: Int, m: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1.2f)
        )
        
        // Value columns with pill-shaped backgrounds
        listOf(
            p to Color(0xFFFFD700),     // Perfect - Gold
            gr to Color(0xFFFF4081),    // Great - Pink
            gd to Color(0xFF00E676),    // Good - Green
            m to Color.Gray             // Miss - Gray
        ).forEach { (count, color) ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (count > 0) {
                    Surface(
                        color = color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = color
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_9_pro")
@Composable
fun MaimaiScoresDetailsPreview() {
    val sampleScore = MaiteaApiData(
        id = 1,
        isHighScore = true,
        isAllPerfect = true,
        isTrackSkip = false,
        achievementFormatted = "100.50%",
        rank = "SSS+",
        playDate = "2023-10-27T10:00:00Z",
        difficultyLevel = DifficultyLevel(value = "master", label = "Master"),
        song = Song(
            name = Name(jp = "Oshama Scramble!", en = "Oshama Scramble!"),
            artist = Artist(jp = "t+pazolite", en = "t+pazolite")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/715258450d147139c3543de1cd5fb024.jpg",
        scoreDetail = ScoreDetail(
            tap = Tap(perfect = 500, great = 10, good = 1, bad = 0),
            hold = Hold(perfect = 50, great = 2, good = 0, bad = 0),
            slide = Slide(perfect = 30, great = 1, good = 0, bad = 0),
            breakk = Break(perfect = 20, great = 0, good = 0, bad = 0),
            hits = Hits(perfect = 40, great = 0, good = 0, bad = 0)
        )
    )

    AtomCityTheme {
        MaimaiScoresDetails(
            scoreEntry = sampleScore,
            onBackClick = {}
        )
    }
}
