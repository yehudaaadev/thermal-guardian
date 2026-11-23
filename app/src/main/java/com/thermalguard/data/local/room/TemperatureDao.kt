package com.thermalguard.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TemperatureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(records: List<TemperatureEntity>)

    @Query("SELECT * FROM temperature_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<TemperatureEntity>>

    @Query("SELECT * FROM temperature_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<TemperatureEntity>

    @Query("SELECT * FROM temperature_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): Flow<TemperatureEntity?>

    @Query("SELECT * FROM temperature_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestRecords(limit: Int): List<TemperatureEntity>

    @Query("SELECT MAX(temperatureCelsius) FROM temperature_records WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getMaxTempBetween(startTime: Long, endTime: Long): Float?

    @Query("SELECT AVG(temperatureCelsius) FROM temperature_records WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAvgTempBetween(startTime: Long, endTime: Long): Float?

    @Query("SELECT MIN(temperatureCelsius) FROM temperature_records WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getMinTempBetween(startTime: Long, endTime: Long): Float?

    @Query("DELETE FROM temperature_records WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long): Int

    @Query("DELETE FROM temperature_records")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM temperature_records")
    fun getRecordCount(): Flow<Int>
}

