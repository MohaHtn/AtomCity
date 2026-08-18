package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherDataResponse<T>(
    val data: T
)
