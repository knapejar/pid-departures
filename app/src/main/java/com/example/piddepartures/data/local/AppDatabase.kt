package com.example.piddepartures.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.local.entity.SavedStopEntity

@Database(
    entities = [SavedStopEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedStopDao(): SavedStopDao
}
