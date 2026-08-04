package org.arcade.atomcity.ui.game.maimai

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import org.arcade.atomcity.data.DifficultyRepository
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
    return when (difficultyValue) {
        "master" -> "Master"
        "advanced" -> "Advanced"
        "remaster" -> "Re:Master"
        "basic" -> "Basic"
        "expert" -> "Expert"
        "utage" -> "宴 (Utage)"
        else -> "N/A"
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
        val internal = levelInfo?.internalLevel?.takeIf { it.isNotEmpty() && it != level }

        // Creating level/internal level separator
        val levels = listOfNotNull(level, internal).joinToString(" • ")

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
