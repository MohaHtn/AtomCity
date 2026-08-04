package com.atomcity.maimai.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "song",
    indices = [
        Index(value = ["code"], name = "idx_song_code")
    ]
) // tag: DifficultyRepository
data class SongEntity(
    @PrimaryKey val id: Int?,
    val code: String?,
    val name_en: String?,
    val name_jp: String?,
    val artist_en: String?,
    val artist_jp: String?,
    val matchedTitle: String?,
    val matchedBy: String?
)

