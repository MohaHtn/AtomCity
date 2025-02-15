package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json


data class ScoreDetail (

  @Json(name = "hits"  ) var hits  : Hits?  = Hits(),
  @Json(name = "tap"   ) var tap   : Tap?   = Tap(),
  @Json(name = "hold"  ) var hold  : Hold?  = Hold(),
  @Json(name = "slide" ) var slide : Slide? = Slide(),
  @Json(name = "break" ) var breakk : Break? = Break()

)