package com.thermalguard.domain.model

data class ThermalData(
    val batteryState: BatteryState = BatteryState(),
    val thermalLevel: ThermalLevel = ThermalLevel.COOL,
    val systemThermalStatus: SystemThermalStatus = SystemThermalStatus.NONE,
    val thermalHeadroom: Float = Float.NaN, // Headroom from PowerManager (API 30+)
    val tempRatePerMinute: Float = 0.0f, // Rate of temperature change per minute (°C/min)
    val isHotspotActive: Boolean = false,
    val hotspotDurationMillis: Long = 0L,
    val activeGamePackage: String? = null,
    val isRapidHeating: Boolean = false,
    val isServiceRunning: Boolean = false,
    val isChargingPaused: Boolean = false,
    val thermalHealthScore: Int = 100,
    val hotspotCooldownEstimateMinutes: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

