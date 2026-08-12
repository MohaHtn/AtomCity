package org.arcade.atomcity.data

import com.atomcity.maimai.db.AppDatabase

class DifficultyRepository(private val database: AppDatabase) {
    suspend fun getLevelByDifficulty(songId: Int, diffIndex: Int): LevelInfo {
        val entity = database.songDao().getLevelByDifficulty(songId, diffIndex)
        return entity?.let {
            LevelInfo(level = it.level ?: "", internalLevel = it.internal_level ?: "")
        } ?: LevelInfo()
    }
}
