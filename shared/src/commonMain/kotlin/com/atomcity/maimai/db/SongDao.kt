package com.atomcity.maimai.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SongDao {
    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getSongById(id: Int): SongEntity?

    @Query("SELECT * FROM song WHERE code = :code")
    suspend fun getSongByCode(code: String): SongEntity?

    @Query("SELECT * FROM song WHERE name_en LIKE '%' || :q || '%' OR name_jp LIKE '%' || :q || '%' OR artist_en LIKE '%' || :q || '%' OR artist_jp LIKE '%' || :q || '%'")
    suspend fun searchSongs(q: String): List<SongEntity>

    @Query("SELECT * FROM level WHERE songId = :songId ORDER BY diffIndex")
    suspend fun getLevelsForSong(songId: Int?): List<LevelEntity>

    @Query(
        """
        SELECT
            song.matchedTitle,
            song.name_en,
            song.name_jp,
            song.code,
            level.diffIndex,
            level.level,
            level.internal_level
        FROM level
        JOIN song ON song.id = level.songId
        """
    )
    suspend fun getAllSongLevels(): List<SongLevelRow>

    @Query(
        """
        SELECT level.*
        FROM level
        JOIN song ON song.id = level.songId
        WHERE lower(COALESCE(song.matchedTitle, song.name_en, song.code, song.name_jp)) = lower(:title)
        ORDER BY level.diffIndex
        LIMIT 1
        """
    )
    suspend fun getLevelsByTitle(title: String): LevelEntity?

    @Query("SELECT * FROM song")
    suspend fun getAllSongs(): List<SongEntity>

    @Query(
        """
        SELECT level.*
        FROM level
        JOIN song ON song.id = level.songId
        WHERE (lower(song.name_en) = lower(:title) OR lower(song.name_jp) = lower(:title) OR lower(song.matchedTitle) = lower(:title))
        AND level.diffIndex = :diffIndex
        LIMIT 1
        """
    )
    suspend fun getLevelByTitleAndDifficulty(title: String, diffIndex: Int): LevelEntity?

    @Query("SELECT * FROM level WHERE songId = :songId AND diffIndex = :difficultyValue ORDER BY uid LIMIT 1")
    suspend fun getLevelByDifficulty(songId: Int, difficultyValue: Int): LevelEntity?
}
