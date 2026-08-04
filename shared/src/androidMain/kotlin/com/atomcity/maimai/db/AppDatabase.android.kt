package com.atomcity.maimai.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.context.GlobalContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = GlobalContext.get().get<Context>()
    val dbFile = appContext.getDatabasePath("maimai_internal_diffs.db")
    
    Log.d("AppDatabase", "Creating database builder from asset: maimai/database/maimai_internal_diffs.db")
    
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
    .createFromAsset("maimai/database/maimai_internal_diffs.db")
    .fallbackToDestructiveMigration(true) // Autorise la recréation si le schéma change
}
