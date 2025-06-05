package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json

data class Name (

  @Json(name = "en" ) var en : String? = null,
  @Json(name = "jp" ) var jp : String? = null

)