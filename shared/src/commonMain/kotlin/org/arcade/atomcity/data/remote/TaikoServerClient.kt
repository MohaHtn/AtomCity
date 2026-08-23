package org.arcade.atomcity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.arcade.atomcity.data.remote.model.taikoserver.*
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.*
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.musicDetails.TaikoServerMusicDetailsResponse
import org.arcade.atomcity.data.remote.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse

class TaikoServerClient(private val client: HttpClient, private val baseUrl: String = "https://taiko.farewell.dev/") {
    suspend fun getDashboard(): String =
        client.get("${baseUrl}Dashboard.md").body()

    suspend fun getPlayHistory(userNumber: String): TaikoServerPlayHistoryResponse =
        client.get("${baseUrl}api/PlayHistory/$userNumber").body()

    suspend fun getMusicDetails(): TaikoServerMusicDetailsResponse =
        client.get("${baseUrl}api/GameData/MusicDetails").body()

    suspend fun getUserSettings(userNumber: String): TaikoServerUserSettingsResponse =
        client.get("${baseUrl}api/UserSettings/$userNumber").body()

    suspend fun getCostumes(): TaikoServerCostumesResponse =
        client.get("${baseUrl}api/GameData/Costumes").body()

    suspend fun getLockedCostumes(): TaikoServerLockedCostumes =
        client.get("${baseUrl}api/GameData/LockedCostumes").body()

    suspend fun getTitles(): TaikoServerTitlesResponse =
        client.get("${baseUrl}api/GameData/Titles").body()

    suspend fun getLockedTitles(): TaikoServerLockedTitles =
        client.get("${baseUrl}api/GameData/LockedTitles").body()

    suspend fun getPlayData(userNumber: String = "327"): String =
        client.get("${baseUrl}api/PlayData/$userNumber").body()

    suspend fun getDanBestData(userNumber: String): TaikoServerDanBestDataResponse =
        client.get("${baseUrl}api/DanBestData/$userNumber").body()

    suspend fun getUser(userNumber: String): TaikoServerUserResponse =
        client.get("${baseUrl}api/Users/$userNumber").body()

    suspend fun getSongLeaderboard(
        songId: String,
        baid: String,
        difficulty: Int,
        page: Int = 1,
        limit: Int = 10
    ): TaikoServerLeaderboardResponse =
        client.get("${baseUrl}api/SongLeaderboard/$songId") {
            parameter("baid", baid)
            parameter("difficulty", difficulty)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun login(loginRequest: Map<String, String>): TaikoServerAuthResponse =
        client.post("${baseUrl}api/Auth/Login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }.body()

    suspend fun changePassword(passwordRequest: Map<String, String>): TaikoServerAuthResponse =
        client.post("${baseUrl}api/Auth/ChangePassword") {
            contentType(ContentType.Application.Json)
            setBody(passwordRequest)
        }.body()
}
