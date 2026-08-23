package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.ITaikoServerRepository
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class GetTaikoServerDataUseCase(private val repository: ITaikoServerRepository) {
    fun getDashboardFlow(): Flow<String?> =
        repository.getDashboardFlow()

    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> =
        repository.getPlayHistoryFlow(userNumber)

    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> =
        repository.getMusicDetailsFlow()

    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> =
        repository.getUserSettingsFlow(userNumber)

    fun getCostumesFlow(): Flow<TaikoServerCostumesResponse?> =
        repository.getCostumesFlow()

    fun getLockedCostumesFlow(): Flow<TaikoServerLockedCostumes?> =
        repository.getLockedCostumesFlow()

    fun getTitlesFlow(): Flow<TaikoServerTitlesResponse?> =
        repository.getTitlesFlow()

    fun getLockedTitlesFlow(): Flow<TaikoServerLockedTitles?> =
        repository.getLockedTitlesFlow()

    fun getPlayDataFlow(userNumber: String): Flow<String?> =
        repository.getPlayDataFlow(userNumber)

    fun getDanBestDataFlow(userNumber: String): Flow<TaikoServerDanBestDataResponse?> =
        repository.getDanBestDataFlow(userNumber)

    fun getUserFlow(userNumber: String): Flow<TaikoServerUserResponse?> =
        repository.getUserFlow(userNumber)

    fun getSongLeaderboardFlow(
        songId: String,
        baid: String,
        difficulty: Int,
        page: Int = 1,
        limit: Int = 10
    ): Flow<TaikoServerLeaderboardResponse?> =
        repository.getSongLeaderboardFlow(songId, baid, difficulty, page, limit)

    suspend fun login(loginRequest: Map<String, String>): TaikoServerAuthResponse =
        repository.login(loginRequest)

    suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse =
        repository.changePassword(passwordRequest)
}
