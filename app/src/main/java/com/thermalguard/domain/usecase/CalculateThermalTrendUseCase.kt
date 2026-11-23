package com.thermalguard.domain.usecase

import javax.inject.Inject

class CalculateThermalTrendUseCase @Inject constructor() {
    /**
     * Calculates the temperature change rate in °C per minute based on recent readings.
     * @param recentPoints List of Pair(timestampMillis, temperatureCelsius), sorted ascending by timestamp.
     * @return Pair(ratePerMinute, isRapidHeating)
     */
    operator fun invoke(recentPoints: List<Pair<Long, Float>>): Pair<Float, Boolean> {
        if (recentPoints.size < 2) {
            return Pair(0.0f, false)
        }

        val first = recentPoints.first()
        val last = recentPoints.last()

        val timeDiffMillis = last.first - first.first
        if (timeDiffMillis < 10_000L) { // Less than 10 seconds, not enough delta
            return Pair(0.0f, false)
        }

        val tempDiff = last.second - first.second
        val minutes = timeDiffMillis / 60_000.0f
        val ratePerMinute = if (minutes > 0) tempDiff / minutes else 0.0f

        // Rapid heating threshold: > 0.5°C per minute or total rise > 1.0°C in <= 2 minutes
        val isRapid = ratePerMinute >= 0.5f && tempDiff >= 0.5f

        return Pair(ratePerMinute, isRapid)
    }
}

