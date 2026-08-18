package org.arcade.atomcity.model.scorefetcher.playsResponse

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherPlaysResponse(
    val data: List<ScorefetcherApiData>
)
