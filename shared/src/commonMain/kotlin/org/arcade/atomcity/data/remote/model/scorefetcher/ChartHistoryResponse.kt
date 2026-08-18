package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.Serializable
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.DifficultyLevel

@Serializable
data class ChartHistoryResponse(
    val playId: Int? = null,
    val playDate: String? = null,
    val achievement: Double? = null,
    val rank: String? = null,
    val rating: Double? = null,
    val ratingFormatted: String? = null,
    val difficultyLevel: String? = null,
    val difficultyLevelJson: DifficultyLevel? = null
)
