package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.arcade.atomcity.model.maitea.playsResponse.Song

@JsonClass(generateAdapter = true)
data class MaimaiMostPlayedEntry(
    @Json(name = "songName") val songName: String?,
    @Json(name = "playCount") val playCount: Int,
    @Json(name = "difficulty") val difficulty: String?,
    @Json(name = "songJson") val songJson: Song? = null,
    var jacketImageUrl: String? = null
)
