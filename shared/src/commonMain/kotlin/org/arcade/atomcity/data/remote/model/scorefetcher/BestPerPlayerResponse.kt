package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.Song
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.DifficultyLevel
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScoreDetail
import kotlin.math.round

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
    val playDate: String? = null,
    @SerialName("full_combo") val fullCombo: Int? = null,
    @SerialName("full_combo_label") val fullComboLabel: String? = null,
    @SerialName("is_all_perfect") val isAllPerfect: Boolean? = null,
    @SerialName("score_detail") val scoreDetail: ScoreDetail? = null,
    var jacketImageUrl: String? = null
) {


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
        return round(percent * 100) / 100.0
    }

    val fcApText: String? get() {
        if (isAllPerfect == true) {
            val isApPlus = when {
                fullComboLabel == "AP+" -> true
                fullComboLabel == "AP" -> false
                else -> {
                    val achPct = (achievement ?: 0.0) / 100.0
                    val maxPct = theoreticalMaxPercent
                    if (maxPct != null && achPct > 0.0) {
                        achPct >= (maxPct - 0.005)
                    } else {
                        false
                    }
                }
            }
            
            return if (isApPlus) "AP+" else "AP"
        }

        if (fullCombo != null && fullCombo > 0) {
            return if (fullCombo == 1) "FC" else "FC+"
        }
        
        val label = fullComboLabel?.trim()
        if (!label.isNullOrEmpty() && !label.equals("NONE", ignoreCase = true) && label != "0") {
            return label
        }

        return null
    }
}
