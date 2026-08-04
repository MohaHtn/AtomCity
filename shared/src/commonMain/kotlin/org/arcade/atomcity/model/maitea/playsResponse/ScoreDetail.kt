package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class ScoreDetail (

  @SerialName( "hits"  ) var hits  : Hits?  = Hits(),
  @SerialName( "tap"   ) var tap   : Tap?   = Tap(),
  @SerialName( "hold"  ) var hold  : Hold?  = Hold(),
  @SerialName( "slide" ) var slide : Slide? = Slide(),
  @SerialName( "break" ) var breakk : Break? = Break()

)