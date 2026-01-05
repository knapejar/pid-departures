package com.example.piddepartures.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piddepartures.data.repository.DepartureRepository
import com.example.piddepartures.domain.model.SavedStop
import com.example.piddepartures.domain.model.StopWithDepartures
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    
    private val _stopsWithDepartures = MutableStateFlow<List<StopWithDepartures>>(emptyList())
    val stopsWithDepartures: StateFlow<List<StopWithDepartures>> = _stopsWithDepartures.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private var refreshJob: Job? = null
    
    init {
        observeSavedStops()
        startAutoRefresh()
    }
    
    private fun observeSavedStops() {
        viewModelScope.launch {
            repository.getSavedStops().collect { savedStops ->
                _stopsWithDepartures.value = savedStops.map { stop ->
                    StopWithDepartures(
                        savedStop = stop,
                        departures = emptyList(),
                        isLoading = true
                    )
                }
                refreshDepartures()
            }
        }
    }
    
    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60000) // Refresh every 60 seconds
                refreshDepartures()
            }
        }
    }
    
    fun refreshDepartures() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val currentStops = _stopsWithDepartures.value
            
            val updated = currentStops.map { stopWithDepartures ->
                val result = repository.getDepartures(
                    stopId = stopWithDepartures.savedStop.stopId,
                    limit = 3
                )
                
                result.fold(
                    onSuccess = { departures ->
                        stopWithDepartures.copy(
                            departures = departures,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        stopWithDepartures.copy(
                            departures = emptyList(),
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
            
            _stopsWithDepartures.value = updated
            _isRefreshing.value = false
        }
    }
    
    fun reorderStops(fromIndex: Int, toIndex: Int) {
        val currentList = _stopsWithDepartures.value.toMutableList()
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        _stopsWithDepartures.value = currentList
        
        viewModelScope.launch {
            repository.updateStopsOrder(currentList.map { it.savedStop })
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
