package com.example.piddepartures.di

import android.content.Context
import androidx.room.Room
import com.example.piddepartures.data.local.AppDatabase
import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.repository.DatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pid_departures_db"
        )
        .addCallback(callback)
        .build()
    }
    
    @Provides
    @Singleton
    fun provideSavedStopDao(database: AppDatabase): SavedStopDao {
        return database.savedStopDao()
    }
}
