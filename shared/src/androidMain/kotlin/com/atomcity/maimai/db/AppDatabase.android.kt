package com.atomcity.maimai.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = org.koin.java.KoinJavaComponent.get<Context>(Context::class.java)
    val dbFile = appContext.getDatabasePath("maimai.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
