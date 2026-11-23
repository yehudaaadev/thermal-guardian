package com.thermalguard.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temperature_records",
    indices = [Index(value = ["timestamp"])]
)
data class TemperatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val temperatureCelsius: Float,
    val thermalStatus: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isHotspotOn: Boolean,
    val isGaming: Boolean
)

