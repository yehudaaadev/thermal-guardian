package com.thermalguard

import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FormatTemperatureTest {

    private lateinit var useCase: FormatTemperatureUseCase

    @Before
    fun setUp() {
        useCase = FormatTemperatureUseCase()
    }

    @Test
    fun testCelsiusFormatting() {
        assertEquals("38.5°C", useCase(38.5f, isCelsius = true, includeUnit = true))
        assertEquals("38.5", useCase(38.5f, isCelsius = true, includeUnit = false))
        assertEquals("0.0°C", useCase(0.0f, isCelsius = true, includeUnit = true))
    }

    @Test
    fun testFahrenheitFormatting() {
        // 38.0°C -> (38 * 9/5) + 32 = 68.4 + 32 = 100.4°F
        assertEquals("100.4°F", useCase(38.0f, isCelsius = false, includeUnit = true))
        assertEquals("100.4", useCase(38.0f, isCelsius = false, includeUnit = false))
        // 0.0°C -> 32.0°F
        assertEquals("32.0°F", useCase(0.0f, isCelsius = false, includeUnit = true))
    }
}

