package org.arcade.atomcity.model.taikoserver.musicDetails

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaikoServerMusicDetails(
    @Json(name = "songId")       val songId       : Int?     = null,
    @Json(name = "index")        val index        : Int?     = null,
    @Json(name = "songName")     val songName     : String?  = null,
    @Json(name = "songNameEN")   val songNameEN   : String?  = null,
    @Json(name = "songNameCN")   val songNameCN   : String?  = null,
    @Json(name = "songNameKO")   val songNameKO   : String?  = null,
    @Json(name = "artistName")   val artistName   : String?  = null,
    @Json(name = "artistNameEN") val artistNameEN : String?  = null,
    @Json(name = "artistNameCN") val artistNameCN : String?  = null,
    @Json(name = "artistNameKO") val artistNameKO : String?  = null,
    @Json(name = "genre")        val genre        : Int?     = null,
    @Json(name = "starEasy")     val starEasy     : Int?     = null,
    @Json(name = "starNormal")   val starNormal   : Int?     = null,
    @Json(name = "starHard")     val starHard     : Int?     = null,
    @Json(name = "starOni")      val starOni      : Int?     = null,
    @Json(name = "starUra")      val starUra      : Int?     = null,
    @Json(name = "isFavorite")   val isFavorite   : Boolean? = null
)
