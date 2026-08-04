package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Artist (
  @SerialName( "en" ) var en : String? = null,
  @SerialName( "jp" ) var jp : String? = null

)