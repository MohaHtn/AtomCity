package org.arcade.atomcity.model.maitea.playerDetailsResponse

  import com.squareup.moshi.Json

  data class Data (
    @Json(name = "id"             ) var id            : Int?       = null,
    @Json(name = "name"           ) var name          : String?    = null,
    @Json(name = "rating"         ) var rating        : Int?       = null,
    @Json(name = "rating_highest" ) var ratingHighest : Int?       = null,
    @Json(name = "level"          ) var level         : Int?       = null,
    @Json(name = "play_stats"     ) var playStats     : PlayStats? = PlayStats(),
    @Json(name = "options"        ) var options       : Options?   = Options()
  )