package com.thermalguard.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.BatteryState
import com.thermalguard.domain.model.SystemThermalStatus
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.domain.usecase.CalculateThermalTrendUseCase
import com.thermalguard.domain.usecase.EvaluateThermalLevelUseCase
import com.thermalguard.util.GameDetector
import com.thermalguard.util.HotspotDetector
import com.thermalguard.util.ThermalSensorManager
import com.thermalguard.widget.ThermalWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.LinkedList
import javax.inject.Inject

@AndroidEntryPoint
class ThermalMonitorService : LifecycleService() {

    @Inject
    lateinit var repository: ThermalRepository

    @Inject
    lateinit var sensorManager: ThermalSensorManager

    @Inject
    lateinit var hotspotDetector: HotspotDetector

    @Inject
    lateinit var gameDetector: GameDetector

    @Inject
    lateinit var evaluateThermalLevel: EvaluateThermalLevelUseCase

    @Inject
    lateinit var calculateThermalTrend: CalculateThermalTrendUseCase

    private var monitoringJob: Job? = null
    private var isReceiverRegistered = false
    private var currentBatteryState: BatteryState = BatteryState()
    private var currentSystemThermalStatus: SystemThermalStatus = SystemThermalStatus.NONE
    private var cachedSettings: UserSettings = UserSettings()

    // Sliding window of recent readings: (timestamp, tempCelsius)
    private val recentReadings = LinkedList<Pair<Long, Float>>()

    private var lastRecordedTimestamp: Long = 0L
    private var lastRecordedTemp: Float = -999f

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                currentBatteryState = sensorManager.parseBatteryIntent(intent)
                processThermalSample()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        isReceiverRegistered = true

        lifecycleScope.launch {
            repository.userSettings.collect { settings ->
                cachedSettings = settings
                processThermalSample()
            }
        }

        sensorManager.registerThermalListener(this) { status ->
            currentSystemThermalStatus = status
            processThermalSample()
        }

        currentBatteryState = sensorManager.readBatteryState(this)
        currentSystemThermalStatus = sensorManager.getSystemThermalStatus(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Launch initial foreground notification
        lifecycleScope.launch {
            val settings = repository.userSettings.first()
            cachedSettings = settings
            val initialData = ThermalData(
                batteryState = currentBatteryState,
                systemThermalStatus = currentSystemThermalStatus,
                isServiceRunning = true
            )
            val notification = NotificationHelper.buildStatusNotification(
                this@ThermalMonitorService,
                initialData,
                settings
            )
            startForeground(NotificationHelper.NOTIFICATION_ID_STATUS, notification)
            startAdaptiveMonitoringLoop()
        }

        return START_STICKY
    }

    private fun startAdaptiveMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val currentData = processThermalSample()
                val settings = cachedSettings
                val interval = if (currentData.batteryState.isCharging) {
                    // Sleep/standby loop while charging to minimize CPU usage & maximize charge speed
                    60_000L
                } else {
                    when (settings.intervalMode) {
                        UserSettings.INTERVAL_15S -> 15_000L
                        UserSettings.INTERVAL_30S -> 30_000L
                        UserSettings.INTERVAL_60S -> 60_000L
                        else -> when (currentData.thermalLevel) {
                            ThermalLevel.COOL -> 30_000L
                            ThermalLevel.WARM -> 15_000L
                            ThermalLevel.HOT -> 5_000L
                            ThermalLevel.CRITICAL -> 3_000L
                        }
                    }
                }
                delay(interval)
            }
        }
    }

    private fun processThermalSample(): ThermalData {
        val now = System.currentTimeMillis()
        val rawBattery = currentBatteryState
        val calibratedTemp = (rawBattery.temperature + cachedSettings.tempOffset).coerceAtLeast(0f)
        val battery = rawBattery.copy(temperature = calibratedTemp)
        val temp = calibratedTemp

        // Update sliding window (keep last 10 points within 5 minutes)
        synchronized(recentReadings) {
            recentReadings.add(Pair(now, temp))
            while (recentReadings.size > 20 || (recentReadings.isNotEmpty() && now - recentReadings.first.first > 5 * 60 * 1000L)) {
                recentReadings.removeFirst()
            }
        }

        val (ratePerMinute, isRapid) = calculateThermalTrend(recentReadings.toList())

        val isHotspot = hotspotDetector.isHotspotActive(this)
        val hotspotDuration = hotspotDetector.getHotspotDurationMillis()

        val foregroundApp = gameDetector.getForegroundApp(this)
        val isGame = foregroundApp != null && gameDetector.isAppGame(this, foregroundApp)

        val headroom = sensorManager.getThermalHeadroom(this)
        val settings = cachedSettings

        val thermalLevel = evaluateThermalLevel(temp, settings)

        val isCharging = battery.isCharging
        val healthScore = run {
            var score = 100
            if (temp > 37.0f) {
                val delta = temp - 37.0f
                val penalty = when {
                    temp <= 40.0f -> (delta * 4f).toInt()
                    temp <= 43.0f -> 12 + ((temp - 40.0f) * 8f).toInt()
                    else -> 36 + ((temp - 43.0f) * 12f).toInt()
                }
                score -= penalty
            }
            if (isRapid) score -= 8
            if (isHotspot && temp > 39.0f) score -= 7
            score.coerceIn(10, 100)
        }

        val cooldownEst = if (temp > 39.0f) {
            val delta = temp - 38.5f
            val coolingRate = if (ratePerMinute < -0.2f) kotlin.math.abs(ratePerMinute) else 1.0f
            kotlin.math.ceil(delta / coolingRate).toInt().coerceIn(1, 20)
        } else {
            0
        }

        val thermalData = ThermalData(
            batteryState = battery,
            thermalLevel = thermalLevel,
            systemThermalStatus = currentSystemThermalStatus,
            thermalHeadroom = headroom,
            tempRatePerMinute = ratePerMinute,
            isHotspotActive = isHotspot,
            hotspotDurationMillis = hotspotDuration,
            activeGamePackage = if (isGame) foregroundApp else null,
            isRapidHeating = isRapid,
            isServiceRunning = true,
            isChargingPaused = isCharging,
            thermalHealthScore = healthScore,
            hotspotCooldownEstimateMinutes = cooldownEst,
            lastUpdated = now
        )

        // Update repository live state
        repository.updateLiveThermalData(thermalData)

        // Update notification
        lifecycleScope.launch(Dispatchers.Main) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null) {
                val notification = NotificationHelper.buildStatusNotification(
                    this@ThermalMonitorService,
                    thermalData,
                    settings
                )
                notificationManager.notify(NotificationHelper.NOTIFICATION_ID_STATUS, notification)
            }

            // Post alerts if needed
            NotificationHelper.checkAndPostAlerts(this@ThermalMonitorService, thermalData, settings)

            // Update Widgets
            ThermalWidgetProvider.updateAllWidgets(this@ThermalMonitorService, thermalData)
        }

        // Database write strategy:
        // Record if >= 30 seconds since last record OR if temperature changed significantly (|delta| >= 0.5°C)
        val shouldRecord = (now - lastRecordedTimestamp >= 30_000L) ||
                (kotlin.math.abs(temp - lastRecordedTemp) >= 0.5f && now - lastRecordedTimestamp >= 10_000L)

        if (shouldRecord && temp > 0f) {
            lastRecordedTimestamp = now
            lastRecordedTemp = temp

            lifecycleScope.launch(Dispatchers.IO) {
                repository.recordTemperature(
                    TemperatureEntity(
                        timestamp = now,
                        temperatureCelsius = temp,
                        thermalStatus = currentSystemThermalStatus.code,
                        batteryLevel = battery.level,
                        isCharging = battery.isCharging,
                        isHotspotOn = isHotspot,
                        isGaming = isGame
                    )
                )
            }
        }

        return thermalData
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Spotify-style: automatically stop monitoring service when app is swiped away from recents
        stopSelf()
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                // Already unregistered
            }
            isReceiverRegistered = false
        }
        sensorManager.unregisterThermalListener(this)

        // Update repository state that service stopped
        val stoppedData = repository.liveThermalData.value.copy(isServiceRunning = false)
        repository.updateLiveThermalData(stoppedData)
        ThermalWidgetProvider.updateAllWidgets(this, stoppedData)

        super.onDestroy()
    }
}

