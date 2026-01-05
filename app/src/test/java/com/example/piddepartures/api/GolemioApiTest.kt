package com.example.piddepartures.api

import com.example.piddepartures.BuildConfig
import com.example.piddepartures.data.remote.GolemioApiService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GolemioApiTest {

    private lateinit var apiService: GolemioApiService
    
    // Use API key from BuildConfig (loaded from local.properties)
    // Fallback to test key if not configured
    private val apiKey = BuildConfig.GOLEMIO_API_KEY.ifEmpty { 
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NDUxOCwiaWF0IjoxNzY3NjE3Njk5LCJleHAiOjExNzY3NjE3Njk5LCJpc3MiOiJnb2xlbWlvIiwianRpIjoiNDM1YTljNjYtYjRjYi00ZTg4LWI5YWMtZjIxZTZjMWQzMDBjIn0.fCu1lP0jHBYXx_D0_qr5R_Vz4SoYosixXRlxk3Twzm8"
    }

    @Before
    fun setup() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Access-Token", apiKey)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.golemio.cz/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GolemioApiService::class.java)
    }

    @Test
    fun testGetStops() = runBlocking {
        val response = apiService.getStops(names = listOf("Anděl"), limit = 5)
        println("Stops found: ${response.features?.size ?: 0}")
        assert(response.features?.isNotEmpty() == true)
    }

    @Test
    fun testGetDepartures() = runBlocking {
        val stopsResponse = apiService.getStops(names = listOf("Anděl"), limit = 20)
        // Find a real stop (location_type 0), not a station (1)
        val stopId = stopsResponse.features?.find { it.properties?.locationType == 0 }?.properties?.stopId
        
        if (stopId != null) {
            println("Testing departures for stopId: $stopId")
            val response = apiService.getDepartureBoards(stopIds = listOf(stopId))
            println("Departures found: ${response.departures?.size ?: 0}")
            assert(response.departures != null)
        } else {
            throw Exception("Could not find a valid platform stop ID for Anděl")
        }
    }
}
