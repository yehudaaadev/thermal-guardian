package com.thermalguard.data.repository

import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.domain.model.DailyThermalSummary
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ThermalRepository {
    val userSettings: Flow<UserSettings>
    val liveThermalData: StateFlow<ThermalData>

    fun updateLiveThermalData(data: ThermalData)

    suspend fun recordTemperature(record: TemperatureEntity): Long
    suspend fun recordBatch(records: List<TemperatureEntity>)

    fun getHistory(startTime: Long, endTime: Long): Flow<List<TemperatureEntity>>
    suspend fun getHistorySync(startTime: Long, endTime: Long): List<TemperatureEntity>
    suspend fun getRecentReadings(limit: Int): List<TemperatureEntity>

    suspend fun getMaxTemp(startTime: Long, endTime: Long): Float?
    suspend fun getAvgTemp(startTime: Long, endTime: Long): Float?
    suspend fun getMinTemp(startTime: Long, endTime: Long): Float?

    suspend fun deleteOldRecords(retentionDays: Int): Int
    suspend fun clearAllHistory(): Int
    fun getRecordCount(): Flow<Int>

    suspend fun updateThresholds(warm: Float, hot: Float, critical: Float)
    suspend fun updateIsCelsius(isCelsius: Boolean)
    suspend fun updateAlertSettings(sound: Boolean, vibration: Boolean, cooldownMinutes: Int)
    suspend fun updateRetentionDays(days: Int)
    suspend fun updateTheme(theme: String)
    suspend fun updateLanguage(language: String)
    suspend fun updateIntervalMode(mode: String)
    suspend fun updateTempOffset(offset: Float)
    suspend fun updateQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int)
    suspend fun updateChargeFullAlarm(enabled: Boolean, targetLevel: Int)
    suspend fun updateOverlayEnabled(enabled: Boolean)
    suspend fun updateOnboardingCompleted(completed: Boolean)
    suspend fun updateMonitoringEnabled(enabled: Boolean)

    suspend fun calculateDailySummary(startTime: Long, endTime: Long, safeTempThreshold: Float): DailyThermalSummary
}

