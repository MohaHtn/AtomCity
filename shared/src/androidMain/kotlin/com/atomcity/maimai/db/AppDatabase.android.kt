package com.atomcity.maimai.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = org.koin.java.KoinJavaComponent.get<Context>(Context::class.java)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("maimai.db").absolutePath
    ).createFromAsset("maimai/database/maimai_internal_diffs.db")
}
