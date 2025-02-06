package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json

data class Latest (

  @Json(name = "id"        ) var id       : Int?    = null,
  @Json(name = "date"      ) var date     : String? = null,
  @Json(name = "date_unix" ) var dateUnix : Int?    = null

)