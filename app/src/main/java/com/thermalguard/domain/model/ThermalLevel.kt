package com.thermalguard.domain.model

import androidx.compose.ui.graphics.Color

enum class ThermalLevel(val defaultThreshold: Float, val color: Color) {
    COOL(0f, Color(0xFF4CAF50)),
    WARM(38f, Color(0xFFFFA726)),
    HOT(40f, Color(0xFFFF5722)),
    CRITICAL(43f, Color(0xFFD32F2F));

    companion object {
        fun fromTemperature(
            tempCelsius: Float,
            warmThreshold: Float = 38f,
            hotThreshold: Float = 40f,
            criticalThreshold: Float = 43f
        ): ThermalLevel {
            return when {
                tempCelsius >= criticalThreshold -> CRITICAL
                tempCelsius >= hotThreshold -> HOT
                tempCelsius >= warmThreshold -> WARM
                else -> COOL
            }
        }
    }
}

