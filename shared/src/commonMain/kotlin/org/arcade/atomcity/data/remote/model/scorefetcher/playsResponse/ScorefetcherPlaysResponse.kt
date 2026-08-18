package org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherPlaysResponse(
    val data: List<ScorefetcherApiData>
)
