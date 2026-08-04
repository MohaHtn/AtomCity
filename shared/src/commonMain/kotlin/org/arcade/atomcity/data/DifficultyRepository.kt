package org.arcade.atomcity.data

import org.arcade.atomcity.db.AppDatabase

class DifficultyRepository(private val database: AppDatabase) {
    suspend fun getLevelByDifficulty(songId: String, difficulty: String): LevelInfo {
        val entity = database.difficultyDao().getLevelByDifficulty(songId, difficulty)
        return entity?.let {
            LevelInfo(level = it.level, internalLevel = it.internalLevel)
        } ?: LevelInfo()
    }
}
