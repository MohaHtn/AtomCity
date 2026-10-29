package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.Serializable
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.DifficultyLevel
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.Song
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScoreDetail
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.FlexibleDoubleSerializer

@Serializable
data class SessionScoreItem(
    val playId: Int? = null,
    val keyHash: String? = null,
    val songName: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val achievement: Double? = null,
    val playDate: String? = null,
    val difficultyLevel: String? = null,
    val difficultyLevelJson: DifficultyLevel? = null,
    val rank: String? = null,
    val rating: Double? = null,
    val playerName: String? = null,
    val songJson: Song? = null,
    val full_combo: Int? = null,
    val is_all_perfect: Boolean? = null,
    val score_detail: ScoreDetail? = null,
    var jacketImageUrl: String? = null
) {
    fun toScorefetcherApiData(): ScorefetcherApiData {
        return ScorefetcherApiData(
            id = playId,
            achievement = achievement,
            scoreDetail = score_detail,
            fullCombo = full_combo,
            isAllPerfect = is_all_perfect,
            difficultyLevel = difficultyLevelJson,
            playDate = playDate,
            song = songJson,
            rank = rank,
            rating = rating,
            jacketImageUrl = jacketImageUrl
        )
    }
}

typealias Score = SessionScoreItem

@Serializable
data class SessionPbs(
    val sessionDate: String = "",
    val pbCount: Int = 0,
    val pbs: List<Score> = emptyList()
)
