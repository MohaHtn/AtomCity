package org.arcade.atomcity.model.maitea.playsResponse

import com.squareup.moshi.Json


data class PlayStats (

  @Json(name = "total"  ) var total  : Int?    = null,
  @Json(name = "wins"   ) var wins   : Int?    = null,
  @Json(name = "vs"     ) var vs     : Int?    = null,
  @Json(name = "sync"   ) var sync   : Int?    = null,
  @Json(name = "first"  ) var first  : First?  = First(),
  @Json(name = "latest" ) var latest : Latest? = Latest()

)