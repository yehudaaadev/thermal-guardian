package com.thermalguard

import com.thermalguard.data.local.datastore.SettingsDataStore
import com.thermalguard.data.local.room.TemperatureDao
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.data.repository.ThermalRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DailySummaryCalculationTest {

    private lateinit var temperatureDao: TemperatureDao
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: ThermalRepositoryImpl

    @Before
    fun setUp() {
        temperatureDao = mockk(relaxed = true)
        settingsDataStore = mockk(relaxed = true)
        repository = ThermalRepositoryImpl(temperatureDao, settingsDataStore)
    }

    @Test
    fun testEmptyRecords_ReturnsZeroSummary() = runTest {
        coEvery { temperatureDao.getRecordsBetweenSync(any(), any()) } returns emptyList()

        val summary = repository.calculateDailySummary(1000L, 2000L, 38.0f)
        assertEquals(0.0f, summary.maxTemp, 0.01f)
        assertEquals(0.0f, summary.minTemp, 0.01f)
        assertEquals(0.0f, summary.avgTemp, 0.01f)
        assertEquals(0, summary.overheatCount)
        assertEquals(0L, summary.minutesAboveSafeTemp)
    }

    @Test
    fun testSummaryWithRecords_ComputesMetricsAccurately() = runTest {
        val baseTime = 1_700_000_000_000L
        val mockRecords = listOf(
            TemperatureEntity(1, baseTime, 35.0f, 0, 80, isCharging = false, isHotspotOn = false, isGaming = false),
            TemperatureEntity(2, baseTime + 60_000L, 37.0f, 0, 79, isCharging = false, isHotspotOn = false, isGaming = false),
            TemperatureEntity(3, baseTime + 120_000L, 39.0f, 1, 78, isCharging = false, isHotspotOn = false, isGaming = false), // Overheat
            TemperatureEntity(4, baseTime + 180_000L, 41.0f, 2, 77, isCharging = false, isHotspotOn = false, isGaming = false), // Overheat
            TemperatureEntity(5, baseTime + 240_000L, 36.0f, 0, 76, isCharging = false, isHotspotOn = false, isGaming = false)  // Normal again
        )

        coEvery { temperatureDao.getRecordsBetweenSync(any(), any()) } returns mockRecords

        val summary = repository.calculateDailySummary(baseTime, baseTime + 300_000L, 38.0f)

        assertEquals(41.0f, summary.maxTemp, 0.01f)
        assertEquals(35.0f, summary.minTemp, 0.01f)
        // Average: (35 + 37 + 39 + 41 + 36) / 5 = 188 / 5 = 37.6f
        assertEquals(37.6f, summary.avgTemp, 0.01f)
        // Transitions above 38.0°C: 1 event
        assertEquals(1, summary.overheatCount)
    }
}

