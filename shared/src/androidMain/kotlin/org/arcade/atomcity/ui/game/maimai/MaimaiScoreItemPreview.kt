package org.arcade.atomcity.ui.game.maimai

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.arcade.atomcity.model.maitea.playsResponse.*

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemMasterPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "master",
                    label = "Master",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemExpertPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "expert",
                    label = "Expert",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemUtagePreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "utage",
                    label = "Utage",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemEasyPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "easy",
                    label = "Easy",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemBasicPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "basic",
                    label = "Basic",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemReMasterPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "remaster",
                    label = "Re:Master",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaimaiScoreItemUnknownPreview(){
    MaterialTheme {
        MaimaiScoreItem(
            play = MaiteaApiData(
                id = 1,
                achievement = 100,
                achievementFormatted = "101.43",
                track = 1,
                song = Song(
                    id = 1,
                    name = Name(jp = "おう た あごうlえ", en = "Fous ta Cagoule"),
                    artist = Artist(jp = "あた ばぞおか ", en = "Fatal Bazooka")
                ),
                difficultyLevel = DifficultyLevel(
                    value = "",
                    label = "",
                    key = 3
                ),
                rank = "SSS+",
                isHighScore = true,
                rating = 15.0,
                jacketImageUrl = "https://example.com/jacket.jpg",
                playDate = "2024-01-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}