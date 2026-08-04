package org.arcade.atomcity.model.maitea

import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.maitea.playsResponse.Song
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel

@Serializable
data class BestPerPlayerResponse(
    val playId: Int? = null,
    val playerName: String? = null,
    val songJson: Song? = null,
    val difficultyLevel: String? = null,
    val difficultyLevelJson: DifficultyLevel? = null,
    val achievement: Double? = null,
    val rank: String? = null,
    val rating: Double? = null,
    val ratingFormatted: String? = null,
    val playDate: String? = null
)
