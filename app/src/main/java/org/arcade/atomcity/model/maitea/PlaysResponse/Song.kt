package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json


data class Song (

  @Json(name = "id"     ) var id     : Int?    = null,
  @Json(name = "code"   ) var code   : String? = null,
  @Json(name = "name"   ) var name   : Name?   = Name(),
  @Json(name = "artist" ) var artist : Artist? = Artist()

)