package org.arcade.atomcity.model.maitea

import kotlinx.serialization.Serializable

@Serializable
data class MaiteaDataResponse<T>(
    val data: T
)
