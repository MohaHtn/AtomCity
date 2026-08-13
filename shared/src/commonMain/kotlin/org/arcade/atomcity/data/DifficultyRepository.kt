package org.arcade.atomcity.data

import com.atomcity.maimai.db.AppDatabase

class DifficultyRepository(private val database: AppDatabase) {
    suspend fun getLevelByDifficulty(songId: Int, diffIndex: Int, songTitle: String? = null): LevelInfo {
        // 1. Try searching by exact title first (more reliable if IDs differ)
        var entity = if (songTitle != null) {
            database.songDao().getLevelByTitleAndDifficulty(songTitle, diffIndex) ?:
            database.songDao().getLevelByTitleAndDifficulty(songTitle, if(diffIndex >= 5) diffIndex - 2 else diffIndex)
        } else null

        // 2. Try by ID with various potential index offsets
        if (entity == null) {
            entity = database.songDao().getLevelByDifficulty(songId, diffIndex) ?:
                     database.songDao().getLevelByDifficulty(songId, diffIndex - 1) ?:
                     database.songDao().getLevelByDifficulty(songId, diffIndex - 2)
        }

        // 3. Last resort: fuzzy search by title prefix
        if (entity == null && songTitle != null) {
            val searchResults = database.songDao().searchSongs(songTitle.take(5))
            if (searchResults.isNotEmpty()) {
                val bestMatch = searchResults.first()
                entity = database.songDao().getLevelByDifficulty(bestMatch.id!!, if(diffIndex >= 5) diffIndex - 2 else diffIndex)
            }
        }

        return entity?.let {
            LevelInfo(level = it.level ?: "", internalLevel = it.internal_level ?: "")
        } ?: LevelInfo()
    }
}
