package com.thermalguard.domain.model

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val foregroundDurationMillis: Long,
    val isGame: Boolean = false,
    val lastTimeUsed: Long = 0L
) {
    val foregroundMinutes: Long
        get() = foregroundDurationMillis / (1000 * 60)
}

