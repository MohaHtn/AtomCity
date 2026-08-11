package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable

@Serializable
data class MaiteaPlaysResponse(
    val data: List<MaiteaApiData>
)
