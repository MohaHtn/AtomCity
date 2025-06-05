package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json

data class First (

  @Json(name = "id"        ) var id       : Int?    = null,
  @Json(name = "date"      ) var date     : String? = null,
  @Json(name = "date_unix" ) var dateUnix : Int?    = null

)