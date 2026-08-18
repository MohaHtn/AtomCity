package org.arcade.atomcity.data.remote.model.taikoserver.usersettings

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PlaySetting (
  @SerialName( "speed") var speed: Int? = null,
  @SerialName( "isVanishOn") var isVanishOn: Boolean? = null,
  @SerialName( "isInverseOn") var isInverseOn: Boolean? = null,
  @SerialName( "randomType") var randomType: Int? = null
)
