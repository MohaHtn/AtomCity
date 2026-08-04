package org.arcade.atomcity.model.maitea.playerDetailsResponse

  import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

  @Serializable
  data class Options (
    @SerialName( "icon"      ) var icon      : Icon?      = Icon(),
    @SerialName( "icon_deka" ) var iconDeka  : IconDeka?    = null,
    @SerialName( "nameplate" ) var nameplate : Nameplate? = Nameplate(),
    @SerialName( "frame"     ) var frame     : Frame?     = Frame(),
    @SerialName( "title"     ) var title     : Title?    = null
  )


@Serializable
data class Title(
    @SerialName( "id") var id: Int?,
    @SerialName( "value") var value: String?
)