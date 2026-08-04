package com.atomcity.maimai.db

data class SongLevelRow(
    val matchedTitle: String?,
    val name_en: String?,
    val name_jp: String?,
    val code: String?,
    val diffIndex: Int?,
    val level: String?,
    val internal_level: String?
)
