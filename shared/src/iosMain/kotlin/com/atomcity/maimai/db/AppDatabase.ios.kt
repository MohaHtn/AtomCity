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
    val fileManager = NSFileManager.defaultManager
    val docsUrl = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val dbFilePath = docsUrl!!.path!! + "/maimai_v3.db"
    
    if (!fileManager.fileExistsAtPath(dbFilePath)) {
        val bundle = NSBundle.mainBundle
        val assetPath = bundle.pathForResource("maimai_internal_diffs", "db")
            ?: bundle.pathForResource("maimai_internal_diffs", "db", "maimai/database")
        
        if (assetPath != null) {
            println("AppDatabase: Fresh copy from $assetPath to $dbFilePath")
            fileManager.copyItemAtPath(assetPath, dbFilePath, null)
        } else {
            println("AppDatabase: CRITICAL ERROR - maimai_internal_diffs.db not found in bundle!")
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
