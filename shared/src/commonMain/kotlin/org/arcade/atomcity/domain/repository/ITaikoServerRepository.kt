package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

interface ITaikoServerRepository {
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?>
    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?>
    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?>
}
