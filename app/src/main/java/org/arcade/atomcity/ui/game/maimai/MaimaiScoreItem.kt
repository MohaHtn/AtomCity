package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Log
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.LevelInfo
import org.arcade.atomcity.model.maitea.playsResponse.*
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getJacketBorderColor
import org.arcade.atomcity.utils.formatPlayDate

@Composable
fun MaimaiScoreItem(
    play: MaiteaApiData,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val difficultyColor = getJacketBorderColor(play.difficultyLevel?.value)
    
    var levelInfo by remember { mutableStateOf<LevelInfo?>(null) }
    val scope = rememberCoroutineScope()

    // Getting level info at the start of the page.
    LaunchedEffect(play.song?.id, play.difficultyLevel?.key) {
        if (play.song?.id != null && play.difficultyLevel?.key != null) {
            scope.launch {
                levelInfo = getMaimaiLevelInfo(context, play.song!!.id!!, play.difficultyLevel!!.key!!)
            }
        }
    }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = getDifficultyColorBackground(play.difficultyLevel?.value),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = play.difficultyLevel?.label?.uppercase() ?: "",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 60.sp
                ),
                color = difficultyColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(difficultyColor.copy(alpha = 0.15f), CircleShape)
                        )
                        
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(play.jacketImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${play.song?.name} artwork",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(2.dp, difficultyColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Best score chip
                    if (play.isHighScore == true) {
                        Surface(
                            color = Color(0xFFFFF9C4), // Pastel Yellow
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-5).dp), // Position it above the jacket halo
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                //TODO: i18n
                                text = "Meilleur score",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.6.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFFFBC02D)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Song & Artist Info
                Column(modifier = Modifier.weight(1f)) {
                    val songNameJp = play.song?.name?.jp
                    val songNameEn = play.song?.name?.en
                    
                    Text(
                        text = songNameJp ?: songNameEn ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (songNameEn != null && songNameEn != songNameJp) {
                        Text(
                            text = songNameEn,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }

                    // Artist name, which is printed only if it is defined.
                    val artistJp = play.song?.artist?.jp
                    val artistEn = play.song?.artist?.en
                    val displayArtist = if (artistJp != null && artistEn != null && artistJp != artistEn) {
                        "$artistJp / $artistEn"
                    } else {
                        artistJp ?: artistEn ?: ""
                    }

                    Text(
                        text = displayArtist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp).alpha(0.7f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Difficulty / Level info chip.
                    MaimaiDifficultyBadge(
                        difficultyValue = play.difficultyLevel?.value,
                        levelInfo = levelInfo,
                        rating = play.rating,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )

                }

                // Achievement Column
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    if (play.rank != null) {
                        Text(
                            text = play.rank!!,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = play.achievementFormatted?.replace("%", "") ?: "0.00",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )


                        Text(
                            text = "%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = difficultyColor,
                            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                        )
                    }

                    play.rating?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = formatPlayDate(play.playDate),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        modifier = Modifier.alpha(0.5f).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Master - Best Score")
@Composable
fun MaimaiScoreItemMasterPreview() {
    val samplePlay = MaiteaApiData(
        id = 1,
        rating = "12.87",
        isHighScore = true,
        achievementFormatted = "100.50%",
        rank = "SSS+",
        playDate = "2023-10-27T10:00:00Z",
        difficultyLevel = DifficultyLevel(value = "master", label = "Master"),
        song = Song(
            name = Name(jp = "Oshama Scramble!", en = "Oshama Scramble!"),
            artist = Artist(jp = "t+pazolite", en = "t+pazolite")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/715258450d147139c3543de1cd5fb024.jpg"
    )
    
    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }
}

@Preview(showBackground = true, name = "Expert")
@Composable
fun MaimaiScoreItemExpertPreview() {
    val samplePlay = MaiteaApiData(
        id = 2,
        isHighScore = false,
        rating = "12.87",
        achievementFormatted = "99.00%",
        rank = "SS",
        playDate = "2023-10-26T15:30:00Z",
        difficultyLevel = DifficultyLevel(value = "expert", label = "Expert"),
        song = Song(
            name = Name(jp = "HAYASAME", en = "HAYASAME"),
            artist = Artist(jp = "t+pazolite", en = "t+pazolite")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/94be91f714c4245fcab1fd577fab687b.jpg"
    )
    
    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }
}

@Preview(showBackground = true, name = "RE:master")
@Composable
fun MaimaiScoreItemRemasterPreview() {
    val samplePlay = MaiteaApiData(
        id = 3,
        isHighScore = true,
        rating = "12.87",
        achievementFormatted = "101.00%",
        rank = "SSS+",
        playDate = "2023-10-25T12:00:00Z",
        difficultyLevel = DifficultyLevel(value = "remaster", label = "Re:Master"),
        song = Song(
            name = Name(jp = "PANDORA PARADOXX", en = "PANDORA PARADOXX"),
            artist = Artist(jp = "PANDORA", en = "PANDORA")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/a154838e11a50e19ade3ec6567b1e4b6.jpg"
    )
    
    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }
}

@Preview(showBackground = true, name = "Advanced")
@Composable
fun MaimaiScoreItemAdvancedPreview() {
    val samplePlay = MaiteaApiData(
        id = 4,
        isHighScore = false,
        rating = "12.87",
        achievementFormatted = "97.50%",
        rank = "S",
        playDate = "2023-10-24T09:15:00Z",
        difficultyLevel = DifficultyLevel(value = "advanced", label = "Advanced"),
        song = Song(
            name = Name(jp = "Garakuta Doll Play", en = "Garakuta Doll Play"),
            artist = Artist(jp = "t+pazolite", en = "t+pazolite")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/7afdb52816e5ab4bba45070717656dcf.jpg"
    )
    
    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }
}

@Preview(showBackground = true, name = "Basic")
@Composable
fun MaimaiScoreItemBasicPreview() {
    val samplePlay = MaiteaApiData(
        id = 5,
        isHighScore = false,
        rating = "12.87",
        achievementFormatted = "100.00%",
        rank = "SSS",
        playDate = "2023-10-23T18:45:00Z",
        difficultyLevel = DifficultyLevel(value = "basic", label = "Basic"),
        song = Song(
            name = Name(jp = "Flower", en = "Flower"),
            artist = Artist(jp = "DJ YOSHITAKA", en = "DJ YOSHITAKA")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/55ebb569d2a1ce954c0754c1373afb56.jpg"
    )

    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }
}

@Preview(showBackground = true, name="Utage")
@Composable
fun MaimaiScoreItemUtagePreview() {
    val samplePlay = MaiteaApiData(
        id = 7,
        isHighScore = false,
        rating = "12.87",
        achievementFormatted = "100.00%",
        rank = "SSS",
        playDate = "2023-10-23T18:45:00Z",
        difficultyLevel = DifficultyLevel(key = 0, value = "utage" , label = "Utage"),
        song = Song(
            name = Name(jp = "Flower", en = "Flower"),
            artist = Artist(jp = "DJ YOSHITAKA", en = "DJ YOSHITAKA")
        ),
        jacketImageUrl = "https://maimai.sega.jp/storage/DX_jacket/55ebb569d2a1ce954c0754c1373afb56.jpg"
    )
    Box(modifier = Modifier.padding(8.dp)) {
        MaimaiScoreItem(play = samplePlay, onClick = {})
    }

}