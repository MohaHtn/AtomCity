package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.ScorefetcherRepository
import org.arcade.atomcity.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.model.scorefetcher.BestPerPlayerResponse

class GetScorefetcherScoresUseCase(private val repository: ScorefetcherRepository) {
    fun getScorefetcherPaginatedData(page: Int): Flow<ScorefetcherPlaysResponse?> = repository.getScorefetcherPaginatedData(page)

    fun getPlayById(id: Int, keyHash: String): Flow<ScorefetcherApiData?> = repository.getPlayById(id, keyHash)

    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>> = repository.get30BestCharts(hashKey)

    fun searchCharts(query: String, keyHash: String? = null): Flow<List<BestPerPlayerResponse>> = repository.searchCharts(query, keyHash)
}
