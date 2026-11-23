package com.thermalguard

import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.domain.usecase.EvaluateThermalLevelUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ThermalLevelEvaluationTest {

    private lateinit var useCase: EvaluateThermalLevelUseCase

    @Before
    fun setUp() {
        useCase = EvaluateThermalLevelUseCase()
    }

    @Test
    fun testDefaultThresholds_Cool() {
        val settings = UserSettings(warmThreshold = 38.0f, hotThreshold = 40.0f, criticalThreshold = 43.0f)
        val level = useCase(35.5f, settings)
        assertEquals(ThermalLevel.COOL, level)
    }

    @Test
    fun testDefaultThresholds_Warm() {
        val settings = UserSettings(warmThreshold = 38.0f, hotThreshold = 40.0f, criticalThreshold = 43.0f)
        val level = useCase(38.0f, settings)
        assertEquals(ThermalLevel.WARM, level)
    }

    @Test
    fun testDefaultThresholds_Hot() {
        val settings = UserSettings(warmThreshold = 38.0f, hotThreshold = 40.0f, criticalThreshold = 43.0f)
        val level = useCase(41.2f, settings)
        assertEquals(ThermalLevel.HOT, level)
    }

    @Test
    fun testDefaultThresholds_Critical() {
        val settings = UserSettings(warmThreshold = 38.0f, hotThreshold = 40.0f, criticalThreshold = 43.0f)
        val level = useCase(43.5f, settings)
        assertEquals(ThermalLevel.CRITICAL, level)
    }

    @Test
    fun testCustomThresholds() {
        val customSettings = UserSettings(warmThreshold = 36.0f, hotThreshold = 39.0f, criticalThreshold = 41.0f)
        assertEquals(ThermalLevel.COOL, useCase(35.9f, customSettings))
        assertEquals(ThermalLevel.WARM, useCase(36.0f, customSettings))
        assertEquals(ThermalLevel.HOT, useCase(39.5f, customSettings))
        assertEquals(ThermalLevel.CRITICAL, useCase(41.0f, customSettings))
    }
}

