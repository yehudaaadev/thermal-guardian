package com.thermalguard.domain.model

data class BatteryState(
    val level: Int = 0,
    val temperature: Float = 0.0f, // in Celsius
    val voltageMv: Int = 0,
    val health: Int = 1, // BatteryManager.BATTERY_HEALTH_*
    val status: Int = 1, // BatteryManager.BATTERY_STATUS_*
    val plugged: Int = 0, // BatteryManager.BATTERY_PLUGGED_*
    val technology: String = "Li-ion",
    val isCharging: Boolean = false
) {
    val voltageVolts: Float
        get() = voltageMv / 1000.0f
}

