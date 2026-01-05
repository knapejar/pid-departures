package com.example.piddepartures.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piddepartures.data.repository.DepartureRepository
import com.example.piddepartures.domain.model.Departure
import com.example.piddepartures.domain.model.SavedStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    
    private val _savedStop = MutableStateFlow<SavedStop?>(null)
    val savedStop: StateFlow<SavedStop?> = _savedStop.asStateFlow()
    
    private val _departures = MutableStateFlow<List<Departure>>(emptyList())
    val departures: StateFlow<List<Departure>> = _departures.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()
    
    fun loadStop(stopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val stop = repository.getSavedStopById(stopId)
            _savedStop.value = stop
            
            stop?.let {
                loadDepartures(it.stopId)
            }
        }
    }
    
    private fun loadDepartures(stopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getDepartures(stopId, limit = 10)
            
            result.fold(
                onSuccess = { departures ->
                    _departures.value = departures
                    _error.value = null
                },
                onFailure = { error ->
                    _departures.value = emptyList()
                    _error.value = error.message
                }
            )
            _isLoading.value = false
        }
    }
    
    fun deleteStop() {
        viewModelScope.launch {
            _savedStop.value?.let { stop ->
                repository.deleteSavedStop(stop.id)
                _deleteSuccess.value = true
            }
        }
    }
    
    fun refresh() {
        _savedStop.value?.let { stop ->
            loadDepartures(stop.stopId)
        }
    }
}
