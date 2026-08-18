package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class DifficultyLevel (

  @SerialName( "key"   ) var key   : Int?    = null,
  @SerialName( "value" ) var value : String? = null,
  @SerialName( "label" ) var label : String? = null

)
