package com.example.piddepartures.data.local.dao

import androidx.room.*
import com.example.piddepartures.data.local.entity.SavedStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedStopDao {
    @Query("SELECT * FROM saved_stops ORDER BY orderIndex ASC, addedAt DESC")
    fun getAllSavedStops(): Flow<List<SavedStopEntity>>
    
    @Query("SELECT * FROM saved_stops WHERE id = :id")
    suspend fun getSavedStopById(id: Long): SavedStopEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedStop(stop: SavedStopEntity): Long
    
    @Update
    suspend fun updateSavedStop(stop: SavedStopEntity)
    
    @Update
    @Transaction
    suspend fun updateSavedStops(stops: List<SavedStopEntity>)
    
    @Delete
    suspend fun deleteSavedStop(stop: SavedStopEntity)
    
    @Query("DELETE FROM saved_stops WHERE id = :id")
    suspend fun deleteSavedStopById(id: Long)
    
    @Query("DELETE FROM saved_stops")
    suspend fun deleteAllSavedStops()
}
