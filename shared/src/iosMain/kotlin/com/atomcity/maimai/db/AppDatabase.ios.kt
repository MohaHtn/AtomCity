package com.atomcity.maimai.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSBundle
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/maimai.db"
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(dbFilePath)) {
        val bundle = NSBundle.mainBundle
        val assetPath = bundle.pathForResource("maimai_internal_diffs", "db", "compose-resources/files/maimai/database")
            ?: bundle.pathForResource("maimai_internal_diffs", "db")
        
        if (assetPath != null) {
            fileManager.copyItemAtPath(assetPath, dbFilePath, null)
        }
    }
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = { AppDatabaseConstructor.initialize() }
    ).setDriver(BundledSQLiteDriver())
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
