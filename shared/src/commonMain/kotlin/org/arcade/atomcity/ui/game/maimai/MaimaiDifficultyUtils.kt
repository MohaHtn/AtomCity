package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.LevelInfo
import org.arcade.atomcity.ui.game.common.getJacketBorderColor

/**
 * Utilities for handling maimai difficulties
 */

/**
 * Formats the difficulty names in maimai.
 *
 * @param difficultyValue the difficulty value taken from maitea API response.
 */
fun getDifficultyText(difficultyValue: String?): String {
    return when (difficultyValue?.lowercase()) {
        "master" -> "Master"
        "advanced" -> "Advanced"
        "remaster", "re:master" -> "Re:Master"
        "basic" -> "Basic"
        "easy" -> "Easy"
        "expert" -> "Expert"
        "utage", "宴" -> "宴 (Utage)"
        else -> "N/A"
    }
}

/**
 * Gets the difficulty index for maimai database lookups.
 *
 * @param difficultyValue the difficulty value taken from maitea API response.
 */
fun getDifficultyIndex(difficultyValue: String?): Int {
    return when (difficultyValue?.lowercase()) {
        "basic" -> 0
        "advanced" -> 1
        "expert" -> 2
        "master" -> 3
        "remaster", "re:master" -> 4
        "utage" -> 5
        // Handle numeric strings if the API returns them
        "0" -> 0
        "1" -> 1
        "2" -> 2
        "3" -> 3
        "4" -> 4
        "5" -> 5
        else -> -1
    }
}

@Composable
fun MaimaiDifficultyBadge(
    difficultyValue: String?,
    levelInfo: LevelInfo? = null,
    rating: String? = "",
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
    )
) {
    val difficultyColor = getJacketBorderColor(difficultyValue)
    val displayText = remember(difficultyValue, levelInfo) {
        val difficultyText = getDifficultyText(difficultyValue)
        val level = levelInfo?.level?.takeIf { it.isNotEmpty() }
        var internal = levelInfo?.internalLevel?.takeIf { it.isNotEmpty() }

        // Formatting internal level to always have at least one decimal if it's a number
        if (internal != null && !internal.contains(".") && internal.toDoubleOrNull() != null) {
            internal = "$internal.0"
        }

        // Creating level/internal level separator
        val levels = listOfNotNull(level, internal.takeIf { it != level }).joinToString(" • ")

        // Adding the difficulty name at the end
        if (levels.isNotEmpty()) "$difficultyText $levels" else difficultyText
    }


    Surface(
        color = difficultyColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(){
            Text(
                text = displayText,
                style = textStyle,
                modifier = Modifier.padding(
                    horizontal = if (isCompact) 8.dp else 12.dp,
                    vertical = if (isCompact) 2.dp else 4.dp
                )
            )
        }
    }

}
