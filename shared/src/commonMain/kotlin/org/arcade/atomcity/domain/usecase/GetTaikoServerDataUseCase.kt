package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.TaikoServerRepository
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse

class GetTaikoServerDataUseCase(private val repository: TaikoServerRepository) {
    fun getPlayHistoryFlow(userNumber: String): Flow<TaikoServerPlayHistoryResponse?> =
        repository.getPlayHistoryFlow(userNumber)

    fun getMusicDetailsFlow(): Flow<TaikoServerMusicDetailsResponse?> =
        repository.getMusicDetailsFlow()

    fun getUserSettingsFlow(userNumber: String): Flow<TaikoServerUserSettingsResponse?> =
        repository.getUserSettingsFlow(userNumber)
}
