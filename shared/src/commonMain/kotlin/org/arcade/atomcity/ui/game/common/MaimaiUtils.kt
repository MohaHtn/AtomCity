package org.arcade.atomcity.ui.game.common

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

fun selectRatingBackground(rating: Int?): Brush {
    return when (rating) {
        null -> SolidColor(Color.Transparent)
        in 0..200 -> SolidColor(Color.White)
        in 200..399 -> SolidColor(Color.Blue)
        in 400..699 -> SolidColor(Color.Green)
        in 700..999 -> SolidColor(Color.Cyan)
        in 1000..1199 -> SolidColor(Color.Red)
        in 1200..1299 -> SolidColor(Color.Magenta)
        in 1300..1399 -> SolidColor(Color(0xFFA52A2A)) // Brown
        in 1400..1449 -> SolidColor(Color.Gray)
        in 1450..1499 -> SolidColor(Color.Yellow)
        else -> Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFFF8C00), // Orange
                Color(0xFFFF0080), // Pink
                Color(0xFF8000FF), // Purple
                Color(0xFF0080FF), // Blue
                Color(0xFF00FF80)  // Green/Teal
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
}

@Composable
fun getDifficultyColorBackground(difficulty: String?): CardColors {
    return when (difficulty) {
        "easy" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFE1F5FE))
        "basic" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFF1F8E9))
        "advanced" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF8E1))
        "expert" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFEBEE))
        "master" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3E5F5))
        "remaster" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFFCE4EC))
        "utage" -> CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3E5F5))
        else -> CardDefaults.elevatedCardColors()
    }
}

fun getJacketBorderColor(difficulty: String?): Color {
    return when (difficulty?.replace(":", "")?.trim()?.lowercase()) {
        "easy" -> Color(0xFF03A9F4) // Deep Light Blue
        "basic" -> Color(0xFF4CAF50) // Strong Green
        "advanced" -> Color(0xFFFBC02D) // Strong Yellow
        "expert" -> Color(0xffff2e42) // Strong Red
        "master" -> Color(0xFF9C27B0) // Deep Purple
        "remaster" -> Color(0xFFD172ED) // Pink
        "utage" -> Color(0xFFFF5722) // Orange
        "宴" -> Color(0xFFFF5722) // Orange
        else -> Color.Transparent
    }
}

fun getDifficultyIndex(difficulty: String?): Int {
    return when (val clean = difficulty?.replace(":", "")?.trim()?.lowercase()) {
        "basic" -> 0
        "advanced" -> 1
        "expert" -> 2
        "master" -> 3
        "remaster" -> 4
        "utage" -> 5
        "宴" -> 5
        else -> clean?.toIntOrNull() ?: 0
    }
}

@Composable
fun rememberMaimaiLevel(
    songId: Int,
    diffIndex: Int,
    songTitle: String?,
    repository: org.arcade.atomcity.domain.repository.IDifficultyRepository
): String {
    val levelInfo by produceState<String?>(initialValue = null, songId, diffIndex, songTitle, repository) {
        val result = repository.getLevelByDifficulty(
            songId = songId,
            diffIndex = diffIndex,
            songTitle = songTitle
        )
        value = result.level
    }
    return levelInfo ?: ""
}
