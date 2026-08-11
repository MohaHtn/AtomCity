package org.arcade.atomcity.model.maitea

import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.maitea.playsResponse.Song

@Serializable
data class MaimaiMostPlayedEntry(
    val songName: String?,
    val playCount: Int,
    val difficulty: String?,
    val songJson: Song? = null,
    var jacketImageUrl: String? = null
)
