package org.arcade.atomcity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.remote.TaikoServerClient
import org.arcade.atomcity.domain.repository.ITaikoServerRepository
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class TaikoServerRepository(private val client: TaikoServerClient) : ITaikoServerRepository {
    override fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> = flow {
        emit(client.getPlayHistory(userNumber))
    }

    override fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> = flow {
        emit(client.getMusicDetails())
    }

    override fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> = flow {
        emit(client.getUserSettings(userNumber))
    }
}
