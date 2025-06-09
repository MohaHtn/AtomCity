package org.arcade.atomcity.model.maitea.playsResponse

import com.squareup.moshi.Json


data class Break (
  @Json(name = "perfect" ) var perfect : Int? = null,
  @Json(name = "great"   ) var great   : Int? = null,
  @Json(name = "good"    ) var good    : Int? = null,
  @Json(name = "bad"     ) var bad     : Int? = null
)