package com.example.piddepartures.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.local.entity.SavedStopEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE saved_stops ADD COLUMN customDirection TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE saved_stops ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [SavedStopEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedStopDao(): SavedStopDao
}
