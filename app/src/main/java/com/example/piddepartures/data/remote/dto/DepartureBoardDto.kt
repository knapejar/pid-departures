package com.example.piddepartures.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DepartureBoardResponse(
    @SerializedName("stops")
    val stops: List<StopDto>?,
    @SerializedName("departures")
    val departures: List<DepartureDto>?,
    @SerializedName("infotexts")
    val infotexts: List<InfotextDto>?
)

data class StopDto(
    @SerializedName("stop_id")
    val stopId: String?,
    @SerializedName("stop_name")
    val stopName: String?,
    @SerializedName("platform_code")
    val platformCode: String?,
    @SerializedName("stop_lat")
    val stopLat: Double?,
    @SerializedName("stop_lon")
    val stopLon: Double?
)

data class DepartureDto(
    @SerializedName("departure_timestamp")
    val departureTimestamp: DepartureTimestampDto?,
    @SerializedName("arrival_timestamp")
    val arrivalTimestamp: DepartureTimestampDto?,
    @SerializedName("route")
    val route: RouteDto?,
    @SerializedName("trip")
    val trip: TripDto?,
    @SerializedName("stop")
    val stop: StopReferenceDto?,
    @SerializedName("delay")
    val delay: DelayDto?,
    @SerializedName("last_stop")
    val lastStop: LastStopDto?
)

data class DepartureTimestampDto(
    @SerializedName("predicted")
    val predicted: String?,
    @SerializedName("scheduled")
    val scheduled: String?,
    @SerializedName("minutes")
    val minutes: String?
)

data class RouteDto(
    @SerializedName("short_name")
    val shortName: String?,
    @SerializedName("type")
    val type: Int?,
    @SerializedName("is_night")
    val isNight: Boolean?,
    @SerializedName("is_regional")
    val isRegional: Boolean?,
    @SerializedName("is_substitute_transport")
    val isSubstituteTransport: Boolean?
)

data class TripDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("headsign")
    val headsign: String?,
    @SerializedName("short_name")
    val shortName: String?,
    @SerializedName("is_canceled")
    val isCanceled: Boolean?,
    @SerializedName("is_wheelchair_accessible")
    val isWheelchairAccessible: Boolean?,
    @SerializedName("is_air_conditioned")
    val isAirConditioned: Boolean?,
    @SerializedName("is_at_stop")
    val isAtStop: Boolean?,
    @SerializedName("direction")
    val direction: String?
)

data class StopReferenceDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("platform_code")
    val platformCode: String?
)

data class DelayDto(
    @SerializedName("is_available")
    val isAvailable: Boolean?,
    @SerializedName("minutes")
    val minutes: Int?,
    @SerializedName("seconds")
    val seconds: Int?
)

data class LastStopDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?
)

data class InfotextDto(
    @SerializedName("text")
    val text: String?,
    @SerializedName("text_en")
    val textEn: String?,
    @SerializedName("display_type")
    val displayType: String?
)
