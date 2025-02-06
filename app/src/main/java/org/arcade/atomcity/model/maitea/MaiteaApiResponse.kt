package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json


data class MaiteaApiResponse (
  @Json(name = "id") var id: Int? = null,
  @Json(name = "achievement") var achievement: Int? = null,
  @Json(name = "achievement_formatted") var achievementFormatted: String? = null,
  @Json(name = "track") var track: Int? = null,
  @Json(name = "score") var score: Int? = null,
  @Json(name = "score_formatted") var scoreFormatted: String? = null,
  @Json(name = "score_detail") var scoreDetail: ScoreDetail? = ScoreDetail(),
  @Json(name = "full_combo") var fullCombo: Int? = null,
  @Json(name = "full_combo_label") var fullComboLabel: String? = null,
  @Json(name = "is_high_score") var isHighScore: Boolean? = null,
  @Json(name = "is_all_perfect") var isAllPerfect: Boolean? = null,
  @Json(name = "is_track_skip") var isTrackSkip: Boolean? = null,
  @Json(name = "difficulty_level") var difficultyLevel: DifficultyLevel? = DifficultyLevel(),
  @Json(name = "play_date") var playDate: String? = null,
  @Json(name = "play_date_unix") var playDateUnix: Int? = null,
  @Json(name = "song") var song: Song? = Song(),
  @Json(name = "player") var player: Player? = Player()
)