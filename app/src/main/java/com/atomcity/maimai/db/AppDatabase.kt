package com.atomcity.maimai.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class, LevelEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Create a database named 'maimai_internal_diffs.db' onto the device by using the database
        // in the asset folder.
        // You can't open the database directly with Room.
        // If you want that behavior, you'll need to open the database commonly with sqlite.
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "maimai_internal_diffs.db")
                    .createFromAsset("maimai/database/maimai_internal_diffs.db")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

