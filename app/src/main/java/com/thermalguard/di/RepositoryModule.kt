package com.thermalguard.di

import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.data.repository.ThermalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThermalRepository(
        impl: ThermalRepositoryImpl
    ): ThermalRepository
}

