package org.arcade.atomcity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.remote.TaikoServerClient
import org.arcade.atomcity.domain.repository.ITaikoServerRepository
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class TaikoServerRepository(private val client: TaikoServerClient) : ITaikoServerRepository {
    override fun getDashboardFlow(): Flow<String?> = flow {
        emit(client.getDashboard())
    }

    override fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> = flow {
        emit(client.getPlayHistory(userNumber))
    }

    override fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> = flow {
        emit(client.getMusicDetails())
    }

    override fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> = flow {
        emit(client.getUserSettings(userNumber))
    }

    override fun getCostumesFlow(): Flow<TaikoServerCostumesResponse?> = flow {
        emit(client.getCostumes())
    }

    override fun getLockedCostumesFlow(): Flow<TaikoServerLockedCostumes?> = flow {
        emit(client.getLockedCostumes())
    }

    override fun getTitlesFlow(): Flow<TaikoServerTitlesResponse?> = flow {
        emit(client.getTitles())
    }

    override fun getLockedTitlesFlow(): Flow<TaikoServerLockedTitles?> = flow {
        emit(client.getLockedTitles())
    }

    override fun getPlayDataFlow(userNumber: String): Flow<String?> = flow {
        emit(client.getPlayData(userNumber))
    }

    override fun getDanBestDataFlow(userNumber: String): Flow<TaikoServerDanBestDataResponse?> = flow {
        emit(client.getDanBestData(userNumber))
    }

    override fun getUserFlow(userNumber: String): Flow<TaikoServerUserResponse?> = flow {
        emit(client.getUser(userNumber))
    }

    override fun getSongLeaderboardFlow(
        songId: String,
        baid: String,
        difficulty: Int,
        page: Int,
        limit: Int
    ): Flow<TaikoServerLeaderboardResponse?> = flow {
        emit(client.getSongLeaderboard(songId, baid, difficulty, page, limit))
    }

    override suspend fun login(loginRequest: TaikoLoginRequest): TaikoServerAuthResponse =
        client.login(loginRequest)

    override suspend fun updateUserSettings(
        baid: Int,
        settings: TaikoServerUserSettingsResponse,
        authToken: String
    ) = client.updateUserSettings(baid, settings, authToken)

    override suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse =
        client.changePassword(passwordRequest)
}
