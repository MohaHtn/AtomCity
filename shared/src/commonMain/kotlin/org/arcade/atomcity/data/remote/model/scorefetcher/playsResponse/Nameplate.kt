package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Nameplate (

  @SerialName( "id"   ) var id   : Int?    = null,
  @SerialName( "png"  ) var png  : String? = null,
  @SerialName( "webp" ) var webp : String? = null

)
