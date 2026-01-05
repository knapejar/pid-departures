package com.example.piddepartures.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piddepartures.data.repository.DepartureRepository
import com.example.piddepartures.domain.model.SavedStop
import com.example.piddepartures.domain.model.StopInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStopViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<StopInfo>>(emptyList())
    val searchResults: StateFlow<List<StopInfo>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _selectedStop = MutableStateFlow<StopInfo?>(null)
    val selectedStop: StateFlow<StopInfo?> = _selectedStop.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            searchStops(query)
        } else {
            _searchResults.value = emptyList()
        }
    }
    
    private fun searchStops(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val result = repository.searchStops(query)
            result.fold(
                onSuccess = { stops ->
                    _searchResults.value = stops
                },
                onFailure = {
                    _searchResults.value = emptyList()
                }
            )
            _isSearching.value = false
        }
    }
    
    fun selectStop(stop: StopInfo) {
        _selectedStop.value = stop
    }
    
    fun saveStop() {
        viewModelScope.launch {
            val stop = _selectedStop.value ?: return@launch
            
            val savedStop = SavedStop(
                stopId = stop.stopId,
                stopName = stop.stopName,
                platformCode = stop.platformCode,
                routeShortName = null,
                routeType = null,
                direction = null
            )
            
            repository.addSavedStop(savedStop)
            _saveSuccess.value = true
        }
    }
    
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
