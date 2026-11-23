package com.thermalguard

import com.thermalguard.domain.usecase.CalculateThermalTrendUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateThermalTrendTest {

    private lateinit var useCase: CalculateThermalTrendUseCase

    @Before
    fun setUp() {
        useCase = CalculateThermalTrendUseCase()
    }

    @Test
    fun testEmptyOrSinglePoint_ReturnsZero() {
        val (rate1, isRapid1) = useCase(emptyList())
        assertEquals(0.0f, rate1, 0.001f)
        assertFalse(isRapid1)

        val (rate2, isRapid2) = useCase(listOf(Pair(1000L, 35.0f)))
        assertEquals(0.0f, rate2, 0.001f)
        assertFalse(isRapid2)
    }

    @Test
    fun testStableTemperature() {
        val points = listOf(
            Pair(0L, 35.0f),
            Pair(60_000L, 35.0f),
            Pair(120_000L, 35.1f)
        )
        val (rate, isRapid) = useCase(points)
        assertEquals(0.05f, rate, 0.01f)
        assertFalse(isRapid)
    }

    @Test
    fun testRapidHeating() {
        // Rises 1.5°C in 2 minutes -> rate = 0.75°C/min (> 0.5°C/min)
        val points = listOf(
            Pair(0L, 36.0f),
            Pair(60_000L, 36.8f),
            Pair(120_000L, 37.5f)
        )
        val (rate, isRapid) = useCase(points)
        assertEquals(0.75f, rate, 0.01f)
        assertTrue(isRapid)
    }

    @Test
    fun testCoolingDown() {
        // Drops 1.0°C in 2 minutes -> rate = -0.5°C/min
        val points = listOf(
            Pair(0L, 40.0f),
            Pair(60_000L, 39.5f),
            Pair(120_000L, 39.0f)
        )
        val (rate, isRapid) = useCase(points)
        assertEquals(-0.5f, rate, 0.01f)
        assertFalse(isRapid)
    }
}

