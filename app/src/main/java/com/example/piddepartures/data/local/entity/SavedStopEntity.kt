package com.example.piddepartures.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_stops")
data class SavedStopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stopId: String,
    val stopName: String,
    val platformCode: String?,
    val routeShortName: String?,
    val routeType: Int?,
    val direction: String?,
    val customDirection: String? = null,
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
