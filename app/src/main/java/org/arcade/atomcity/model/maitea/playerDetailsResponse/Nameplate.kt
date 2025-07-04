package org.arcade.atomcity.model.maitea.playerDetailsResponse

  import com.squareup.moshi.Json

  data class Nameplate (
    @Json(name = "id"   ) var id   : Int?    = null,
    @Json(name = "png"  ) var png  : String? = null,
    @Json(name = "webp" ) var webp : String? = null
  )