package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse

/**
 * Use case responsible for retrieving score-related data, including paginated plays, 
 * best scores, Utage scores, and searching charts.
 * 
 * Adheres to the Single Responsibility Principle (SRP) by grouping score operations.
 *
 * @property repository The [IScorefetcherRepository] data source.
 */
class GetScorefetcherScoresUseCase(private val repository: IScorefetcherRepository) {
    /**
     * Retrieves a paginated list of play data.
     *
     * @param page The target page number.
     * @return A [Flow] emitting the [ScorefetcherPlaysResponse].
     */
    fun getScorefetcherPaginatedData(page: Int): Flow<ScorefetcherPlaysResponse?> = repository.getScorefetcherPaginatedData(page)

    /**
     * Retrieves a specific play entry by its ID and key hash.
     *
     * @param id The play ID.
     * @param keyHash The player's key hash.
     * @return A [Flow] emitting the [ScorefetcherApiData], or null if not found.
     */
    fun getPlayById(id: Int, keyHash: String): Flow<ScorefetcherApiData?> = repository.getPlayById(id, keyHash)

    /**
     * Retrieves the top 30 best charts for a player.
     *
     * @Optional hashKey The unique key hash of the player.
     * @return A [Flow] emitting a list of [PlayerBest30Response].
     */
    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>> = repository.get30BestCharts(hashKey)

    /**
     * Retrieves the top Utage scores for a player.
     *
     * @Optional hashKey The unique key hash of the player.
     * @return A [Flow] emitting a list of [PlayerBest30Response].
     */
    fun getTopUtageScores(hashKey: String? = null): Flow<List<PlayerBest30Response>> = repository.getTopUtageScores(hashKey)

    /**
     * Searches for charts based on query terms and filters.
     *
     * @param query The search query string.
     * @param keyHash The player's key hash filter.
     * @param rank The rank filter.
     * @param source The source filter.
     * @return A [Flow] emitting a list of matching [BestPerPlayerResponse].
     */
    fun searchCharts(query: String = "", keyHash: String? = null, rank: String? = null, source: String? = null): Flow<List<BestPerPlayerResponse>> = repository.searchCharts(query, keyHash, rank, source)
}
