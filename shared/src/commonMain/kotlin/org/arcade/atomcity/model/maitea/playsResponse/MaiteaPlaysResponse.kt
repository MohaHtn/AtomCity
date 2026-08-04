package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MaiteaPlaysResponse (
    @SerialName( "data") var data: List<MaiteaApiData> = emptyList()
)
