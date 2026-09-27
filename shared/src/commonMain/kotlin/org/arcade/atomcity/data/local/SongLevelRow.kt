/**
 * Song Level Row Model
 *
 * Data transfer model representing joined query results of songs and their corresponding levels.
 */
package org.arcade.atomcity.data.local

data class SongLevelRow(
    val matchedTitle: String?,
    val name_en: String?,
    val name_jp: String?,
    val code: String?,
    val diffIndex: Int?,
    val level: String?,
    val internal_level: String?
)
