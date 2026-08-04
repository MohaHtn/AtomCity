package org.arcade.atomcity.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "difficulties")
data class DifficultyEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val difficulty: String,
    val level: String,
    val internalLevel: String
)
