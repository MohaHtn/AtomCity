package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.model.maitea.playsResponse.*

@Preview(showBackground = true, name = "All Perfect + (No break greats)")
@Composable
fun PreviewAllPerfectPlus() {
    MaterialTheme {
        MaimaiScoreBadgeRow(
            scoreEntry = MaiteaApiData(
                isAllPerfect = true,
                scoreDetail = ScoreDetail(
                    breakk = Break(perfect = 10, great = 0, good = 0, bad = 0)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "All Perfect (With break greats)")
@Composable
fun PreviewAllPerfect() {
    MaterialTheme {
        MaimaiScoreBadgeRow(
            scoreEntry = MaiteaApiData(
                isAllPerfect = true,
                scoreDetail = ScoreDetail(
                    breakk = Break(perfect = 8, great = 2, good = 0, bad = 0)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Full Combo Blue (With greats)")
@Composable
fun PreviewFullComboBlue() {
    MaterialTheme {
        MaimaiScoreBadgeRow(
            scoreEntry = MaiteaApiData(
                fullCombo = 1,
                scoreDetail = ScoreDetail(
                    hits = Hits(perfect = 100, great = 5, good = 0, bad = 0)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Full Combo Yellow (No greats)")
@Composable
fun PreviewFullComboYellow() {
    MaterialTheme {
        MaimaiScoreBadgeRow(
            scoreEntry = MaiteaApiData(
                fullCombo = 1,
                scoreDetail = ScoreDetail(
                    hits = Hits(perfect = 105, great = 0, good = 0, bad = 0)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Multiple Badges")
@Composable
fun PreviewMultipleBadges() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MaimaiScoreBadgeRow(
                scoreEntry = MaiteaApiData(
                    isHighScore = true,
                    isAllPerfect = true,
                    fullCombo = 1,
                    scoreDetail = ScoreDetail(
                        hits = Hits(great = 0),
                        breakk = Break(great = 0)
                    )
                )
            )
        }
    }
}
