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
  @SerialName( "score") var score: Double? = null,
  @SerialName( "score_formatted") var scoreFormatted: String? = null,
  @SerialName( "max_score") var maxScore: Double? = null,
  @SerialName( "max_score_formatted") var maxScoreFormatted: String? = null,
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

    val scoreFormattedFixed: String? get() {
        return scoreFormatted ?: score?.let { formatScoreValue(it) }
    }

    val maxScoreFormattedFixed: String? get() {
        return maxScoreFormatted ?: maxScore?.let { formatScoreValue(it) }
    }

    val theoreticalMaxScore: Double? get() {
        val detail = scoreDetail ?: return null
        val tapCount = (detail.tap?.perfect ?: 0) + (detail.tap?.great ?: 0) +
                (detail.tap?.good ?: 0) + (detail.tap?.bad ?: 0)
        val holdCount = (detail.hold?.perfect ?: 0) + (detail.hold?.great ?: 0) +
                (detail.hold?.good ?: 0) + (detail.hold?.bad ?: 0)
        val slideCount = (detail.slide?.perfect ?: 0) + (detail.slide?.great ?: 0) +
                (detail.slide?.good ?: 0) + (detail.slide?.bad ?: 0)
        val breakCount = (detail.breakk?.perfect ?: 0) + (detail.breakk?.great ?: 0) +
                (detail.breakk?.good ?: 0) + (detail.breakk?.bad ?: 0)

        val totalNotes = tapCount + holdCount + slideCount + breakCount
        if (totalNotes == 0) return null

        return (tapCount * 500) + (holdCount * 1000) + (slideCount * 1500) + (breakCount * 2500 * 1.04)
    }

    val theoreticalMaxScoreFormatted: String? get() {
        return theoreticalMaxScore?.let { formatScoreValue(it) }
    }

    val theoreticalMaxPercent: Double? get() {
        val sd = scoreDetail ?: return null
        fun sumCounts(vararg counts: Int?): Int = counts.filterNotNull().sum()

        val taps = sd.tap?.let { sumCounts(it.perfect, it.great, it.good, it.bad) } ?: 0
        val holds = sd.hold?.let { sumCounts(it.perfect, it.great, it.good, it.bad) } ?: 0
        val slides = sd.slide?.let { sumCounts(it.perfect, it.great, it.good, it.bad) } ?: 0
        val breaks = sd.breakk?.let { sumCounts(it.perfect, it.great, it.good, it.bad) } ?: 0

        val totalWithoutBonus = taps * 500.0 + holds * 1000.0 + slides * 1500.0 + breaks * 2500.0
        if (totalWithoutBonus <= 0.0) return null

        val breakBonus = breaks * 2500.0 * 0.04
        val totalWithBonus = totalWithoutBonus + breakBonus

        val percent = (totalWithBonus / totalWithoutBonus) * 100.0 - 0.0045
        return kotlin.math.round(percent * 100) / 100.0
    }
}

fun formatScoreValue(value: Double): String {
    return if (value % 1.0 == 0.0) {
        val longVal = value.toLong()
        val str = longVal.toString()
        val builder = StringBuilder()
        val len = str.length
        for (i in 0 until len) {
            if (i > 0 && ((len - i) % 3 == 0)) {
                builder.append(',')
            }
            builder.append(str[i])
        }
        builder.toString()
    } else {
        value.format(2)
    }
}
