package org.arcade.atomcity.model.maitea.playerDetailsResponse

import kotlinx.serialization.Serializable

@Serializable
data class MaiteaPlayerDetailsResponse(
    val data: List<Data>? = null
)

@Serializable
data class Data(
    val name: String? = null,
    val rating: Int? = null,
    val profileImageUrl: String? = null,
    val options: Options? = null
)

@Serializable
data class Icon(
    val id: String? = null,
    val url: String? = null,
    val png: String? = null
)

@Serializable
data class Title(
    val id: Int? = null,
    val value: String? = null
)

@Serializable
data class Options(
    val showRating: Boolean? = null,
    val iconDeka: Icon? = null,
    val icon: Icon? = null,
    val title: Title? = null,
    val frame: Icon? = null
)
