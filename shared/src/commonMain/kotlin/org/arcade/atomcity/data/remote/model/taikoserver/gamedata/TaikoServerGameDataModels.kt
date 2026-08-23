package org.arcade.atomcity.data.remote.model.taikoserver.gamedata

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerCostume(
    val costumeId: Int? = null,
    val costumeType: String? = null,
    val costumeName: String? = null,
    val costumeNameEN: String? = null,
    val costumeNameCN: String? = null,
    val costumeNameKO: String? = null
)

@Serializable
data class TaikoServerLockedCostumes(
    val kigurumi: List<Int> = emptyList(),
    val head: List<Int> = emptyList(),
    val body: List<Int> = emptyList(),
    val face: List<Int> = emptyList(),
    val puchi: List<Int> = emptyList()
)

@Serializable
data class TaikoServerTitle(
    val titleId: Int? = null,
    val titleName: String? = null,
    val titleNameEN: String? = null,
    val titleNameCN: String? = null,
    val titleNameKO: String? = null,
    val titleRarity: Int? = null
)

@Serializable
data class TaikoServerLockedTitles(
    val title: List<Int> = emptyList(),
    val titlePlate: List<Int> = emptyList()
)

typealias TaikoServerTitlesResponse = Map<String, TaikoServerTitle>
typealias TaikoServerCostumesResponse = List<TaikoServerCostume>
