package org.arcade.atomcity.model.maitea.playerBest30Response

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.maitea.playsResponse.Song

@Serializable
data class PlayerBest30Response(
    @SerialName("playId"                   ) var playId:           Int? = null,
    @SerialName("keyHash"                  ) var keyHash:          String? = null,
    @SerialName("songName"                 ) var songName:         String? = null,
    @SerialName("achievement"              ) var achievement:      Double? = null,
    @SerialName("playDate"                 ) var playDate:         String? = null,
    @SerialName("difficultyLevel"          ) var difficultyLevel:  String? = null,
    @SerialName("difficultyLevelJson"      ) var difficultyLevelJson: DifficultyLevel? = null,
    @SerialName("rank"                     ) var rank:             String? = null,
    @SerialName("rating"                   ) var rating:           Double? = null,
    @SerialName("playerName"               ) var playerName:       String? = null,
    @SerialName("songJson"                 ) var songJson:         Song? = null
) {
    val ratingFormatted: String? get() = rating?.toString()
}
