package org.arcade.atomcity.network;

import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse;
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;

interface TaikoServerService {
    @GET("PlayHistory/{userNumber}")
    suspend fun getPlayHistory(
        @Path("userNumber") userNumber: String
    ): TaikoServerPlayHistoryResponse

    @GET("GameData/MusicDetails")
    suspend fun getMusicDetails()
    : TaikoServerMusicDetailsResponse

    @GET("UserSettings/1")
    suspend fun getUserSettings(): TaikoServerUserSettingsResponse

    @GET("PlayData/152")
    suspend fun getPlayData(): String
}
