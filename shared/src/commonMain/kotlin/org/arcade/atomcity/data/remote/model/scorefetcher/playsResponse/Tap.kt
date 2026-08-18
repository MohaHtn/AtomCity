package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Tap (

  @SerialName( "perfect" ) var perfect : Int? = null,
  @SerialName( "great"   ) var great   : Int? = null,
  @SerialName( "good"    ) var good    : Int? = null,
  @SerialName( "bad"     ) var bad     : Int? = null

)
