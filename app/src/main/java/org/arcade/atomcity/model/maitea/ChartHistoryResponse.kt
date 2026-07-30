package org.arcade.atomcity.model.maitea

import com.squareup.moshi.JsonClass
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.maitea.playsResponse.Song

@JsonClass(generateAdapter = true)
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
    val ratingFormatted: String? get() = rating?.let { String.format(java.util.Locale.US, "%.2f", it) }
}