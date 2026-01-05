package com.example.piddepartures.data.repository

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.piddepartures.data.local.AppDatabase
import com.example.piddepartures.data.local.entity.SavedStopEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class DatabaseCallback @Inject constructor(
    private val database: Provider<AppDatabase>
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            // Add default stop - Anděl (Metro B platform)
            // U457Z1P is one of the main platforms for Anděl
            database.get().savedStopDao().insertSavedStop(
                SavedStopEntity(
                    stopId = "U457Z1P",
                    stopName = "Anděl",
                    platformCode = "B",
                    routeShortName = null,
                    routeType = null,
                    direction = null
                )
            )
        }
    }
}
