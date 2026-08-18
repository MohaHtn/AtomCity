package org.arcade.atomcity.model.scorefetcher.playerDetailsResponse

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherPlayerDetailsResponse(
    val data: List<PlayerDetailsData>? = null
)
