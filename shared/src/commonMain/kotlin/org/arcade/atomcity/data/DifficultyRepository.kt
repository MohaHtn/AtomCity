package org.arcade.atomcity.data

import com.atomcity.maimai.db.AppDatabase
import com.atomcity.maimai.db.LevelEntity

class DifficultyRepository(private val database: AppDatabase) {
    /**
     * Retrieves level information for a specific song and difficulty.
     * 
     * @param songId The ID of the song in the database (may be -1 if unknown).
     * @param diffIndex The difficulty index (0=Basic, 1=Advanced, 2=Expert, 3=Master, 4=Re:Master, 5=Utage).
     * @param songTitle Primary title to search for (usually Japanese).
     * @param altTitle Alternative title to search for (usually English).
     */
    suspend fun getLevelByDifficulty(
        songId: Int, 
        diffIndex: Int, 
        songTitle: String? = null, 
        altTitle: String? = null
    ): LevelInfo {
        if (diffIndex == -1) return LevelInfo()

        // 1. Try searching by exact titles first (more reliable if IDs differ between sources)
        var entity: LevelEntity? = if (songTitle != null) {
            database.songDao().getLevelByTitleAndDifficulty(songTitle, diffIndex)
        } else null

        if (entity == null && altTitle != null) {
            entity = database.songDao().getLevelByTitleAndDifficulty(altTitle, diffIndex)
        }

        // 2. Try by song ID if we have a valid one
        if (entity == null && songId != -1) {
            entity = database.songDao().getLevelByDifficulty(songId, diffIndex)
        }

        // 3. Fallback: Fuzzy search by song title prefix
        if (entity == null && songTitle != null && songTitle.length >= 3) {
            val searchResults = database.songDao().searchSongs(songTitle.take(5))
            for (match in searchResults) {
                val matchEntity = database.songDao().getLevelByDifficulty(match.id!!, diffIndex)
                if (matchEntity != null) {
                    entity = matchEntity
                    break
                }
            }
        }

        return entity?.let {
            LevelInfo(level = it.level ?: "", internalLevel = it.internal_level ?: "")
        } ?: LevelInfo()
    }
}
