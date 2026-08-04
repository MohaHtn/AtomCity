package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class Song (

  @SerialName( "id"     ) var id     : Int?    = null,
  @SerialName( "code"   ) var code   : String? = null,
  @SerialName( "name"   ) var name   : Name?   = Name(),
  @SerialName( "artist" ) var artist : Artist? = Artist()

)