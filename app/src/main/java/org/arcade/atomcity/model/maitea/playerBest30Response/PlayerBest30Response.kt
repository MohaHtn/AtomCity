package org.arcade.atomcity.model.maitea.playerBest30Response

import com.squareup.moshi.Json

data class PlayerBest30Response(
    @Json(name = "keyHash"                  ) var keyHash:          String? = null,
    @Json(name = "songName"                 ) var songName:         String? = null,
    @Json(name = "achievement"              ) var achievement:      Double? = null,
    @Json(name = "playDate"                 ) var playDate:         String? = null,
    @Json(name = "difficulty"               ) var difficulty:       String? = null,
    @Json(name = "rank"                     ) var rank:             String? = null,
    @Json(name = "rating"                   ) var rating:           Double? = null,
    @Json(name = "playerName"               ) var playerName:       String? = null,
    @Json(name = "maiteaProfileId"          ) var maiteaProfileId:  String? = null,
    @Json(name = "maiteaProfileUrl"         ) var maiteaProfileUrl: String? = null
)
