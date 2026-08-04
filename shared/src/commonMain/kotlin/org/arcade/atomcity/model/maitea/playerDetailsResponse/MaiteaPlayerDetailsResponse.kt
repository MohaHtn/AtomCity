package org.arcade.atomcity.model.maitea.playerDetailsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MaiteaPlayerDetailsResponse(
    @SerialName( "data") var data: List<Data> = emptyList()
)
