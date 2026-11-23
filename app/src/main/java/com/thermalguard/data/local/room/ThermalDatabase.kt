package com.thermalguard.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TemperatureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ThermalDatabase : RoomDatabase() {
    abstract fun temperatureDao(): TemperatureDao
}

