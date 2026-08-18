package org.arcade.atomcity.model.scorefetcher.playerDetailsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Latest (
  @SerialName( "id"        ) var id       : Int?    = null,
  @SerialName( "date"      ) var date     : String? = null,
  @SerialName( "date_unix" ) var dateUnix : Int?    = null
)