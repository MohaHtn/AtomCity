package org.arcade.atomcity.domain.repository

import org.arcade.atomcity.domain.model.LevelInfo

interface IDifficultyRepository {
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
    ): LevelInfo
}
