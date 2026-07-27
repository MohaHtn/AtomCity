package org.arcade.atomcity.model.maitea

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChartHistoryResponse(
    val keyHash: String?,
    val songName: String?,
    val achievement: Double?,
    val playDate: String?,
    val difficulty: String?,
    val difficultyLevel: String?,
    val rank: String?,
    val rating: Double?,
    val playerName: String?
)
