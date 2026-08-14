package org.arcade.atomcity.model.maitea

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.arcade.atomcity.model.maitea.playsResponse.Song
import org.arcade.atomcity.data.LevelInfo

@Serializable
data class MaimaiMostPlayedEntry(
    val songName: String?,
    val songNameEn: String? = null,
    val songNameJp: String? = null,
    val playCount: Int,
    val playPercentage: Double? = null,
    val difficulty: String?,
    val songJson: Song? = null,
    var jacketImageUrl: String? = null,
    val userPlayCounts: Map<String, Int>? = null,
    @Transient
    var levelInfo: LevelInfo? = null
)
