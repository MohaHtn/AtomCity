package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json

data class Icon (

  @Json(name = "id"   ) var id   : Int?    = null,
  @Json(name = "png"  ) var png  : String? = null,
  @Json(name = "webp" ) var webp : String? = null

)