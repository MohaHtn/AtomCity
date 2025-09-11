package org.arcade.atomcity.ui.game.maimai

import android.content.Context
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.utils.formatPlayDate
import java.io.BufferedReader
import java.io.InputStreamReader
import org.arcade.atomcity.ui.game.common.getDifficultyColorBackground
import org.arcade.atomcity.ui.game.common.getDifficultyLevelFromCSV
import org.arcade.atomcity.ui.game.common.getJacketBorderColor

@Composable
fun MaimaiScoresDetails(
    scoreEntry: MaiteaApiData? = null
) {
    Scaffold { innerPadding ->
        Card(
            colors = getDifficultyColorBackground(scoreEntry?.difficultyLevel?.value),
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(scoreEntry?.jacketImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = scoreEntry?.song?.name?.jp ?: "Song Jacket",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .border(4.dp, getJacketBorderColor(scoreEntry?.difficultyLevel?.value),
                            androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = scoreEntry?.song?.name?.jp ?: "Unknown",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = formatPlayDate(scoreEntry?.playDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = scoreEntry?.song?.name?.en ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(4.dp)

                    )
                    Text(
                        text = getDifficultyLevelFromCSV(
                            context = LocalContext.current,
                            songName = scoreEntry?.song?.name?.jp ?: "Unknown",
                            difficulty = scoreEntry?.difficultyLevel?.value
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = if (scoreEntry?.isHighScore == true) "Meilleur Score" else "Score",
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (scoreEntry?.isHighScore == true) Color(0xFFFFEB3B).copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f),
                            labelColor = if (scoreEntry?.isHighScore == true) Color.Black else Color.Gray
                        ),
                        leadingIcon = if (scoreEntry?.isHighScore == true) {
                            {}
                        } else null,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = scoreEntry?.achievementFormatted ?: "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)

                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1.5f)

                    )
                    Text(
                        text = "Perfect",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)

                    )
                    Text(
                        text = "Great",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Good",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Bad",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                TableRow(
                    label = "Tap",
                    nbPerfect = scoreEntry?.scoreDetail?.tap?.perfect.toString(),
                    nbGreat = scoreEntry?.scoreDetail?.tap?.great.toString(),
                    nbGood = scoreEntry?.scoreDetail?.tap?.good.toString(),
                    nbBad = scoreEntry?.scoreDetail?.tap?.bad.toString()
                )
                TableRow(
                    label = "Hold",
                    nbPerfect = scoreEntry?.scoreDetail?.hold?.perfect.toString(),
                    nbGreat = scoreEntry?.scoreDetail?.hold?.great.toString(),
                    nbGood = scoreEntry?.scoreDetail?.hold?.good.toString(),
                    nbBad = scoreEntry?.scoreDetail?.hold?.bad.toString()
                )
                TableRow(
                    label = "Break",
                    nbPerfect = scoreEntry?.scoreDetail?.breakk?.perfect.toString(),
                    nbGreat = scoreEntry?.scoreDetail?.breakk?.great.toString(),
                    nbGood = scoreEntry?.scoreDetail?.breakk?.good.toString(),
                    nbBad = scoreEntry?.scoreDetail?.breakk?.bad.toString()
                )
                TableRow(
                    label = "Hits",
                    nbPerfect = scoreEntry?.scoreDetail?.hits?.perfect.toString(),
                    nbGreat = scoreEntry?.scoreDetail?.hits?.great.toString(),
                    nbGood = scoreEntry?.scoreDetail?.hits?.good.toString(),
                    nbBad = scoreEntry?.scoreDetail?.hits?.bad.toString()
                )
                TableRow(
                    label = "Slide",
                    nbPerfect = scoreEntry?.scoreDetail?.slide?.perfect.toString(),
                    nbGreat = scoreEntry?.scoreDetail?.slide?.great.toString(),
                    nbGood = scoreEntry?.scoreDetail?.slide?.good.toString(),
                    nbBad = scoreEntry?.scoreDetail?.slide?.bad.toString()
                )
            }
        }
    }
}

@Composable
fun TableRow(label: String = "", nbPerfect: String, nbGreat: String, nbGood: String, nbBad: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = nbPerfect,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = nbGreat,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = nbGood,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = nbBad,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}



