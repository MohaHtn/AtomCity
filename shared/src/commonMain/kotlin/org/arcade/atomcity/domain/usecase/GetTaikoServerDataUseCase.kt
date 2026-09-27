package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.ITaikoServerRepository
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.dan.*

/**
 * Use case responsible for managing and retrieving Taiko server game data, user settings, 
 * leaderboards, and authentication workflows.
 * 
 * Adheres to the Single Responsibility Principle (SRP) by encapsulating Taiko-specific domain operations.
 *
 * @property repository The [ITaikoServerRepository] data source.
 */
class GetTaikoServerDataUseCase(private val repository: ITaikoServerRepository) {
    /**
     * Retrieves the dashboard flow.
     *
     * @return A [Flow] emitting dashboard data as a string.
     */
    fun getDashboardFlow(): Flow<String?> =
        repository.getDashboardFlow()

    /**
     * Retrieves the Dan course data flow.
     *
     * @return A [Flow] emitting a list of [DanCourseData].
     */
    fun getDanDataFlow(): Flow<List<DanCourseData>> =
        repository.getDanDataFlow()

    /**
     * Retrieves the play history flow for a specific user.
     *
     * @param userNumber The identifier of the user.
     * @return A [Flow] emitting the [TaikoServerPlayHistoryResponse].
     */
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> =
        repository.getPlayHistoryFlow(userNumber)

    /**
     * Retrieves the music details flow.
     *
     * @return A [Flow] emitting the [TaikoServerMusicDetailsResponse].
     */
    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> =
        repository.getMusicDetailsFlow()

    /**
     * Retrieves the user settings flow for a specific user.
     *
     * @param userNumber The identifier of the user.
     * @return A [Flow] emitting the [TaikoServerUserSettingsResponse].
     */
    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> =
        repository.getUserSettingsFlow(userNumber)

    /**
     * Retrieves the costumes flow.
     *
     * @return A [Flow] emitting the [TaikoServerCostumesResponse].
     */
    fun getCostumesFlow(): Flow<TaikoServerCostumesResponse?> =
        repository.getCostumesFlow()

    /**
     * Retrieves the locked costumes flow.
     *
     * @return A [Flow] emitting the [TaikoServerLockedCostumes].
     */
    fun getLockedCostumesFlow(): Flow<TaikoServerLockedCostumes?> =
        repository.getLockedCostumesFlow()

    /**
     * Retrieves the titles flow.
     *
     * @return A [Flow] emitting the [TaikoServerTitlesResponse].
     */
    fun getTitlesFlow(): Flow<TaikoServerTitlesResponse?> =
        repository.getTitlesFlow()

    /**
     * Retrieves the locked titles flow.
     *
     * @return A [Flow] emitting the [TaikoServerLockedTitles].
     */
    fun getLockedTitlesFlow(): Flow<TaikoServerLockedTitles?> =
        repository.getLockedTitlesFlow()

    /**
     * Retrieves the play data flow for a specific user.
     *
     * @param userNumber The identifier of the user.
     * @return A [Flow] emitting play data as a string.
     */
    fun getPlayDataFlow(userNumber: String): Flow<String?> =
        repository.getPlayDataFlow(userNumber)

    /**
     * Retrieves the Dan best scores flow for a specific user.
     *
     * @param userNumber The identifier of the user.
     * @return A [Flow] emitting the [TaikoServerDanBestDataResponse].
     */
    fun getDanBestDataFlow(userNumber: String): Flow<TaikoServerDanBestDataResponse?> =
        repository.getDanBestDataFlow(userNumber)

    /**
     * Retrieves the user information flow for a specific user.
     *
     * @param userNumber The identifier of the user.
     * @return A [Flow] emitting the [TaikoServerUserResponse].
     */
    fun getUserFlow(userNumber: String): Flow<TaikoServerUserResponse?> =
        repository.getUserFlow(userNumber)

    /**
     * Retrieves the song leaderboard flow.
     *
     * @param songId The song identifier.
     * @param baid The Bandai Namco ID.
     * @param difficulty The difficulty level.
     * @param page The page number (default is 1).
     * @param limit The number of items per page (default is 10).
     * @return A [Flow] emitting the [TaikoServerLeaderboardResponse].
     */
    fun getSongLeaderboardFlow(
        songId: String,
        baid: String,
        difficulty: Int,
        page: Int = 1,
        limit: Int = 10
    ): Flow<TaikoServerLeaderboardResponse?> =
        repository.getSongLeaderboardFlow(songId, baid, difficulty, page, limit)

    /**
     * Authenticates a user on the Taiko server.
     *
     * @param loginRequest The login credentials request.
     * @return The [TaikoServerAuthResponse] containing authentication details.
     */
    suspend fun login(loginRequest: TaikoLoginRequest): TaikoServerAuthResponse =
        repository.login(loginRequest)

    /**
     * Updates user settings on the server.
     *
     * @param baid The Bandai Namco ID.
     * @param settings The updated [TaikoServerUserSettingsResponse].
     * @param authToken The authorization token.
     */
    suspend fun updateUserSettings(baid: Int, settings: TaikoServerUserSettingsResponse, authToken: String) =
        repository.updateUserSettings(baid, settings, authToken)

    /**
     * Changes the user password.
     *
     * @param passwordRequest A map containing password change details.
     * @return The [TaikoServerAuthResponse].
     */
    suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse =
        repository.changePassword(passwordRequest)

    /**
     * Posts a favorite song.
     *
     * @param request The [TaikoFavoriteSongRequest] details.
     * @param authToken The authorization token (optional).
     */
    suspend fun postFavoriteSong(request: TaikoFavoriteSongRequest, authToken: String? = null) =
        repository.postFavoriteSong(request, authToken)
}
