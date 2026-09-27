/**
 * Application Room Database
 *
 * Defines the local SQLite Room database configuration, entities, database constructor, and DAO accessors for multiplatform persistence.
 */
package org.arcade.atomcity.data.local.maimai

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.arcade.atomcity.data.local.maimai.SongEntity
import org.arcade.atomcity.data.local.maimai.LevelEntity
import org.arcade.atomcity.data.local.maimai.SongDao

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}

@Database(entities = [SongEntity::class, LevelEntity::class], version = 2, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
