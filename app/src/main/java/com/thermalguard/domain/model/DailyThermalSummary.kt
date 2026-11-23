package com.thermalguard.domain.model

data class DailyThermalSummary(
    val dateString: String,
    val maxTemp: Float,
    val minTemp: Float,
    val avgTemp: Float,
    val overheatCount: Int,
    val minutesAboveSafeTemp: Long,
    val peakHour: String
)

