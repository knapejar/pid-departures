package com.example.piddepartures.data.remote

import com.example.piddepartures.data.remote.dto.DepartureBoardResponse
import com.example.piddepartures.data.remote.dto.StopsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GolemioApiService {
    
    @GET("v2/pid/departureboards")
    suspend fun getDepartureBoards(
        @Query("ids[]") stopIds: List<String>,
        @Query("minutesAfter") minutesAfter: Int = 60,
        @Query("minutesBefore") minutesBefore: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("filter") filter: String = "routeHeadingOnce"
    ): DepartureBoardResponse
    
    @GET("v2/gtfs/stops")
    suspend fun getStops(
        @Query("names") names: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): StopsResponse
}
