package org.arcade.atomcity.model.maitea.playerDetailsResponse

import kotlinx.serialization.Serializable

@Serializable
data class MaiteaPlayerDetailsResponse(
    val data: List<PlayerDetailsData>? = null
)
