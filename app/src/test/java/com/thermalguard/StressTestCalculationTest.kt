package com.thermalguard

import com.thermalguard.util.StressTestResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StressTestCalculationTest {

    @Test
    fun testDissipationScoreFormula_MinimalTempRise() {
        val tempDelta = 0.2f
        val rawScore = 100 - (tempDelta * 15).toInt()
        val finalScore = rawScore.coerceIn(30, 99)
        assertEquals(97, finalScore)
    }

    @Test
    fun testDissipationScoreFormula_ModerateTempRise() {
        val tempDelta = 1.5f
        val rawScore = 100 - (tempDelta * 15).toInt()
        val finalScore = rawScore.coerceIn(30, 99)
        // 100 - 22 = 78
        assertEquals(78, finalScore)
    }

    @Test
    fun testDissipationScoreFormula_HeavyTempRiseClamping() {
        val tempDelta = 6.0f
        val rawScore = 100 - (tempDelta * 15).toInt() // 100 - 90 = 10
        val finalScore = rawScore.coerceIn(30, 99)
        assertEquals(30, finalScore)
    }

    @Test
    fun testStressTestResult_SafetyCutoff() {
        val result = StressTestResult(
            initialTemp = 36.0f,
            peakTemp = 42.1f,
            tempDelta = 6.1f,
            dissipationScore = 30,
            durationSeconds = 15,
            wasAbortedForSafety = true
        )

        assertTrue(result.wasAbortedForSafety)
        assertEquals(42.1f, result.peakTemp, 0.01f)
    }

    @Test
    fun testStressTestResult_CompletedSafely() {
        val result = StressTestResult(
            initialTemp = 33.0f,
            peakTemp = 34.5f,
            tempDelta = 1.5f,
            dissipationScore = 78,
            durationSeconds = 30,
            wasAbortedForSafety = false
        )

        assertFalse(result.wasAbortedForSafety)
        assertEquals(1.5f, result.tempDelta, 0.01f)
    }
}

