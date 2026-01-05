package com.example.piddepartures.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopsResponse(
    @SerializedName("type")
    val type: String,
    @SerializedName("features")
    val features: List<StopFeature>
)

data class StopFeature(
    @SerializedName("type")
    val type: String,
    @SerializedName("geometry")
    val geometry: GeometryDto,
    @SerializedName("properties")
    val properties: StopPropertiesDto
)

data class GeometryDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("coordinates")
    val coordinates: List<Double>
)

data class StopPropertiesDto(
    @SerializedName("stop_id")
    val stopId: String,
    @SerializedName("stop_name")
    val stopName: String,
    @SerializedName("platform_code")
    val platformCode: String?,
    @SerializedName("zone_id")
    val zoneId: String?,
    @SerializedName("wheelchair_boarding")
    val wheelchairBoarding: Int?,
    @SerializedName("asw_id")
    val aswId: AswIdDto?
)

data class AswIdDto(
    @SerializedName("node")
    val node: Int,
    @SerializedName("stop")
    val stop: Int
)
