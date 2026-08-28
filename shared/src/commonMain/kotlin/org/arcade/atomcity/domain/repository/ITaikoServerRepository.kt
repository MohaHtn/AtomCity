package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

interface ITaikoServerRepository {
    fun getDashboardFlow(): Flow<String?>
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?>
    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?>
    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?>
    fun getCostumesFlow(): Flow<TaikoServerCostumesResponse?>
    fun getLockedCostumesFlow(): Flow<TaikoServerLockedCostumes?>
    fun getTitlesFlow(): Flow<TaikoServerTitlesResponse?>
    fun getLockedTitlesFlow(): Flow<TaikoServerLockedTitles?>
    fun getPlayDataFlow(userNumber: String): Flow<String?>
    fun getDanBestDataFlow(userNumber: String): Flow<TaikoServerDanBestDataResponse?>
    fun getUserFlow(userNumber: String): Flow<TaikoServerUserResponse?>
    fun getSongLeaderboardFlow(songId: String, baid: String, difficulty: Int, page: Int, limit: Int): Flow<TaikoServerLeaderboardResponse?>
    suspend fun login(loginRequest: TaikoLoginRequest): TaikoServerAuthResponse
    suspend fun updateUserSettings(baid: Int, settings: TaikoServerUserSettingsResponse, authToken: String): TaikoServerUserSettingsResponse
    suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse
}
