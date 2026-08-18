package org.arcade.atomcity.data.local

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}

@Database(entities = [DifficultyEntity::class, SongEntity::class, LevelEntity::class], version = 2, exportSchema = true)
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
