package org.arcade.atomcity.model.maitea.playerDetailsResponse

  import com.squareup.moshi.Json

  data class Options (
    @Json(name = "icon"      ) var icon      : Icon?      = Icon(),
    @Json(name = "icon_deka" ) var iconDeka  : String?    = null,
    @Json(name = "nameplate" ) var nameplate : Nameplate? = Nameplate(),
    @Json(name = "frame"     ) var frame     : Frame?     = Frame()
  )