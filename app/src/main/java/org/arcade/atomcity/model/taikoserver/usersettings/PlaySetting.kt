package org.arcade.atomcity.model.taikoserver.usersettings

import com.squareup.moshi.Json


data class PlaySetting (
  @Json(name = "speed") var speed: Int? = null,
  @Json(name = "isVanishOn") var isVanishOn: Boolean? = null,
  @Json(name = "isInverseOn") var isInverseOn: Boolean? = null,
  @Json(name = "randomType") var randomType: Int? = null
)