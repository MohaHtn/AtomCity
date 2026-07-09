package org.arcade.atomcity.ui.game.maimai

import android.content.Context
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


/**
 * Retrieves level information (level and internal_level) for a given song and difficulty.
 * @param context Android context
 * @param songId The song ID
 * @param difficultyValue The difficulty index (1=easy, 2=basic, 3=advanced, 4=expert, 5=master, 6=remaster)
 * @return A LevelInfo object containing the level and internal level, or null if not found
 */
suspend fun getMaimaiLevelInfo(
    context: Context,
    songId: Int,
    difficultyValue: Int
): LevelInfo? {
    return DifficultyRepository.getLevelByDifficulty(context, songId, difficultyValue)
}

@Composable
fun MaimaiDifficultyBadge(
    difficultyValue: String?,
    levelInfo: LevelInfo?,
    modifier: Modifier = Modifier,
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
        val levels = listOfNotNull(level, internal).joinToString(" | ")

        // Adding the difficulty name at the end
        if (levels.isNotEmpty()) "$difficultyText $levels" else difficultyText
    }

    Surface(
        color = difficultyColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Text(
            text = displayText,
            style = textStyle,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}
