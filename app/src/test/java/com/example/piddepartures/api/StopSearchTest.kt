package com.example.piddepartures.api

import com.example.piddepartures.BuildConfig
import com.example.piddepartures.data.local.dao.SavedStopDao
import com.example.piddepartures.data.remote.GolemioApiService
import com.example.piddepartures.data.repository.DepartureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StopSearchTest {

    private lateinit var apiService: GolemioApiService
    private lateinit var repository: DepartureRepository
    
    // Use API key from BuildConfig (loaded from local.properties)
    // Fallback to test key if not configured
    private val apiKey = BuildConfig.GOLEMIO_API_KEY.ifEmpty { 
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NDUxOCwiaWF0IjoxNzY3NjE3Njk5LCJleHAiOjExNzY3NjE3Njk5LCJpc3MiOiJnb2xlbWlvIiwianRpIjoiNDM1YTljNjYtYjRjYi00ZTg4LWI5YWMtZjIxZTZjMWQzMDBjIn0.fCu1lP0jHBYXx_D0_qr5R_Vz4SoYosixXRlxk3Twzm8"
    }

    // Mock DAO pro testy
    private val mockDao = object : SavedStopDao {
        override fun getAllSavedStops() = flowOf(emptyList<com.example.piddepartures.data.local.entity.SavedStopEntity>())
        override suspend fun getSavedStopById(id: Long) = null
        override suspend fun insertSavedStop(stop: com.example.piddepartures.data.local.entity.SavedStopEntity) = 0L
        override suspend fun deleteSavedStop(stop: com.example.piddepartures.data.local.entity.SavedStopEntity) {}
        override suspend fun deleteSavedStopById(id: Long) {}
        override suspend fun deleteAllSavedStops() {}
    }

    @Before
    fun setup() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
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
        repository = DepartureRepository(apiService, mockDao)
    }

    @Test
    fun testSearchNamestiMiru() = runBlocking {
        println("\n=== TEST 1: Vyhledání 'chaplinovo' (bez diakritiky) ===")
        val result1 = repository.searchStops("chaplinovo")
        result1.fold(
            onSuccess = { stops ->
                println("Nalezeno zastávek: ${stops.size}")
                stops.take(5).forEach { 
                    println("  - ${it.stopName} (${it.stopId}) ${it.platformCode ?: ""}")
                }
                
                // Ověření, že jsme našli Chaplinovo náměstí
                val chaplinovo = stops.filter { it.stopName.contains("Chaplinovo", ignoreCase = true) }
                println("\nZastávky 'Chaplinovo náměstí': ${chaplinovo.size}")
                chaplinovo.forEach {
                    println("  - ${it.stopName} (${it.stopId}) ${it.platformCode ?: ""}")
                }
                
                assert(chaplinovo.isNotEmpty()) { "Nebyly nalezeny zastávky 'Chaplinovo náměstí'" }
            },
            onFailure = { error ->
                println("CHYBA: ${error.message}")
                throw error
            }
        )
        
        println("\n=== TEST 2: Vyhledání 'namesti' (bez diakritiky) ===")
        val result2 = repository.searchStops("namesti")
        result2.fold(
            onSuccess = { stops ->
                println("Nalezeno zastávek: ${stops.size}")
                println("První  3 zastávky:")
                stops.take(3).forEach { 
                    println("  - ${it.stopName} (${it.stopId}) ${it.platformCode ?: ""}")
                }
                
                assert(stops.isNotEmpty()) { "Nebyly nalezeny zastávky pro 'namesti'" }
                // Testujeme, že vyhledávání funguje bez diakritiky
                assert(stops.any { it.stopName.contains("náměstí", ignoreCase = true) }) { "Nebyla nalezena zastávka s 'náměstí'" }
            },
            onFailure = { error ->
                println("CHYBA: ${error.message}")
                throw error
            }
        )
        
        println("\n=== TEST 3: Vyhledání 'Václavské náměstí' (s diakritikou) ===")
        val result3 = repository.searchStops("Václavské náměstí")
        result3.fold(
            onSuccess = { stops ->
                println("Nalezeno zastávek: ${stops.size}")
                stops.forEach { 
                    println("  - ${it.stopName} (${it.stopId}) ${it.platformCode ?: ""}")
                }
                
                assert(stops.isNotEmpty()) { "Nebyly nalezeny zastávky pro 'Václavské náměstí'" }
            },
            onFailure = { error ->
                println("CHYBA: ${error.message}")
                throw error
            }
        )
        
        println("\n=== TEST 4: Vyhledání 'namesti miru' (bez diakritiky, 2 slova) ===")
        val result4 = repository.searchStops("namesti miru")
        result4.fold(
            onSuccess = { stops ->
                println("Nalezeno zastávek: ${stops.size}")
                stops.forEach { 
                    println("  - ${it.stopName} (${it.stopId}) ${it.platformCode ?: ""}")
                }
                
                assert(stops.isNotEmpty()) { "Nebyly nalezeny zastávky pro 'namesti miru'" }
                assert(stops.any { it.stopName == "Náměstí Míru" }) { 
                    "Nebyla nalezena přesná shoda 'Náměstí Míru'. Nalezené zastávky: ${stops.map { it.stopName }}" 
                }
            },
            onFailure = { error ->
                println("CHYBA: ${error.message}")
                throw error
            }
        )
        
        println("\n✅ Všechny testy prošly!")
        println("📝 Poznámka: Vyhledávání funguje bez diakritiky, cachuje zastávky a pro přesné dotazy používá API")
    }
}
