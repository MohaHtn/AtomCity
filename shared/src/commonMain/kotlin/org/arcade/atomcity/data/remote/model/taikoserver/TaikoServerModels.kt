package org.arcade.atomcity.data.remote.model.taikoserver

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerPlayHistoryResponse(
    val taikoServerSongHistoryData: TaikoServerSongHistoryData? = null
)

@Serializable
data class TaikoServerSongHistoryData(
    val entries: List<TaikoServerHistoryEntry> = emptyList()
)

@Serializable
data class TaikoServerHistoryEntry(
    val songId: String? = null,
    val songName: String? = null,
    val artistName: String? = null,
    val value: Int? = null,
    val difficulty: Int? = null,
    val score: Int? = null,
    val playTime: String? = null
)

@Serializable
data class TaikoServerMusicDetailsResponse(
    val musicName: String? = null,
    val musicArtist: String? = null
)

@Serializable
data class TaikoServerUserSettingsResponse(
    val myDonName: String? = null,
    val title: String? = null
)

@Serializable
data class TaikoServerDanBestDataResponse(
    val baid: Int? = null,
    val danId: Int? = null,
    val status: Int? = null
)

@Serializable
data class TaikoServerUserResponse(
    val baid: Int? = null,
    val nickname: String? = null,
    val accessCode: String? = null
)

@Serializable
data class TaikoServerLeaderboardResponse(
    val songId: Int? = null,
    val difficulty: Int? = null,
    val entries: List<TaikoServerLeaderboardEntry> = emptyList()
)

@Serializable
data class TaikoServerLeaderboardEntry(
    val baid: Int? = null,
    val nickname: String? = null,
    val score: Int? = null,
    val rank: Int? = null
)

@Serializable
data class TaikoServerAuthResponse(
    val success: Boolean = false,
    val message: String? = null,
    val token: String? = null
)
