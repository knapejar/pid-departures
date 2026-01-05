package com.example.piddepartures.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.piddepartures.data.repository.DepartureRepository
import com.google.gson.Gson
import javax.inject.Inject
import androidx.datastore.preferences.core.*
import androidx.hilt.work.HiltWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DepartureWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DepartureRepository,
    private val gson: Gson
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_STOP_ID = "stop_id"
        const val KEY_APP_WIDGET_ID = "app_widget_id"
    }

    override suspend fun doWork(): Result {
        return try {
            val stopId = inputData.getLong(KEY_STOP_ID, -1L)
            val appWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
            
            if (stopId == -1L) {
                return Result.failure()
            }
            
            if (appWidgetId == -1) {
                return Result.failure()
            }

            val stop = repository.getSavedStopById(stopId)
            
            if (stop == null) {
                return Result.failure()
            }
            
            val departuresResult = repository.getDepartures(stop.stopId, limit = 10)
            
            val glanceId = GlanceAppWidgetManager(context)
                .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[longPreferencesKey("stop_id")] = stopId
                prefs[stringPreferencesKey("stop_name")] = stop.stopName
                prefs[stringPreferencesKey("direction")] = stop.customDirection ?: stop.direction ?: ""
                
                departuresResult.fold(
                    onSuccess = { departures ->
                        prefs[booleanPreferencesKey("is_loading")] = false
                        prefs[stringPreferencesKey("departures")] = gson.toJson(departures)
                        prefs.remove(stringPreferencesKey("error"))
                    },
                    onFailure = { error ->
                        prefs[booleanPreferencesKey("is_loading")] = false
                        prefs[stringPreferencesKey("error")] = error.message ?: "Unknown error"
                    }
                )
            }

            DepartureWidget.update(context, glanceId)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
