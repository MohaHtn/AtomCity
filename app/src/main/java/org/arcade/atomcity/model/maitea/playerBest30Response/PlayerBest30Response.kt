package org.arcade.atomcity.model.maitea.playerBest30Response

import com.squareup.moshi.Json
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.maitea.playsResponse.Song

data class PlayerBest30Response(
    @Json(name = "playId"                   ) var playId:           Int? = null,
    @Json(name = "keyHash"                  ) var keyHash:          String? = null,
    @Json(name = "songName"                 ) var songName:         String? = null,
    @Json(name = "achievement"              ) var achievement:      Double? = null,
    @Json(name = "playDate"                 ) var playDate:         String? = null,
    @Json(name = "difficultyLevel"          ) var difficultyLevel:  String? = null,
    @Json(name = "difficultyLevelJson"      ) var difficultyLevelJson: DifficultyLevel? = null,
    @Json(name = "rank"                     ) var rank:             String? = null,
    @Json(name = "rating"                   ) var rating:           Double? = null,
    @Json(name = "playerName"               ) var playerName:       String? = null,
    @Json(name = "songJson"                 ) var songJson:         Song? = null
)
