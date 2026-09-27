package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry
import org.arcade.atomcity.data.remote.model.scorefetcher.RankProgressionResponse

/**
 * Use case responsible for retrieving analytics and statistical data 
 * from the Scorefetcher repository.
 * 
 * It follows the Single Responsibility Principle (SRP) by isolating 
 * analytics-related operations (such as chart histories, most played charts, 
 * and rank progression) from other domain logic.
 *
 * @property repository The [IScorefetcherRepository] data source used for analytics.
 */
class GetScorefetcherAnalyticsUseCase(private val repository: IScorefetcherRepository) {

    /**
     * Retrieves the history of charts played for a given song.
     *
     * @param songName The name of the target song.
     * @param difficulty The specified difficulty level (optional).
     * @return A [Flow] emitting the list of [ChartHistoryResponse].
     */
    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>> = repository.getChartHistory(songName, difficulty)

    /**
     * Retrieves the best score achieved per player for a specific chart.
     *
     * @param songName The name of the target song.
     * @param difficulty The specified difficulty level (optional).
     * @return A [Flow] emitting the list of [BestPerPlayerResponse].
     */
    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>> = repository.getBestPerPlayer(songName, difficulty)

    /**
     * Retrieves the most played charts globally across all players.
     *
     * @param limit The maximum number of charts to return (default is 30).
     * @param period The timeframe for the query (e.g., "month").
     * @param date A specific date string to filter by (optional).
     * @param groupByHashkey Whether to group the result by the unique hash key.
     * @return A [Flow] emitting the list of [MaimaiMostPlayedEntry].
     */
    fun getMostPlayed(
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null,
        groupByHashkey: Boolean = false
    ): Flow<List<MaimaiMostPlayedEntry>> = repository.getMostPlayed(limit, period, date, groupByHashkey)

    /**
     * Retrieves the most played charts specific to a given user.
     *
     * @param keyHash The hash key identifying the user.
     * @param limit The maximum number of charts to return (default is 30).
     * @param period The timeframe for the query (e.g., "month").
     * @param date A specific date string to filter by (optional).
     * @param groupByHashkey Whether to group the result by the unique hash key.
     * @return A [Flow] emitting the list of [MaimaiMostPlayedEntry].
     */
    fun getMostPlayedByHash(
        keyHash: String? = null,
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null,
        groupByHashkey: Boolean = false
    ): Flow<List<MaimaiMostPlayedEntry>> = repository.getMostPlayedByHash(keyHash, limit, period, date, groupByHashkey)

    /**
     * Retrieves the progression history of a player's rank over time.
     *
     * @param targetKeyHash The hash key identifying the user.
     * @return A [Flow] emitting the [RankProgressionResponse].
     */
    fun getRankProgression(targetKeyHash: String? = null): Flow<RankProgressionResponse> = repository.getRankProgression(targetKeyHash)

    /**
     * Updates the public visibility status of the player's progression data.
     *
     * @param isPublic True to allow public viewing, false to hide it.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateProgressionVisibility(isPublic: Boolean): Boolean = repository.updateProgressionVisibility(isPublic)
}
