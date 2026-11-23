package com.thermalguard.domain.usecase

import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.model.UserSettings
import javax.inject.Inject

class EvaluateThermalLevelUseCase @Inject constructor() {
    operator fun invoke(
        temperatureCelsius: Float,
        settings: UserSettings
    ): ThermalLevel {
        return ThermalLevel.fromTemperature(
            tempCelsius = temperatureCelsius,
            warmThreshold = settings.warmThreshold,
            hotThreshold = settings.hotThreshold,
            criticalThreshold = settings.criticalThreshold
        )
    }
}

