package org.arcade.atomcity.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.arcade.atomcity.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse

class TaikoServerClient(private val client: HttpClient, private val baseUrl: String = "https://taiko.farewell.dev/api/") {
    suspend fun getPlayHistory(userNumber: String): TaikoServerPlayHistoryResponse =
        client.get("${baseUrl}PlayHistory/$userNumber").body()

    suspend fun getMusicDetails(): TaikoServerMusicDetailsResponse =
        client.get("${baseUrl}GameData/MusicDetails").body()

    suspend fun getUserSettings(userNumber: String): TaikoServerUserSettingsResponse =
        client.get("${baseUrl}UserSettings/$userNumber").body()

    suspend fun getPlayData(): String =
        client.get("${baseUrl}PlayData/152").body()
}
