package org.arcade.atomcity.model.maitea.playsResponse

import com.squareup.moshi.Json


data class DifficultyLevel (

  @Json(name = "key"   ) var key   : Int?    = null,
  @Json(name = "value" ) var value : String? = null,
  @Json(name = "label" ) var label : String? = null

)