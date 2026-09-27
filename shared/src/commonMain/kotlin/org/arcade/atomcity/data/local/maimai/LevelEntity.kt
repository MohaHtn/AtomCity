/**
 * Level Entity
 *
 * Room database entity storing specific chart level metadata and foreign key relations to songs.
 */
package org.arcade.atomcity.data.local.maimai

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import org.arcade.atomcity.data.local.maimai.SongEntity

@Entity(
    tableName = "level",
    indices = [
        Index(value = ["songId"], name = "idx_level_songId"),
        Index(value = ["songId"], name = "index_level_songId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long?,
    val songId: Int,
    val diffIndex: Int?,
    val internal_level: String?,
    val level: String?
)
