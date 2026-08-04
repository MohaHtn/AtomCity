package org.arcade.atomcity.model.taikoserver.musicDetails

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TaikoServerMusicDetails(
    @SerialName( "songId")       val songId       : Int?     = null,
    @SerialName( "index")        val index        : Int?     = null,
    @SerialName( "songName")     val songName     : String?  = null,
    @SerialName( "songNameEN")   val songNameEN   : String?  = null,
    @SerialName( "songNameCN")   val songNameCN   : String?  = null,
    @SerialName( "songNameKO")   val songNameKO   : String?  = null,
    @SerialName( "artistName")   val artistName   : String?  = null,
    @SerialName( "artistNameEN") val artistNameEN : String?  = null,
    @SerialName( "artistNameCN") val artistNameCN : String?  = null,
    @SerialName( "artistNameKO") val artistNameKO : String?  = null,
    @SerialName( "genre")        val genre        : Int?     = null,
    @SerialName( "starEasy")     val starEasy     : Int?     = null,
    @SerialName( "starNormal")   val starNormal   : Int?     = null,
    @SerialName( "starHard")     val starHard     : Int?     = null,
    @SerialName( "starOni")      val starOni      : Int?     = null,
    @SerialName( "starUra")      val starUra      : Int?     = null,
    @SerialName( "isFavorite")   val isFavorite   : Boolean? = null
)
