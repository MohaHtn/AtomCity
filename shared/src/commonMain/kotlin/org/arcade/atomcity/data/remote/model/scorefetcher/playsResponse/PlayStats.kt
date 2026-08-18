package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class PlayStats (

  @SerialName( "total"  ) var total  : Int?    = null,
  @SerialName( "wins"   ) var wins   : Int?    = null,
  @SerialName( "vs"     ) var vs     : Int?    = null,
  @SerialName( "sync"   ) var sync   : Int?    = null,
  @SerialName( "first"  ) var first  : First?  = First(),
  @SerialName( "latest" ) var latest : Latest? = Latest()

)
