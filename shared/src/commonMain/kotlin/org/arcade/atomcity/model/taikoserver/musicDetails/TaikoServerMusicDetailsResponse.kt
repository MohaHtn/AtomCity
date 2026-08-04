package org.arcade.atomcity.model.taikoserver.musicDetails

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerMusicDetailsResponse(
    val entries: List<MusicDetailEntry> = emptyList()
)

@Serializable
data class MusicDetailEntry(
    val key: String? = null,
    val value: MusicDetail? = null
)

@Serializable
data class MusicDetail(
    val songName: String? = null,
    val artistName: String? = null
)
