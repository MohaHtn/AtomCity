package org.arcade.atomcity.data.remote.model.taikoserver.songHistory

import kotlinx.serialization.Serializable
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.PlaySetting

@Serializable
data class TaikoServerPlayHistoryResponse(
    val songHistoryData: List<TaikoServerHistoryEntry> = emptyList()
)

@Serializable
data class TaikoServerHistoryEntry(
    val songId: Int? = null,
    val genre: Int? = null,
    val musicName: String? = null,
    val musicNameEN: String? = null,
    val musicNameCN: String? = null,
    val musicNameKO: String? = null,
    val musicArtist: String? = null,
    val musicArtistEN: String? = null,
    val musicArtistCN: String? = null,
    val musicArtistKO: String? = null,
    val difficulty: Int? = null,
    val stars: Int? = null,
    val showDetails: Boolean? = null,
    val score: Int? = null,
    val crown: Int? = null,
    val scoreRank: Int? = null,
    val playTime: String? = null,
    val isFavorite: Boolean? = null,
    val goodCount: Int? = null,
    val okCount: Int? = null,
    val missCount: Int? = null,
    val comboCount: Int? = null,
    val hitCount: Int? = null,
    val drumrollCount: Int? = null,
    val songNumber: Int? = null,
    val playSetting: PlaySetting? = null
)
