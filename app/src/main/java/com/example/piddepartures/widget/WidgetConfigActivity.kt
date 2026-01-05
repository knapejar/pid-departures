package com.example.piddepartures.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.piddepartures.data.repository.DepartureRepository
import com.example.piddepartures.domain.model.SavedStop
import com.example.piddepartures.ui.theme.PIDDeparturesTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.datastore.preferences.core.*

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(Activity.RESULT_CANCELED)
        
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            PIDDeparturesTheme {
                WidgetConfigScreen(
                    onStopSelected = { stop ->
                        configureWidget(stop)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun configureWidget(stop: SavedStop) {
        lifecycleScope.launch {
            try {
                val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)
                
                // Nastavit počáteční stav widgetu
                updateAppWidgetState(this@WidgetConfigActivity, glanceId) { prefs ->
                    prefs[longPreferencesKey("stop_id")] = stop.id
                    prefs[stringPreferencesKey("stop_name")] = stop.stopName
                    prefs[stringPreferencesKey("direction")] = stop.customDirection ?: stop.direction ?: ""
                    prefs[booleanPreferencesKey("is_loading")] = true
                }
                
                DepartureWidget.update(this@WidgetConfigActivity, glanceId)
                
                // Naplánovat periodické aktualizace
                scheduleWidgetUpdates(stop.id)
                
                // Vrátit výsledek
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }
    
    private fun scheduleWidgetUpdates(stopId: Long) {
        val workManager = WorkManager.getInstance(this)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val updateRequest = PeriodicWorkRequestBuilder<DepartureWidgetWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(DepartureWidgetWorker.KEY_STOP_ID to stopId)
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "widget_update_$appWidgetId",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateRequest
        )
    }
}

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    
    private val _savedStops = MutableStateFlow<List<SavedStop>>(emptyList())
    val savedStops: StateFlow<List<SavedStop>> = _savedStops.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getSavedStops().collect { stops ->
                _savedStops.value = stops
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    onStopSelected: (SavedStop) -> Unit,
    onCancel: () -> Unit,
    viewModel: WidgetConfigViewModel = hiltViewModel()
) {
    val savedStops by viewModel.savedStops.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Stop for Widget") }
            )
        }
    ) { padding ->
        if (savedStops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "No saved stops",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Please add stops in the main app first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedStops) { stop ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStopSelected(stop) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stop.stopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val displayDirection = stop.customDirection ?: stop.direction
                            displayDirection?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            stop.platformCode?.let { platform ->
                                Text(
                                    text = "Platform: $platform",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
