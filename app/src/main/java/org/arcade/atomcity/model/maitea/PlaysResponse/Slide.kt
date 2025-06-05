package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json

data class Slide (

  @Json(name = "perfect" ) var perfect : Int? = null,
  @Json(name = "great"   ) var great   : Int? = null,
  @Json(name = "good"    ) var good    : Int? = null,
  @Json(name = "bad"     ) var bad     : Int? = null

)