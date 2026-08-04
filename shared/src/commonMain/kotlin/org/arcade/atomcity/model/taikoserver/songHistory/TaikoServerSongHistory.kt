package org.arcade.atomcity.model.taikoserver.songHistory

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TaikoServerSongHistory (

  @SerialName( "songId")        var songId        : Int?     = null,
  @SerialName( "genre")         var genre         : Int?     = null,
  @SerialName( "musicName")     var musicName     : String?  = null,
  @SerialName( "musicArtist")   var musicArtist   : String?  = null,
  @SerialName( "difficulty")    var difficulty    : Int?     = null,
  @SerialName( "stars")         var stars         : Int?     = null,
  @SerialName( "showDetails")   var showDetails   : Boolean? = null,
  @SerialName( "score")         var score         : Int?     = null,
  @SerialName( "crown")         var crown         : Int?     = null,
  @SerialName( "scoreRank")     var scoreRank     : Int?     = null,
  @SerialName( "playTime")      var playTime      : String?  = null,
  @SerialName( "isFavorite")    var isFavorite    : Boolean? = null,
  @SerialName( "goodCount")     var goodCount     : Int?     = null,
  @SerialName( "okCount")       var okCount       : Int?     = null,
  @SerialName( "missCount")     var missCount     : Int?     = null,
  @SerialName( "comboCount")    var comboCount    : Int?     = null,
  @SerialName( "hitCount")      var hitCount      : Int?     = null,
  @SerialName( "drumrollCount") var drumrollCount : Int?     = null,
  @SerialName( "songNumber")    var songNumber    : Int?     = null

)