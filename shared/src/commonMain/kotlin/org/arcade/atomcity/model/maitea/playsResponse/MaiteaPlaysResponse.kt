package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MaiteaPlaysResponse(
    val data: List<MaiteaApiData>
)

@Serializable
data class MaiteaApiData(
    val id: Int? = null,
    val playId: Int? = null,
    val song: Song? = null,
    val difficultyLevel: DifficultyLevel? = null,
    val achievements: Double? = null,
    val achievementFormatted: String? = null,
    val rank: String? = null,
    val rating: Double? = null,
    val ratingFormatted: String? = null,
    val isHighScore: Boolean? = null,
    val isAllPerfect: Boolean? = null,
    val isTrackSkip: Boolean? = null,
    val playDate: String? = null,
    var jacketImageUrl: String? = null,
    val scoreDetail: ScoreDetail? = null
)

@Serializable
data class Song(
    val id: String? = null,
    val name: Name? = null,
    val artist: Artist? = null
)

@Serializable
data class Name(
    val jp: String? = null,
    val en: String? = null
)

@Serializable
data class Artist(
    val jp: String? = null,
    val en: String? = null,
    val name: String? = null
)

@Serializable
data class DifficultyLevel(
    val key: String? = null,
    val value: String? = null,
    val label: String? = null
)

@Serializable
data class ScoreDetail(
    val tap: Tap? = null,
    val hold: Hold? = null,
    val slide: Slide? = null,
    val touch: Touch? = null,
    @SerialName("break") val breakk: Break? = null,
    val hits: Hits? = null
)

@Serializable
data class Tap(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)

@Serializable
data class Hold(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)

@Serializable
data class Slide(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)

@Serializable
data class Touch(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)

@Serializable
data class Break(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)

@Serializable
data class Hits(val perfect: Int? = null, val great: Int? = null, val good: Int? = null, val bad: Int? = null, val miss: Int? = null)
