package org.arcade.atomcity.model.maitea.playerDetailsResponse

import com.squareup.moshi.Json

data class MaiteaPlayerDetailsResponse(
    @Json(name = "data") var data: List<MaiteaPlayerDetailsResponse> = emptyList()
)
