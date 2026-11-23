package com.thermalguard.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.thermalguard.R
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class ThermalTileService : TileService() {

    @Inject
    lateinit var repository: ThermalRepository

    @Inject
    lateinit var formatTemp: FormatTemperatureUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var observeJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            repository.liveThermalData.collect { data ->
                val settings = repository.userSettings.first()
                updateTile(data.isServiceRunning, data.batteryState.temperature, settings.isCelsius, data.thermalLevel)
            }
        }
    }

    override fun onStopListening() {
        observeJob?.cancel()
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val currentData = repository.liveThermalData.value
        val isRunning = currentData.isServiceRunning

        serviceScope.launch {
            if (isRunning) {
                // Stop service
                val intent = Intent(this@ThermalTileService, ThermalMonitorService::class.java)
                stopService(intent)
                repository.updateMonitoringEnabled(false)
            } else {
                // Start service
                val intent = Intent(this@ThermalTileService, ThermalMonitorService::class.java)
                ContextCompat.startForegroundService(this@ThermalTileService, intent)
                repository.updateMonitoringEnabled(true)
            }
        }
    }

    private fun updateTile(
        isRunning: Boolean,
        tempCelsius: Float,
        isCelsius: Boolean,
        level: ThermalLevel
    ) {
        val tile = qsTile ?: return

        if (isRunning) {
            tile.state = Tile.STATE_ACTIVE
            val tempStr = formatTemp(tempCelsius, isCelsius)
            tile.label = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.qs_tile_state_active, tempStr)
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.qs_tile_state_inactive)
            }
        }

        tile.updateTile()
    }
}

