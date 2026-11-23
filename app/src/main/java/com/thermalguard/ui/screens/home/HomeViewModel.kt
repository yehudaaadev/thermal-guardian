package com.thermalguard.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.CoolingSuggestion
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.service.ThermalMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val thermalData: ThermalData = ThermalData(),
    val userSettings: UserSettings = UserSettings(),
    val suggestions: List<CoolingSuggestion> = emptyList(),
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ThermalRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.liveThermalData,
        repository.userSettings
    ) { thermalData, settings ->
        val suggestions = CoolingSuggestion.getApplicableSuggestions(thermalData)
        HomeUiState(
            thermalData = thermalData,
            userSettings = settings,
            suggestions = suggestions,
            isServiceRunning = thermalData.isServiceRunning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleMonitoringService(context: Context) {
        val currentState = uiState.value.isServiceRunning
        viewModelScope.launch {
            if (currentState) {
                val intent = Intent(context, ThermalMonitorService::class.java)
                context.stopService(intent)
                repository.updateMonitoringEnabled(false)
            } else {
                val intent = Intent(context, ThermalMonitorService::class.java)
                ContextCompat.startForegroundService(context, intent)
                repository.updateMonitoringEnabled(true)
            }
        }
    }

    fun toggleOverlay(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateOverlayEnabled(enabled)
            val intent = Intent(context, com.thermalguard.service.ThermalOverlayService::class.java)
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(context)) {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }
}

