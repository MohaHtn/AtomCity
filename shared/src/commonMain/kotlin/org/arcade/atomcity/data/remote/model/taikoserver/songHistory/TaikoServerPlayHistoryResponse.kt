package org.arcade.atomcity.data.remote.model.taikoserver.songHistory

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerPlayHistoryResponse(
    val taikoServerSongHistoryData: List<TaikoServerHistoryEntry> = emptyList()
)

@Serializable
data class TaikoServerHistoryEntry(
    val songId: String? = null,
    val songName: String? = null,
    val artistName: String? = null,
    val musicName: String? = null,
    val musicArtist: String? = null,
    val value: Int? = null,
    val difficulty: Int? = null,
    val score: Int? = null,
    val playTime: String? = null
)
