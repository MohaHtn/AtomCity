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
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}

@Database(entities = [SongEntity::class, LevelEntity::class], version = 2, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}

// We remove the @Suppress so that any mismatch will be caught at compile time.
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
