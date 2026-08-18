package org.arcade.atomcity.data.local

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}

@Database(entities = [DifficultyEntity::class, SongEntity::class, LevelEntity::class], version = 3, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun difficultyDao(): DifficultyDao
    abstract fun songDao(): SongDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Dao
interface DifficultyDao {
    @Query("SELECT * FROM difficulties WHERE songId = :songId AND difficulty = :difficulty LIMIT 1")
    suspend fun getLevelByDifficulty(songId: String, difficulty: String): DifficultyEntity?
}
