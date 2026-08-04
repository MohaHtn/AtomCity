package org.arcade.atomcity.model.taikoserver

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
