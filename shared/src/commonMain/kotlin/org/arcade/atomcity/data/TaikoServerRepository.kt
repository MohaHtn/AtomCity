package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.network.TaikoServerClient
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class TaikoServerRepository(private val client: TaikoServerClient) {
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> = flow {
        emit(client.getPlayHistory(userNumber))
    }

    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> = flow {
        emit(client.getMusicDetails())
    }

    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> = flow {
        emit(client.getUserSettings(userNumber))
    }
}
