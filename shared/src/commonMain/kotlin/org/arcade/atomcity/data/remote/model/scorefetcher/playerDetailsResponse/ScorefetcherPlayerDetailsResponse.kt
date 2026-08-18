package org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherPlayerDetailsResponse(
    val data: List<PlayerDetailsData>? = null
)
