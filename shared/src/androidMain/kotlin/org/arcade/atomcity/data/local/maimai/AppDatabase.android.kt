/**
 * Android AppDatabase Platform Implementation
 *
 * Provides Android-specific Room database builder and assets creation for Maimai database.
 */
package org.arcade.atomcity.data.local.maimai

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.java.KoinJavaComponent

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = KoinJavaComponent.get<Context>(Context::class.java)
    val dbFile = appContext.getDatabasePath("atomcity.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).createFromAsset("maimai/database/maimai_internal_diffs.db")
}
