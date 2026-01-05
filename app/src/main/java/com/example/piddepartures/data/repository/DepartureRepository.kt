package com.example.piddepartures.data.repository

import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.local.entity.SavedStopEntity
import com.example.piddepartures.data.remote.GolemioApiService
import com.example.piddepartures.domain.model.Departure
import com.example.piddepartures.domain.model.SavedStop
import com.example.piddepartures.domain.model.StopInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartureRepository @Inject constructor(
    private val apiService: GolemioApiService,
    private val savedStopDao: SavedStopDao
) {
    
    // Cache pro všechny zastávky - načte se jen jednou
    private var allStopsCache: List<StopInfo>? = null
    private val cacheMutex = Mutex()
    
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
    
    /**
     * Načte všechny zastávky z API a uloží je do cache.
     * Volá se pouze jednou, další volání používají cachedovaná data.
     */
    private suspend fun loadAllStopsIfNeeded() {
        cacheMutex.withLock {
            if (allStopsCache != null) return
            
            val allStops = mutableListOf<StopInfo>()
            var offset = 0
            val limit = 10000
            val maxOffset = 30000 // Maximální počet zastávek k načtení
            
            try {
                // Načítáme všechny zastávky postupně s offsetem
                while (offset < maxOffset) {
                    val response = apiService.getStops(
                        limit = limit,
                        offset = offset
                    )
                    
                    val stops = response.features?.mapNotNull { feature ->
                        val props = feature.properties
                        val geom = feature.geometry
                        
                        if (props?.stopId != null && props.stopName != null) {
                            StopInfo(
                                stopId = props.stopId,
                                stopName = props.stopName,
                                platformCode = props.platformCode,
                                latitude = geom?.coordinates?.getOrNull(1),
                                longitude = geom?.coordinates?.getOrNull(0)
                            )
                        } else null
                    } ?: emptyList()
                    
                    allStops.addAll(stops)
                    
                    // Pokud jsme dostali méně než limit, máme všechny zastávky
                    if (stops.size < limit) break
                    
                    offset += limit
                }
                
                allStopsCache = allStops
            } catch (e: Exception) {
                // V případě chyby necháme cache prázdnou a vyhodíme výjimku
                throw e
            }
        }
    }
    
    /**
     * Odstraní diakritiku z textu pro vyhledávání.
     */
    private fun String.removeDiacritics(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
    
    /**
     * Vyhledá zastávky podle dotazu (podporuje částečné vyhledávání bez diakritiky).
     * Cachuje všechny zastávky při prvním volání.
     */
    suspend fun searchStops(query: String): Result<List<StopInfo>> {
        return try {
            // Načteme všechny zastávky do cache (pouze při prvním volání)
            loadAllStopsIfNeeded()
            
            // Pokud je dotaz prázdný, vrátíme prázdný seznam
            if (query.isBlank()) {
                return Result.success(emptyList())
            }
            
            val normalizedQuery = query.trim().removeDiacritics().lowercase()
            
            // Filtrujeme zastávky podle dotazu (bez diakritiky, case-insensitive)
            val filteredStops = allStopsCache?.filter { stop ->
                val normalizedName = stop.stopName.removeDiacritics().lowercase()
                normalizedName.contains(normalizedQuery)
            }?.take(20) ?: emptyList()
            
            Result.success(filteredStops)
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
