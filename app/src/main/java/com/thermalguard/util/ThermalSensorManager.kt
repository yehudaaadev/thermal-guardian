package com.thermalguard.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.thermalguard.domain.model.BatteryState
import com.thermalguard.domain.model.SystemThermalStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThermalSensorManager @Inject constructor() {

    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var cachedThermalStatus: SystemThermalStatus = SystemThermalStatus.NONE

    /**
     * Reads current battery state synchronously using sticky Intent from registerReceiver(null).
     */
    fun readBatteryState(context: Context): BatteryState {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, filter) ?: return BatteryState()
        return parseBatteryIntent(batteryIntent)
    }

    /**
     * Parses an ACTION_BATTERY_CHANGED intent into BatteryState model.
     */
    fun parseBatteryIntent(intent: Intent): BatteryState {
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempCelsius = rawTemp / 10.0f

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            0
        }

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                plugged != 0

        return BatteryState(
            level = batteryPct,
            temperature = tempCelsius,
            voltageMv = voltage,
            health = health,
            status = status,
            plugged = plugged,
            technology = technology,
            isCharging = isCharging
        )
    }

    /**
     * Gets system thermal status via PowerManager (API 29+).
     */
    fun getSystemThermalStatus(context: Context): SystemThermalStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                return SystemThermalStatus.fromCode(powerManager.currentThermalStatus)
            }
        }
        return cachedThermalStatus
    }

    /**
     * Gets thermal headroom forecast (API 30+). Returns NaN if unavailable.
     */
    fun getThermalHeadroom(context: Context, forecastSeconds: Int = 30): Float {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                try {
                    return powerManager.getThermalHeadroom(forecastSeconds)
                } catch (e: Exception) {
                    // Ignored if device doesn't implement thermal headroom
                }
            }
        }
        return Float.NaN
    }

    /**
     * Registers a thermal status change listener on PowerManager (API 29+).
     */
    fun registerThermalListener(context: Context, onStatusChanged: (SystemThermalStatus) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            unregisterThermalListener(context)

            val listener = PowerManager.OnThermalStatusChangedListener { status ->
                val thermalStatus = SystemThermalStatus.fromCode(status)
                cachedThermalStatus = thermalStatus
                onStatusChanged(thermalStatus)
            }
            powerManager.addThermalStatusListener(context.mainExecutor, listener)
            thermalListener = listener
        }
    }

    /**
     * Unregisters the thermal status change listener.
     */
    fun unregisterThermalListener(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            thermalListener?.let {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.removeThermalStatusListener(it)
                thermalListener = null
            }
        }
    }
}

