package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Options (

  @SerialName( "icon"      ) var icon      : Icon?      = Icon(),
  @SerialName( "icon_deka" ) var iconDeka  : IconDeka?    = IconDeka(),
  @SerialName( "nameplate" ) var nameplate : Nameplate? = Nameplate(),
  @SerialName( "frame"     ) var frame     : Frame?     = Frame()

)
