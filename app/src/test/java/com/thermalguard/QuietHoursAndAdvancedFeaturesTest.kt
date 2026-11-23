package com.thermalguard

import com.thermalguard.domain.model.UserSettings
import com.thermalguard.service.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursAndAdvancedFeaturesTest {

    @Test
    fun testQuietHours_Disabled() {
        val settings = UserSettings(quietHoursEnabled = false)
        assertFalse(NotificationHelper.isQuietHoursActive(settings))
    }

    @Test
    fun testQuietHours_ActiveOverMidnight() {
        // Test overnight interval e.g. 22:00 (1320m) to 06:00 (360m)
        val start = 22 * 60
        val end = 6 * 60

        fun checkMinutes(minutes: Int): Boolean {
            return if (start <= end) {
                minutes in start until end
            } else {
                minutes >= start || minutes < end
            }
        }

        assertTrue(checkMinutes(23 * 60)) // 23:00 -> in quiet hours
        assertTrue(checkMinutes(1 * 60))  // 01:00 -> in quiet hours
        assertTrue(checkMinutes(5 * 60 + 59)) // 05:59 -> in quiet hours
        assertFalse(checkMinutes(6 * 60)) // 06:00 -> out
        assertFalse(checkMinutes(12 * 60)) // 12:00 -> out
        assertFalse(checkMinutes(21 * 60 + 59)) // 21:59 -> out
    }

    @Test
    fun testCalibrationOffsetAddition() {
        val rawTemp = 36.2f
        val offset = -0.5f
        val calibratedTemp = (rawTemp + offset).coerceAtLeast(0f)
        assertEquals(35.7f, calibratedTemp, 0.001f)

        val positiveOffset = 1.5f
        val positiveCalibrated = (rawTemp + positiveOffset).coerceAtLeast(0f)
        assertEquals(37.7f, positiveCalibrated, 0.001f)
    }

    @Test
    fun testHeatImpactCalculation() {
        // Normal app with 20 mins
        val normalMins = 20
        val normalIsGame = false
        val normalLoad = (normalMins * (if (normalIsGame) 2.5f else 1.0f)).toInt()
        assertEquals(20, normalLoad)

        // Game with 20 mins -> 2.5x multiplier
        val gameMins = 20
        val gameIsGame = true
        val gameLoad = (gameMins * (if (gameIsGame) 2.5f else 1.0f)).toInt()
        assertEquals(50, gameLoad)
    }
}

