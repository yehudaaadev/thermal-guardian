package com.thermalguard.domain.model

data class UserSettings(
    val warmThreshold: Float = 38.0f,
    val hotThreshold: Float = 40.0f,
    val criticalThreshold: Float = 43.0f,
    val isCelsius: Boolean = true,
    val alertSound: Boolean = true,
    val alertVibration: Boolean = true,
    val cooldownMinutes: Int = 5,
    val retentionDays: Int = 30,
    val theme: String = THEME_SYSTEM,
    val language: String = LANG_SYSTEM,
    val intervalMode: String = INTERVAL_ADAPTIVE,
    val tempOffset: Float = 0.0f,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = 22 * 60,
    val quietHoursEndMinutes: Int = 6 * 60,
    val chargeFullAlarmEnabled: Boolean = true,
    val targetChargeLevel: Int = 80,
    val isOverlayEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isMonitoringEnabled: Boolean = false
) {
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val LANG_SYSTEM = "system"
        const val LANG_ID = "in"
        const val LANG_EN = "en"

        const val INTERVAL_ADAPTIVE = "adaptive"
        const val INTERVAL_15S = "15s"
        const val INTERVAL_30S = "30s"
        const val INTERVAL_60S = "60s"
    }
}

