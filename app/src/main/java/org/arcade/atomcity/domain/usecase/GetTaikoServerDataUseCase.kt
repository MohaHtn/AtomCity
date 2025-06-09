package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.network.TaikoServerService

class GetTaikoServerDataUseCase(private val taikoServerService: TaikoServerService) {

    suspend fun execute(page: String): Flow<TaikoServerPlayHistoryResponse?> {
        return withContext(Dispatchers.IO) {
            flow {
                val response = taikoServerService.getPlayHistory(page)
                emit(response)
            }
        }
    }

    suspend fun executeMusicDetails(): Flow<TaikoServerMusicDetailsResponse?> {
        return withContext(Dispatchers.IO) {
            flow {
                val response = taikoServerService.getMusicDetails()
                emit(response)
            }
        }
    }
}