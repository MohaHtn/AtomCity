package org.arcade.atomcity.model.scorefetcher.playerBest30Response

import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.scorefetcher.playsResponse.Song
import org.arcade.atomcity.model.scorefetcher.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.scorefetcher.playsResponse.ScoreDetail

@Serializable
data class PlayerBest30Response(
    val playId: Int? = null,
    val songJson: Song? = null,
    val difficultyLevelJson: DifficultyLevel? = null,
    val achievement: Double? = null,
    val rank: String? = null,
    val rating: Double? = null,
    val playDate: String? = null,
    val isHighScore: Boolean? = null,
    val isAllPerfect: Boolean? = null,
    val isTrackSkip: Boolean? = null,
    val scoreDetail: ScoreDetail? = null,
    var jacketImageUrl: String? = null
)
