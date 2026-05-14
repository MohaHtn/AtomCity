package org.arcade.atomcity.ui.game.maimai

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
import androidx.compose.runtime.Composable
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
import org.arcade.atomcity.model.maitea.playsResponse.*
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getDifficultyLevelFromCSV
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.formatPlayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScoresDetails(
    scoreEntry: MaiteaApiData? = null,
    onBackClick: () -> Unit
) {
    val difficultyColor = getJacketBorderColor(scoreEntry?.difficultyLevel?.value)
    
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
                    // Header: BEST Badge + Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (scoreEntry?.isHighScore == true) {
                            Surface(
                                color = Color(0xFFFFF9C4),
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "Meilleur Score",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFBC02D)
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
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

                    // Difficulty Badge
                    Surface(
                        color = difficultyColor,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = getDifficultyLevelFromCSV(
                                context = LocalContext.current,
                                songName = scoreEntry?.song?.name?.jp ?: "Unknown",
                                difficulty = scoreEntry?.difficultyLevel?.value
                            ),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
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
                    
                    detail?.tap?.let { 
                        DetailRow("Tap", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0) 
                    }
                    detail?.hold?.let { 
                        DetailRow("Hold", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0) 
                    }
                    detail?.breakk?.let { 
                        DetailRow("Break", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0) 
                    }
                    detail?.slide?.let { 
                        DetailRow("Slide", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0) 
                    }
                    detail?.hits?.let { 
                        DetailRow("Touch", it.perfect ?: 0, it.great ?: 0, it.good ?: 0, it.bad ?: 0)
                    }
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
fun MaimaiScoresDetailsPreview() {
    val sampleScore = MaiteaApiData(
        id = 1,
        isHighScore = true,
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
