package com.example.piddepartures.domain.model

data class SavedStop(
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

data class Departure(
    val routeShortName: String,
    val routeType: Int,
    val headsign: String,
    val minutes: String,
    val platformCode: String?,
    val isDelayed: Boolean,
    val delayMinutes: Int?,
    val isWheelchairAccessible: Boolean,
    val isAirConditioned: Boolean?,
    val isCanceled: Boolean
)

data class StopWithDepartures(
    val savedStop: SavedStop,
    val departures: List<Departure>,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class StopInfo(
    val stopId: String,
    val stopName: String,
    val platformCode: String?,
    val latitude: Double?,
    val longitude: Double?
)

fun getRouteTypeIcon(routeType: Int): String {
    return when (routeType) {
        0 -> "🚊" // Tram
        1 -> "🚇" // Metro
        2 -> "🚆" // Train
        3 -> "🚌" // Bus
        4 -> "⛴️" // Ferry
        7 -> "🚟" // Funicular
        11 -> "🚎" // Trolleybus
        else -> "🚍"
    }
}

fun getRouteTypeName(routeType: Int): String {
    return when (routeType) {
        0 -> "Tram"
        1 -> "Metro"
        2 -> "Train"
        3 -> "Bus"
        4 -> "Ferry"
        7 -> "Funicular"
        11 -> "Trolleybus"
        else -> "Unknown"
    }
}
