package com.example.piddepartures.data.repository

import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.local.entity.SavedStopEntity
import com.example.piddepartures.data.remote.GolemioApiService
import com.example.piddepartures.domain.model.Departure
import com.example.piddepartures.domain.model.SavedStop
import com.example.piddepartures.domain.model.StopInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartureRepository @Inject constructor(
    private val apiService: GolemioApiService,
    private val savedStopDao: SavedStopDao
) {
    
    fun getSavedStops(): Flow<List<SavedStop>> {
        return savedStopDao.getAllSavedStops().map { entities ->
            entities.map { it.toSavedStop() }
        }
    }
    
    suspend fun getSavedStopById(id: Long): SavedStop? {
        return savedStopDao.getSavedStopById(id)?.toSavedStop()
    }
    
    suspend fun addSavedStop(savedStop: SavedStop): Long {
        return savedStopDao.insertSavedStop(savedStop.toEntity())
    }
    
    suspend fun deleteSavedStop(id: Long) {
        savedStopDao.deleteSavedStopById(id)
    }
    
    suspend fun getDepartures(stopId: String, limit: Int = 3): Result<List<Departure>> {
        return try {
            val response = apiService.getDepartureBoards(
                stopIds = listOf(stopId),
                minutesAfter = 60,
                minutesBefore = 0,
                limit = limit,
                filter = "routeHeadingOnce"
            )
            
            val departures = response.departures?.map { dto ->
                Departure(
                    routeShortName = dto.route.shortName ?: "",
                    routeType = dto.route.type ?: 3,
                    headsign = dto.trip.headsign,
                    minutes = dto.departureTimestamp.minutes,
                    platformCode = dto.stop.platformCode,
                    isDelayed = dto.delay?.isAvailable == true && (dto.delay.minutes ?: 0) > 0,
                    delayMinutes = dto.delay?.minutes,
                    isWheelchairAccessible = dto.trip.isWheelchairAccessible,
                    isAirConditioned = dto.trip.isAirConditioned,
                    isCanceled = dto.trip.isCanceled
                )
            } ?: emptyList()
            
            Result.success(departures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchStops(query: String): Result<List<StopInfo>> {
        return try {
            val response = apiService.getStops(
                names = query,
                limit = 20
            )
            
            val stops = response.features.map { feature ->
                StopInfo(
                    stopId = feature.properties.stopId,
                    stopName = feature.properties.stopName,
                    platformCode = feature.properties.platformCode,
                    latitude = feature.geometry.coordinates.getOrNull(1),
                    longitude = feature.geometry.coordinates.getOrNull(0)
                )
            }
            
            Result.success(stops)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun SavedStopEntity.toSavedStop() = SavedStop(
        id = id,
        stopId = stopId,
        stopName = stopName,
        platformCode = platformCode,
        routeShortName = routeShortName,
        routeType = routeType,
        direction = direction,
        addedAt = addedAt
    )
    
    private fun SavedStop.toEntity() = SavedStopEntity(
        id = id,
        stopId = stopId,
        stopName = stopName,
        platformCode = platformCode,
        routeShortName = routeShortName,
        routeType = routeType,
        direction = direction,
        addedAt = addedAt
    )
}
