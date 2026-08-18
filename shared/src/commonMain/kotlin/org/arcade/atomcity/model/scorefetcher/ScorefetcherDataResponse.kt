package org.arcade.atomcity.model.scorefetcher

import kotlinx.serialization.Serializable

@Serializable
data class ScorefetcherDataResponse<T>(
    val data: T
)
