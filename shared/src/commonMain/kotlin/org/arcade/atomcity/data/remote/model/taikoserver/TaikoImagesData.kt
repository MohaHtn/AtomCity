package org.arcade.atomcity.data.remote.model.taikoserver

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TaikoImagesData(
    val images: ImagesSection
)

@Serializable
data class ImagesSection(
    val _files: List<String> = emptyList(),
    @SerialName("Costumes") val costumes: CostumesSection? = null,
    @SerialName("Nameplates") val nameplates: NameplatesSection? = null,
    @SerialName("Speed") val speed: SpeedSection? = null
)

@Serializable
data class CostumesSection(
    val body: FileList? = null,
    val face: FileList? = null,
    val head: FileList? = null,
    val kigurumi: FileList? = null,
    val masks: FileList? = null,
    val puchi: FileList? = null
)

@Serializable
data class NameplatesSection(
    val _files: List<String> = emptyList()
)

@Serializable
data class SpeedSection(
    val _files: List<String> = emptyList()
)

@Serializable
data class FileList(
    val _files: List<String> = emptyList()
)
