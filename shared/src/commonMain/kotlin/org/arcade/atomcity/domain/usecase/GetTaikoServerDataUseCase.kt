package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.ITaikoServerRepository
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class GetTaikoServerDataUseCase(private val repository: ITaikoServerRepository) {
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> =
        repository.getPlayHistoryFlow(userNumber)

    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> =
        repository.getMusicDetailsFlow()

    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> =
        repository.getUserSettingsFlow(userNumber)
}
