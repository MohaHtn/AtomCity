package org.arcade.atomcity.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = org.koin.java.KoinJavaComponent.get<Context>(Context::class.java)
    val dbFile = appContext.getDatabasePath("atomcity.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).createFromAsset("maimai/database/maimai_internal_diffs.db")
}
