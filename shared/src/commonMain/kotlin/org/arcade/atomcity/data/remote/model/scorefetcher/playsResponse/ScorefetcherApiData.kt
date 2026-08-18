package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import org.arcade.atomcity.utils.format

@Serializable
data class ScorefetcherApiData (
  @SerialName( "id") var id: Int? = null,
  @SerialName( "achievement") var achievement: Int? = null,
  @SerialName( "achievement_formatted") var achievementFormatted: String? = null,
  @SerialName( "track") var track: Int? = null,
  @SerialName( "score") var score: Int? = null,
  @SerialName( "score_formatted") var scoreFormatted: String? = null,
  @SerialName( "score_detail") var scoreDetail: ScoreDetail? = ScoreDetail(),
  @SerialName( "full_combo") var fullCombo: Int? = null,
  @SerialName( "full_combo_label") var fullComboLabel: String? = null,
  @SerialName( "is_high_score") var isHighScore: Boolean? = null,
  @SerialName( "is_all_perfect") var isAllPerfect: Boolean? = null,
  @SerialName( "is_track_skip") var isTrackSkip: Boolean? = null,
  @SerialName( "difficulty_level") var difficultyLevel: DifficultyLevel? = DifficultyLevel(),
  @SerialName( "play_date") var playDate: String? = null,
  @SerialName( "play_date_unix") var playDateUnix: Int? = null,
  @SerialName( "song") var song: Song? = Song(),
  @SerialName( "player") var player: Player? = Player(),
  @SerialName( "jacket_image_url") var jacketImageUrl: String? = null,
  @SerialName( "rank") var rank: String? = null,
  @SerialName("rating") var rating: Double? = null
) {
    val ratingFormatted: String? get() = rating?.format(2)

    val achievementFormattedFixed: String? get() {
        return achievement?.let {
            "${(it.toDouble() / 100.0).format(2)}%"
        } ?: achievementFormatted
    }
}
