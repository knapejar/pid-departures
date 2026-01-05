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
            
            val departures = response.departures?.mapNotNull { dto ->
                val route = dto.route
                val trip = dto.trip
                val ts = dto.departureTimestamp
                val stop = dto.stop
                
                if (route != null && trip?.headsign != null && ts?.minutes != null) {
                    Departure(
                        routeShortName = route.shortName ?: "",
                        routeType = route.type ?: 3,
                        headsign = trip.headsign,
                        minutes = ts.minutes,
                        platformCode = stop?.platformCode,
                        isDelayed = dto.delay?.isAvailable == true && (dto.delay?.minutes ?: 0) > 0,
                        delayMinutes = dto.delay?.minutes,
                        isWheelchairAccessible = trip.isWheelchairAccessible == true,
                        isAirConditioned = trip.isAirConditioned,
                        isCanceled = trip.isCanceled == true
                    )
                } else null
            } ?: emptyList()
            
            Result.success(departures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchStops(query: String): Result<List<StopInfo>> {
        return try {
            val response = apiService.getStops(
                names = listOf(query),
                limit = 20
            )
            
            val stops = response.features?.mapNotNull { feature ->
                val props = feature.properties
                val geom = feature.geometry
                // Only return actual platforms (location_type = 0), not stations (location_type = 1)
                // because departureboards endpoint requires platform stop_id
                if (props?.stopId != null && props.stopName != null && props.locationType == 0) {
                    StopInfo(
                        stopId = props.stopId,
                        stopName = props.stopName,
                        platformCode = props.platformCode,
                        latitude = geom?.coordinates?.getOrNull(1),
                        longitude = geom?.coordinates?.getOrNull(0)
                    )
                } else null
            } ?: emptyList()
            
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
