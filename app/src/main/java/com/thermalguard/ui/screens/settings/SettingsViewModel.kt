package com.thermalguard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ThermalRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun updateThresholds(warm: Float, hot: Float, critical: Float) {
        viewModelScope.launch {
            repository.updateThresholds(warm, hot, critical)
        }
    }

    fun updateIsCelsius(isCelsius: Boolean) {
        viewModelScope.launch {
            repository.updateIsCelsius(isCelsius)
        }
    }

    fun updateAlertSettings(sound: Boolean, vibration: Boolean, cooldownMinutes: Int) {
        viewModelScope.launch {
            repository.updateAlertSettings(sound, vibration, cooldownMinutes)
        }
    }

    fun updateRetentionDays(days: Int) {
        viewModelScope.launch {
            repository.updateRetentionDays(days)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            repository.updateTheme(theme)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun updateIntervalMode(mode: String) {
        viewModelScope.launch {
            repository.updateIntervalMode(mode)
        }
    }

    fun updateTempOffset(offset: Float) {
        viewModelScope.launch {
            repository.updateTempOffset(offset)
        }
    }

    fun updateQuietHours(enabled: Boolean, startMinutes: Int = 22 * 60, endMinutes: Int = 6 * 60) {
        viewModelScope.launch {
            repository.updateQuietHours(enabled, startMinutes, endMinutes)
        }
    }

    fun updateChargeFullAlarm(enabled: Boolean, targetLevel: Int) {
        viewModelScope.launch {
            repository.updateChargeFullAlarm(enabled, targetLevel)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
        }
    }
}

