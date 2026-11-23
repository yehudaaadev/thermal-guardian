package com.thermalguard.data.repository

import com.thermalguard.data.local.datastore.SettingsDataStore
import com.thermalguard.data.local.room.TemperatureDao
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.domain.model.DailyThermalSummary
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThermalRepositoryImpl @Inject constructor(
    private val temperatureDao: TemperatureDao,
    private val settingsDataStore: SettingsDataStore
) : ThermalRepository {

    private val _liveThermalData = MutableStateFlow(ThermalData())
    override val liveThermalData: StateFlow<ThermalData> = _liveThermalData.asStateFlow()

    override val userSettings: Flow<UserSettings> = settingsDataStore.userSettingsFlow

    override fun updateLiveThermalData(data: ThermalData) {
        _liveThermalData.value = data
    }

    override suspend fun recordTemperature(record: TemperatureEntity): Long {
        return temperatureDao.insertRecord(record)
    }

    override suspend fun recordBatch(records: List<TemperatureEntity>) {
        temperatureDao.insertBatch(records)
    }

    override fun getHistory(startTime: Long, endTime: Long): Flow<List<TemperatureEntity>> {
        return temperatureDao.getRecordsBetween(startTime, endTime)
    }

    override suspend fun getHistorySync(startTime: Long, endTime: Long): List<TemperatureEntity> {
        return temperatureDao.getRecordsBetweenSync(startTime, endTime)
    }

    override suspend fun getRecentReadings(limit: Int): List<TemperatureEntity> {
        return temperatureDao.getLatestRecords(limit)
    }

    override suspend fun getMaxTemp(startTime: Long, endTime: Long): Float? {
        return temperatureDao.getMaxTempBetween(startTime, endTime)
    }

    override suspend fun getAvgTemp(startTime: Long, endTime: Long): Float? {
        return temperatureDao.getAvgTempBetween(startTime, endTime)
    }

    override suspend fun getMinTemp(startTime: Long, endTime: Long): Float? {
        return temperatureDao.getMinTempBetween(startTime, endTime)
    }

    override suspend fun deleteOldRecords(retentionDays: Int): Int {
        val cutoff = System.currentTimeMillis() - (retentionDays.toLong() * 24 * 60 * 60 * 1000)
        return temperatureDao.deleteOlderThan(cutoff)
    }

    override suspend fun clearAllHistory(): Int {
        return temperatureDao.deleteAll()
    }

    override fun getRecordCount(): Flow<Int> {
        return temperatureDao.getRecordCount()
    }

    override suspend fun updateThresholds(warm: Float, hot: Float, critical: Float) {
        settingsDataStore.updateThresholds(warm, hot, critical)
    }

    override suspend fun updateIsCelsius(isCelsius: Boolean) {
        settingsDataStore.updateIsCelsius(isCelsius)
    }

    override suspend fun updateAlertSettings(sound: Boolean, vibration: Boolean, cooldownMinutes: Int) {
        settingsDataStore.updateAlertSettings(sound, vibration, cooldownMinutes)
    }

    override suspend fun updateRetentionDays(days: Int) {
        settingsDataStore.updateRetentionDays(days)
    }

    override suspend fun updateTheme(theme: String) {
        settingsDataStore.updateTheme(theme)
    }

    override suspend fun updateLanguage(language: String) {
        settingsDataStore.updateLanguage(language)
    }

    override suspend fun updateIntervalMode(mode: String) {
        settingsDataStore.updateIntervalMode(mode)
    }

    override suspend fun updateTempOffset(offset: Float) {
        settingsDataStore.updateTempOffset(offset)
    }

    override suspend fun updateQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        settingsDataStore.updateQuietHours(enabled, startMinutes, endMinutes)
    }

    override suspend fun updateChargeFullAlarm(enabled: Boolean, targetLevel: Int) {
        settingsDataStore.updateChargeFullAlarm(enabled, targetLevel)
    }

    override suspend fun updateOverlayEnabled(enabled: Boolean) {
        settingsDataStore.updateOverlayEnabled(enabled)
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        settingsDataStore.updateOnboardingCompleted(completed)
    }

    override suspend fun updateMonitoringEnabled(enabled: Boolean) {
        settingsDataStore.updateMonitoringEnabled(enabled)
    }

    override suspend fun calculateDailySummary(
        startTime: Long,
        endTime: Long,
        safeTempThreshold: Float
    ): DailyThermalSummary {
        val records = temperatureDao.getRecordsBetweenSync(startTime, endTime)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(startTime))

        if (records.isEmpty()) {
            return DailyThermalSummary(
                dateString = dateString,
                maxTemp = 0.0f,
                minTemp = 0.0f,
                avgTemp = 0.0f,
                overheatCount = 0,
                minutesAboveSafeTemp = 0L,
                peakHour = "--:--"
            )
        }

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE
        var sum = 0.0
        var overheatEvents = 0
        var wasOverheating = false
        var highestTempRecord: TemperatureEntity? = null

        // Calculate minutes above safe temp based on interval deltas
        var totalMillisAboveSafe = 0L

        for (i in records.indices) {
            val r = records[i]
            val temp = r.temperatureCelsius
            if (temp > max) {
                max = temp
                highestTempRecord = r
            }
            if (temp < min) {
                min = temp
            }
            sum += temp

            val isOver = temp >= safeTempThreshold
            if (isOver && !wasOverheating) {
                overheatEvents++
            }
            wasOverheating = isOver

            if (isOver && i > 0) {
                val delta = r.timestamp - records[i - 1].timestamp
                // Cap delta to 5 mins in case service was stopped
                if (delta in 1..300_000L) {
                    totalMillisAboveSafe += delta
                } else {
                    totalMillisAboveSafe += 30_000L // Default estimate 30s
                }
            }
        }

        val avg = (sum / records.size).toFloat()
        val minutesAbove = totalMillisAboveSafe / 60_000L

        val peakHour = highestTempRecord?.let {
            val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            hourFormat.format(Date(it.timestamp))
        } ?: "--:--"

        return DailyThermalSummary(
            dateString = dateString,
            maxTemp = if (max == Float.MIN_VALUE) 0f else max,
            minTemp = if (min == Float.MAX_VALUE) 0f else min,
            avgTemp = avg,
            overheatCount = overheatEvents,
            minutesAboveSafeTemp = minutesAbove,
            peakHour = peakHour
        )
    }
}

