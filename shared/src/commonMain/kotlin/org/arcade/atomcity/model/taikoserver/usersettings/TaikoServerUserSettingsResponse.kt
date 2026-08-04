package org.arcade.atomcity.model.taikoserver.usersettings

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerUserSettingsResponse(
    val myDonName: String? = null,
    val title: String? = null,
    val kigurumi: Int? = null,
    val head: Int? = null,
    val body: Int? = null,
    val face: Int? = null,
    val puchi: Int? = null
)
