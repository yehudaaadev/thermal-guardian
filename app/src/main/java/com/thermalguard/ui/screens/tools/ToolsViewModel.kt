package com.thermalguard.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.BatteryState
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.service.ThermalOverlayService
import com.thermalguard.util.StressTestResult
import com.thermalguard.util.StressTestRunner
import com.thermalguard.util.ThermalSensorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ToolsUiState(
    val isStressTesting: Boolean = false,
    val stressDurationOption: Int = 30, // 15, 30, 60
    val stressElapsedSeconds: Int = 0,
    val stressCurrentTemp: Float = 0f,
    val stressResult: StressTestResult? = null,
    val isCoolingTimerRunning: Boolean = false,
    val coolingElapsedSeconds: Int = 0,
    val coolingStartTemp: Float = 0f,
    val coolingCurrentTemp: Float = 0f,
    val isCoolingTargetReached: Boolean = false,
    val isOverlayEnabled: Boolean = false
)

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val repository: ThermalRepository,
    private val sensorManager: ThermalSensorManager,
    private val stressRunner: StressTestRunner
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    val liveThermalData: StateFlow<ThermalData> = repository.liveThermalData
    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    private var coolingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isOverlayEnabled = settings.isOverlayEnabled
                )
            }
        }
    }

    fun setStressDurationOption(duration: Int) {
        if (!_uiState.value.isStressTesting) {
            _uiState.value = _uiState.value.copy(stressDurationOption = duration)
        }
    }

    fun startStressTest(context: Context) {
        val duration = _uiState.value.stressDurationOption
        val currentTemp = sensorManager.readBatteryState(context).temperature

        _uiState.value = _uiState.value.copy(
            isStressTesting = true,
            stressElapsedSeconds = 0,
            stressCurrentTemp = currentTemp,
            stressResult = null
        )

        stressRunner.startTest(
            context = context,
            durationSeconds = duration,
            safetyAbortTemp = 42.0f,
            scope = viewModelScope,
            onProgress = { sample ->
                _uiState.value = _uiState.value.copy(
                    stressElapsedSeconds = sample.elapsedSeconds,
                    stressCurrentTemp = sample.currentTemp
                )
            },
            onComplete = { result ->
                _uiState.value = _uiState.value.copy(
                    isStressTesting = false,
                    stressResult = result
                )
            }
        )
    }

    fun cancelStressTest() {
        stressRunner.cancelTest()
        _uiState.value = _uiState.value.copy(
            isStressTesting = false
        )
    }

    fun startCoolingTimer(context: Context) {
        val currentTemp = sensorManager.readBatteryState(context).temperature
        _uiState.value = _uiState.value.copy(
            isCoolingTimerRunning = true,
            coolingElapsedSeconds = 0,
            coolingStartTemp = currentTemp,
            coolingCurrentTemp = currentTemp,
            isCoolingTargetReached = false
        )

        coolingJob?.cancel()
        coolingJob = viewModelScope.launch(Dispatchers.Default) {
            var elapsed = 0
            while (isActive) {
                delay(1000L)
                elapsed++
                val temp = sensorManager.readBatteryState(context).temperature
                val reached = (temp <= 36.0f) || ((currentTemp - temp) >= 1.5f)
                _uiState.value = _uiState.value.copy(
                    coolingElapsedSeconds = elapsed,
                    coolingCurrentTemp = temp,
                    isCoolingTargetReached = reached
                )
            }
        }
    }

    fun stopCoolingTimer() {
        coolingJob?.cancel()
        coolingJob = null
        _uiState.value = _uiState.value.copy(
            isCoolingTimerRunning = false
        )
    }

    fun toggleOverlay(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            repository.updateOverlayEnabled(enabled)
            val intent = Intent(context, ThermalOverlayService::class.java)
            if (enabled) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }

    fun generateDiagnosticReport(): String {
        val thermal = liveThermalData.value
        val battery = thermal.batteryState
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val healthText = when (battery.health) {
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good (Healthy)"
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Unknown"
        }

        return """
            ======================================
            🛡️ THERMALGUARD DIAGNOSTIC REPORT
            ======================================
            Generated: $dateString
            Device Model: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}
            Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            
            [THERMAL STATUS]
            Current Temperature: ${String.format("%.1f", battery.temperature)}°C
            Thermal Level: ${thermal.thermalLevel.name}
            SoC Thermal Throttling: ${thermal.systemThermalStatus.name}
            Heating Rate: ${String.format("%+.2f", thermal.tempRatePerMinute)}°C/min
            Thermal Headroom: ${if (thermal.thermalHeadroom.isNaN()) "N/A" else String.format("%.2f", thermal.thermalHeadroom)}
            
            [BATTERY HEALTH]
            Battery Level: ${battery.level}%
            Battery Health: $healthText
            Voltage: ${battery.voltageMv} mV
            Power Source: ${if (battery.isCharging) "Charging (${battery.plugged})" else "Discharging"}
            Technology: ${battery.technology}
            
            [ACTIVE HEAT SOURCES]
            Hotspot Active: ${thermal.isHotspotActive} (${thermal.hotspotDurationMillis / 60000} mins)
            Active Game: ${thermal.activeGamePackage ?: "None"}
            Rapid Heating: ${thermal.isRapidHeating}
            
            ThermalGuard - 100% On-Device Thermal Engine
            ======================================
        """.trimIndent()
    }

    override fun onCleared() {
        coolingJob?.cancel()
        stressRunner.cancelTest()
        super.onCleared()
    }
}

