package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json

data class MaiTeaResponse (
    @Json(name = "data") var data: List<MaiteaApiData> = emptyList()
)
