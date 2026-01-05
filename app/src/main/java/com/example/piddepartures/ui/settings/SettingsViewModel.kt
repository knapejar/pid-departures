package com.example.piddepartures.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piddepartures.data.repository.DepartureRepository
import com.example.piddepartures.domain.model.SavedStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DepartureRepository
) : ViewModel() {
    
    private val _savedStop = MutableStateFlow<SavedStop?>(null)
    val savedStop: StateFlow<SavedStop?> = _savedStop.asStateFlow()
    
    private val _customDirection = MutableStateFlow("")
    val customDirection: StateFlow<String> = _customDirection.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    fun loadStop(stopId: Long) {
        viewModelScope.launch {
            val stop = repository.getSavedStopById(stopId)
            _savedStop.value = stop
            _customDirection.value = stop?.customDirection ?: ""
        }
    }
    
    fun updateCustomDirection(direction: String) {
        _customDirection.value = direction
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _isSaving.value = true
            _savedStop.value?.let { stop ->
                val updatedStop = stop.copy(
                    customDirection = _customDirection.value.trim().ifEmpty { null }
                )
                repository.updateSavedStop(updatedStop)
                _saveSuccess.value = true
            }
            _isSaving.value = false
        }
    }
}
