package com.example.piddepartures.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.LocalSize
import androidx.glance.LocalContext
import com.example.piddepartures.MainActivity
import com.example.piddepartures.domain.model.Departure
import com.example.piddepartures.domain.model.getRouteTypeIcon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DepartureWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Responsive(
        setOf(
            /* Small */ DpSize(120.dp, 120.dp),
            /* Medium */ DpSize(200.dp, 120.dp),
            /* Large */ DpSize(280.dp, 200.dp),
            /* Extra Large */ DpSize(280.dp, 300.dp)
        )
    )
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }
    
    @Composable
    private fun WidgetContent() {
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val size = LocalSize.current
        val context = LocalContext.current
        val stopId = prefs[longPreferencesKey("stop_id")] ?: 0L
        val stopName = prefs[androidx.datastore.preferences.core.stringPreferencesKey("stop_name")] ?: "PID Departures"
        val direction = prefs[androidx.datastore.preferences.core.stringPreferencesKey("direction")]
        val isLoading = prefs[androidx.datastore.preferences.core.booleanPreferencesKey("is_loading")] ?: false
        val error = prefs[androidx.datastore.preferences.core.stringPreferencesKey("error")]
        val departuresJson = prefs[androidx.datastore.preferences.core.stringPreferencesKey("departures")]
        
        // Určit počet zobrazených odjezdů podle velikosti widgetu
        val maxDepartures = when {
            size.height > 250.dp -> 8
            size.height > 180.dp -> 5
            size.height > 120.dp -> 3
            else -> 2
        }
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(
                    actionStartActivity(
                        Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            putExtra("stopId", stopId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                )
        ) {
            // Header
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = stopName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onBackground
                    ),
                    maxLines = 1
                )
                direction?.let {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.primary
                        ),
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Loading...",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                error != null -> {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading",
                            style = TextStyle(
                                color = GlanceTheme.colors.error,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                departuresJson != null -> {
                    DeparturesList(departuresJson, maxDepartures)
                }
                else -> {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No departures",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun DeparturesList(departuresJson: String, maxDepartures: Int) {
        val gson = Gson()
        val type = object : TypeToken<List<Departure>>() {}.type
        val departures: List<Departure>? = try {
            gson.fromJson(departuresJson, type)
        } catch (e: Exception) {
            null
        }
        
        when {
            departures == null -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error parsing data",
                        style = TextStyle(
                            color = GlanceTheme.colors.error,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            departures.isEmpty() -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming departures",
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            else -> {
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    departures.take(maxDepartures).forEachIndexed { index, departure ->
                        DepartureRow(departure)
                        if (index < departures.size - 1 && index < maxDepartures - 1) {
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun DepartureRow(departure: Departure) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(8.dp)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = getRouteTypeIcon(departure.routeType),
                style = TextStyle(fontSize = 14.sp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            
            Text(
                text = departure.routeShortName,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
            
            Spacer(modifier = GlanceModifier.width(6.dp))
            
            Text(
                text = departure.headsign,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.onPrimaryContainer
                ),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1
            )
            
            Text(
                text = "${departure.minutes} min",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (departure.isDelayed) GlanceTheme.colors.error else GlanceTheme.colors.primary
                )
            )
        }
    }
}

class DepartureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DepartureWidget
}
