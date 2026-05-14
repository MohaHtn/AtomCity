package org.arcade.atomcity.domain.usecase

import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.network.TaikoServerService

class GetTaikoServerDataUseCase(private val taikoServerService: TaikoServerService) {

    suspend fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> {
        return withContext(Dispatchers.IO) {
            flow {
                val response = taikoServerService.getPlayHistory(userNumber)
                emit(response)
            }
        }
    }

    suspend fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> {
        return withContext(Dispatchers.IO) {
            flow {
                val response = taikoServerService.getMusicDetails()
                emit(response)
            }
        }
    }

    suspend fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> {
        return withContext(Dispatchers.IO) {
            flow {
                val response = taikoServerService.getUserSettings(userNumber)
                emit(response)
            }
        }
    }
}