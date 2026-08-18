package org.arcade.atomcity.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Icon (

  @SerialName( "id"   ) var id   : Int?    = null,
  @SerialName( "png"  ) var png  : String? = null,
  @SerialName( "webp" ) var webp : String? = null

)