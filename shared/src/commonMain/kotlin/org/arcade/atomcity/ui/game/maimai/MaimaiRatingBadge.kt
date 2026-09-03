package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.ui.core.AutoResizedText
import org.arcade.atomcity.ui.game.common.selectRatingBackground
import org.arcade.atomcity.utils.format

fun computeRating(rating: Int?): String {
    return rating?.let { (it.toDouble() / 100.0).format(2) } ?: "0.00"
}

fun getContrastingColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}

@Composable
fun MaimaiRatingBadge(
    rating: Int?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp
) {
    val ratingBackground = selectRatingBackground(rating)
    val ratingTextColor = remember(ratingBackground) {
        if (ratingBackground is SolidColor) {
            getContrastingColor(ratingBackground.value)
        } else {
            Color.White
        }
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .background(ratingBackground, RoundedCornerShape(4.dp)),
        color = Color.Transparent,
        tonalElevation = 4.dp
    ) {
        AutoResizedText(
            text = computeRating(rating),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                letterSpacing = 0.sp
            ),
            color = ratingTextColor,
            minFontSize = 8.sp,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}
