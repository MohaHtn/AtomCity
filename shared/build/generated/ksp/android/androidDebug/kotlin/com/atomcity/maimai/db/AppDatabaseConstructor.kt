package com.atomcity.maimai.db

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  actual override fun initialize(): AppDatabase = com.atomcity.maimai.db.AppDatabase_Impl()
}
