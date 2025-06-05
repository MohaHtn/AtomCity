package org.arcade.atomcity.model.maitea.PlaysResponse

import com.squareup.moshi.Json

data class MaiteaPlaysResponse (
    @Json(name = "data") var data: List<MaiteaApiData> = emptyList()
)
