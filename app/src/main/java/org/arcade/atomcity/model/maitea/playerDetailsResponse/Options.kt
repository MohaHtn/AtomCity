package org.arcade.atomcity.model.maitea.playerDetailsResponse

  import com.squareup.moshi.Json

  data class Options (
    @Json(name = "icon"      ) var icon      : Icon?      = Icon(),
    @Json(name = "icon_deka" ) var iconDeka  : IconDeka?    = null,
    @Json(name = "nameplate" ) var nameplate : Nameplate? = Nameplate(),
    @Json(name = "frame"     ) var frame     : Frame?     = Frame(),
    @Json(name = "title"     ) var title     : Title?    = null
  )


data class Title(
    @Json(name = "id") var id: Int?,
    @Json(name = "value") var value: String?
)