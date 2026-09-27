/**
 * Difficulty Entity
 *
 * Room database entity representing song difficulty metadata and level details.
 */
package org.arcade.atomcity.data.local.maimai

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
