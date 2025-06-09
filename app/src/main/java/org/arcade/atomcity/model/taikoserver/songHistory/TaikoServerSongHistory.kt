package org.arcade.atomcity.model.taikoserver.songHistory

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaikoServerSongHistory (

  @Json(name = "songId")        var songId        : Int?     = null,
  @Json(name = "genre")         var genre         : Int?     = null,
  @Json(name = "musicName")     var musicName     : String?  = null,
  @Json(name = "musicArtist")   var musicArtist   : String?  = null,
  @Json(name = "difficulty")    var difficulty    : Int?     = null,
  @Json(name = "stars")         var stars         : Int?     = null,
  @Json(name = "showDetails")   var showDetails   : Boolean? = null,
  @Json(name = "score")         var score         : Int?     = null,
  @Json(name = "crown")         var crown         : Int?     = null,
  @Json(name = "scoreRank")     var scoreRank     : Int?     = null,
  @Json(name = "playTime")      var playTime      : String?  = null,
  @Json(name = "isFavorite")    var isFavorite    : Boolean? = null,
  @Json(name = "goodCount")     var goodCount     : Int?     = null,
  @Json(name = "okCount")       var okCount       : Int?     = null,
  @Json(name = "missCount")     var missCount     : Int?     = null,
  @Json(name = "comboCount")    var comboCount    : Int?     = null,
  @Json(name = "hitCount")      var hitCount      : Int?     = null,
  @Json(name = "drumrollCount") var drumrollCount : Int?     = null,
  @Json(name = "songNumber")    var songNumber    : Int?     = null

)