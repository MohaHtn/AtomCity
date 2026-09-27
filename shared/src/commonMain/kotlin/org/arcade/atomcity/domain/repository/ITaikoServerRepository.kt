package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.dan.*

/**
 * Interface defining the data operations and network communications for the Taiko Server.
 * 
 * Adheres to the Dependency Inversion Principle (DIP), decoupling the Taiko domain and use cases
 * from the underlying API client implementations.
 */
interface ITaikoServerRepository {
    /**
     * Retrieves the dashboard flow.
     *
     * @return A [Flow] emitting dashboard data as a string.
     */
    fun getDashboardFlow(): Flow<String?>

    /**
     * Retrieves the Dan course data flow.
     *
     * @return A [Flow] emitting a list of [DanCourseData].
     */
    fun getDanDataFlow(): Flow<List<DanCourseData>>

    /**
     * Retrieves the play history flow for a specific user.
     *
     * @param userNumber The unique user number identifier.
     * @return A [Flow] emitting the [TaikoServerPlayHistoryResponse].
     */
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?>

    /**
     * Retrieves the music details flow.
     *
     * @return A [Flow] emitting the [TaikoServerMusicDetailsResponse].
     */
    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?>

    /**
     * Retrieves the user settings flow for a specific user.
     *
     * @param userNumber The unique user number identifier.
     * @return A [Flow] emitting the [TaikoServerUserSettingsResponse].
     */
    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?>

    /**
     * Retrieves the costumes flow.
     *
     * @return A [Flow] emitting the [TaikoServerCostumesResponse].
     */
    fun getCostumesFlow(): Flow<TaikoServerCostumesResponse?>

    /**
     * Retrieves the locked costumes flow.
     *
     * @return A [Flow] emitting the [TaikoServerLockedCostumes].
     */
    fun getLockedCostumesFlow(): Flow<TaikoServerLockedCostumes?>

    /**
     * Retrieves the titles flow.
     *
     * @return A [Flow] emitting the [TaikoServerTitlesResponse].
     */
    fun getTitlesFlow(): Flow<TaikoServerTitlesResponse?>

    /**
     * Retrieves the locked titles flow.
     *
     * @return A [Flow] emitting the [TaikoServerLockedTitles].
     */
    fun getLockedTitlesFlow(): Flow<TaikoServerLockedTitles?>

    /**
     * Retrieves the play data flow for a specific user.
     *
     * @param userNumber The unique user number identifier.
     * @return A [Flow] emitting play data as a string.
     */
    fun getPlayDataFlow(userNumber: String): Flow<String?>

    /**
     * Retrieves the Dan best scores flow for a specific user.
     *
     * @param userNumber The unique user number identifier.
     * @return A [Flow] emitting the [TaikoServerDanBestDataResponse].
     */
    fun getDanBestDataFlow(userNumber: String): Flow<TaikoServerDanBestDataResponse?>

    /**
     * Retrieves the user profile flow for a specific user.
     *
     * @param userNumber The unique user number identifier.
     * @return A [Flow] emitting the [TaikoServerUserResponse].
     */
    fun getUserFlow(userNumber: String): Flow<TaikoServerUserResponse?>

    /**
     * Retrieves the song leaderboard flow.
     *
     * @param songId The song identifier.
     * @param baid The Bandai Namco ID.
     * @param difficulty The difficulty level.
     * @param page The page number.
     * @param limit The number of items per page.
     * @return A [Flow] emitting the [TaikoServerLeaderboardResponse].
     */
    fun getSongLeaderboardFlow(songId: String, baid: String, difficulty: Int, page: Int, limit: Int): Flow<TaikoServerLeaderboardResponse?>

    /**
     * Authenticates a user with the Taiko server.
     *
     * @param loginRequest The login credentials.
     * @return The [TaikoServerAuthResponse].
     */
    suspend fun login(loginRequest: TaikoLoginRequest): TaikoServerAuthResponse

    /**
     * Updates user settings on the Taiko server.
     *
     * @param baid The Bandai Namco ID.
     * @param settings The settings to update.
     * @param authToken The authorization token.
     */
    suspend fun updateUserSettings(baid: Int, settings: TaikoServerUserSettingsResponse, authToken: String)

    /**
     * Changes the user password on the Taiko server.
     *
     * @param passwordRequest A map containing old and new password information.
     * @return The [TaikoServerAuthResponse].
     */
    suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse

    /**
     * Posts a favorite song to the Taiko server.
     *
     * @param request The favorite song request details.
     * @param authToken The authorization token (optional).
     */
    suspend fun postFavoriteSong(request: TaikoFavoriteSongRequest, authToken: String? = null)
}
