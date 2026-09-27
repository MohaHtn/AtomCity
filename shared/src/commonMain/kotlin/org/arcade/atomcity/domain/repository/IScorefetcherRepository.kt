/**
 * Scorefetcher Repository Interface
 *
 * Contract defining data operations for fetching Maimai player statistics, scores, analytics, and managing API keys.
 */
package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry
import org.arcade.atomcity.data.remote.model.scorefetcher.RankProgressionResponse
import org.arcade.atomcity.data.remote.DeleteApiKeyResponse
import org.arcade.atomcity.data.remote.TaikoUser

/**
 * Interface defining the data operations for the Scorefetcher API.
 * 
 * It acts as the central data source for fetching Maimai player statistics,
 * importing scores, fetching analytics, and managing API keys.
 * This interface ensures adherence to the Dependency Inversion Principle (DIP), 
 * keeping the domain layer decoupled from external network and database implementations.
 */
interface IScorefetcherRepository {
    /**
     * Finds the jacket image URL associated with a specific song name.
     *
     * @param songName The name of the song.
     * @return The URL string of the jacket image, or null if not found.
     */
    fun findJacketUrlBySongName(songName: String?): String?

    /**
     * Observes the status of the background score import worker.
     *
     * @return A [Flow] emitting the current [ImportWorkerStatus], or null if inactive.
     */
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?>

    /**
     * Refreshes and retrieves the current import worker status remotely.
     *
     * @return The refreshed [ImportWorkerStatus].
     */
    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus

    /**
     * Sets the active state of the import worker.
     *
     * @param active True to mark the worker as active, false otherwise.
     */
    fun setImportWorkerActive(active: Boolean)

    /**
     * Checks if the import worker is currently active.
     *
     * @return A [Flow] emitting true if the worker is active.
     */
    fun isImportWorkerActive(): Flow<Boolean>

    /**
     * Clears the cached paginated data for the Scorefetcher responses.
     */
    fun clearScorefetcherPaginatedCache()

    /**
     * Initiates the score import process.
     *
     * @return True if the import started successfully, false otherwise.
     */
    suspend fun startScorefetcherImport(): Boolean

    /**
     * Retrieves a specific page of the paginated score data.
     *
     * @param page The page number to retrieve.
     * @return A [Flow] emitting the [ScorefetcherPlaysResponse] for the requested page.
     */
    fun getScorefetcherPaginatedData(page: Int): Flow<ScorefetcherPlaysResponse?>

    /**
     * Retrieves the details of the player associated with the current API key.
     *
     * @return A [Flow] emitting the [ScorefetcherPlayerDetailsResponse].
     */
    fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?>

    /**
     * Retrieves the mapping of player profiles.
     *
     * @return A [Flow] emitting a map of user key hashes to their display names.
     */
    fun getProfiles(): Flow<Map<String, String?>>

    /**
     * Retrieves the ratings of various players.
     *
     * @return A [Flow] emitting a map of user key hashes to their ratings.
     */
    fun getRatings(): Flow<Map<String, Int?>>

    /**
     * Retrieves the top 30 standard charts for a given player.
     *
     * @param hashKey The hashed identifier of the player. If null, the current user is assumed.
     * @return A [Flow] emitting a list of [PlayerBest30Response].
     */
    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>>

    /**
     * Retrieves the top Utage scores for a given player.
     *
     * @param hashKey The hashed identifier of the player. If null, the current user is assumed.
     * @return A [Flow] emitting a list of [PlayerBest30Response].
     */
    fun getTopUtageScores(hashKey: String? = null): Flow<List<PlayerBest30Response>>

    /**
     * Retrieves the historical chart data for a specific song and difficulty.
     *
     * @param songName The name of the song.
     * @param difficulty The difficulty level (optional).
     * @return A [Flow] emitting a list of [ChartHistoryResponse].
     */
    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>>

    /**
     * Retrieves the best score per player for a specific song and difficulty.
     *
     * @param songName The name of the song.
     * @param difficulty The difficulty level (optional).
     * @return A [Flow] emitting a list of [BestPerPlayerResponse].
     */
    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>>

    /**
     * Retrieves the details of a specific play by its ID.
     *
     * @param id The unique identifier of the play.
     * @param keyHash The hashed identifier of the player.
     * @return A [Flow] emitting the [ScorefetcherApiData], or null if not found.
     */
    fun getPlayById(id: Int, keyHash: String): Flow<ScorefetcherApiData?>

    /**
     * Searches for charts based on various filters.
     *
     * @param query The search term.
     * @param keyHash The hashed identifier of the player.
     * @param rank The rank filter.
     * @param source The source filter.
     * @return A [Flow] emitting a list of [BestPerPlayerResponse] matching the criteria.
     */
    fun searchCharts(query: String = "", keyHash: String? = null, rank: String? = null, source: String? = null): Flow<List<BestPerPlayerResponse>>

    /**
     * Retrieves the most played charts globally.
     *
     * @param limit The maximum number of entries to return.
     * @param period The time period (e.g., "day", "week", "month").
     * @param date A specific date to query.
     * @param groupByHashkey Whether to group the results by hash key.
     * @return A [Flow] emitting a list of [MaimaiMostPlayedEntry].
     */
    fun getMostPlayed(limit: Int? = 30, period: String? = "month", date: String? = null, groupByHashkey: Boolean = false): Flow<List<MaimaiMostPlayedEntry>>

    /**
     * Retrieves the most played charts for a specific player.
     *
     * @param keyHash The hashed identifier of the player.
     * @param limit The maximum number of entries to return.
     * @param period The time period.
     * @param date A specific date to query.
     * @param groupByHashkey Whether to group the results by hash key.
     * @return A [Flow] emitting a list of [MaimaiMostPlayedEntry].
     */
    fun getMostPlayedByHash(keyHash: String? = null, limit: Int? = 30, period: String? = "month", date: String? = null, groupByHashkey: Boolean = false): Flow<List<MaimaiMostPlayedEntry>>

    /**
     * Retrieves the rank progression data.
     *
     * @param targetKeyHash The specific player's hash key.
     * @return A [Flow] emitting the [RankProgressionResponse].
     */
    fun getRankProgression(targetKeyHash: String? = null): Flow<RankProgressionResponse>

    /**
     * Updates the public visibility of the player's progression.
     *
     * @param isPublic True to make it public, false to make it private.
     * @return True if successful, false otherwise.
     */
    suspend fun updateProgressionVisibility(isPublic: Boolean): Boolean

    /**
     * Removes an API key from the remote server.
     *
     * @param apiKey The API key to remove.
     * @return A [Flow] emitting the deletion response.
     */
    suspend fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse>

    /**
     * Adds a Taiko user profile based on their BAID.
     *
     * @param baid The Bandai Namco ID.
     * @return True if the addition was successful or the user already exists.
     */
    suspend fun addTaikoUser(baid: Int): Boolean

    /**
     * Retrieves the list of associated Taiko users.
     *
     * @return A [Flow] emitting the list of Taiko users.
     */
    fun getTaikoUsers(): Flow<List<TaikoUser>>
}
