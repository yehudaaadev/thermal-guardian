package com.thermalguard.domain.usecase

import java.util.Locale
import javax.inject.Inject

class FormatTemperatureUseCase @Inject constructor() {
    /**
     * Formats Celsius temperature based on user settings (°C or °F).
     */
    operator fun invoke(celsius: Float, isCelsius: Boolean = true, includeUnit: Boolean = true): String {
        return if (isCelsius) {
            val formatted = String.format(Locale.US, "%.1f", celsius)
            if (includeUnit) "$formatted°C" else formatted
        } else {
            val fahrenheit = (celsius * 9f / 5f) + 32f
            val formatted = String.format(Locale.US, "%.1f", fahrenheit)
            if (includeUnit) "$formatted°F" else formatted
        }
    }
}

