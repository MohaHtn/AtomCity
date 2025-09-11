package org.arcade.atomcity.ui.game.common

import androidx.compose.ui.graphics.Color

fun selectRatingBackgroundColor(rating: Int?): Color {
    return when (rating) {
        null -> Color.Transparent
        in 0..200 -> Color.White
        in 200..399 -> Color.Blue
        in 400..699 -> Color.Green
        in 700..999 -> Color.Cyan
        in 1000..1199 -> Color.Red
        in 1200..1299 -> Color.Magenta
        in 1300..1399 -> Color(0xFFA52A2A) // Brown
        in 1400..1449 -> Color.Gray
        in 1450..1499 -> Color.Yellow
        else -> Color.Black
    }
}