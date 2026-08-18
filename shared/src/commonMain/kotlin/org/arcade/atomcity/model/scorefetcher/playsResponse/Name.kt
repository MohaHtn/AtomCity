package org.arcade.atomcity.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Name (

  @SerialName( "en" ) var en : String? = null,
  @SerialName( "jp" ) var jp : String? = null

)