package com.example.piddepartures.data.repository

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.piddepartures.data.local.AppDatabase
import com.example.piddepartures.data.local.entity.SavedStopEntity
import dagger.hilt.android.qualifiers.ApplicationContext
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
            // Add default stop - Anděl
            database.get().savedStopDao().insertSavedStop(
                SavedStopEntity(
                    stopId = "U1040Z1P",
                    stopName = "Anděl",
                    platformCode = "A",
                    routeShortName = null,
                    routeType = null,
                    direction = null
                )
            )
        }
    }
}
