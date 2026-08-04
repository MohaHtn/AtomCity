package org.arcade.atomcity.model.maitea

import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.maitea.playsResponse.Song

@Serializable
data class ChartHistoryResponse(
    val playId: Int?,
    val keyHash: String?,
    val songName: String?,
    val achievement: Double?,
    val playDate: String?,
    val difficultyLevel: String?,
    val difficultyLevelJson: DifficultyLevel?,
    val rank: String?,
    val rating: Double?,
    val playerName: String?,
    val songJson: Song?
) {
    val ratingFormatted: String? get() = rating?.toString()
}