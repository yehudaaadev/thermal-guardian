package com.thermalguard.di

import android.content.Context
import androidx.room.Room
import com.thermalguard.data.local.room.TemperatureDao
import com.thermalguard.data.local.room.ThermalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideThermalDatabase(
        @ApplicationContext context: Context
    ): ThermalDatabase {
        return Room.databaseBuilder(
            context,
            ThermalDatabase::class.java,
            "thermal_guard.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTemperatureDao(database: ThermalDatabase): TemperatureDao {
        return database.temperatureDao()
    }
}

