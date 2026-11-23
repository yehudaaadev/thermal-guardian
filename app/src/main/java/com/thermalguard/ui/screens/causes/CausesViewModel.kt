package com.thermalguard.ui.screens.causes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.AppUsageInfo
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.util.GameDetector
import com.thermalguard.util.HotspotDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CausesUiState(
    val thermalData: ThermalData = ThermalData(),
    val hasUsageStatsPermission: Boolean = false,
    val topActiveApps: List<AppUsageInfo> = emptyList(),
    val isHotspotActive: Boolean = false,
    val hotspotDurationMillis: Long = 0L,
    val activeGamePackage: String? = null,
    val isRapidHeating: Boolean = false
)

@HiltViewModel
class CausesViewModel @Inject constructor(
    private val repository: ThermalRepository,
    private val hotspotDetector: HotspotDetector,
    private val gameDetector: GameDetector
) : ViewModel() {

    private val _hasUsagePermission = MutableStateFlow(false)
    private val _topApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())

    val uiState: StateFlow<CausesUiState> = combine(
        repository.liveThermalData,
        _hasUsagePermission,
        _topApps
    ) { thermalData, hasPermission, topApps ->
        CausesUiState(
            thermalData = thermalData,
            hasUsageStatsPermission = hasPermission,
            topActiveApps = topApps,
            isHotspotActive = thermalData.isHotspotActive,
            hotspotDurationMillis = thermalData.hotspotDurationMillis,
            activeGamePackage = thermalData.activeGamePackage,
            isRapidHeating = thermalData.isRapidHeating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CausesUiState()
    )

    fun refreshState(context: Context) {
        val hasPermission = gameDetector.hasUsageStatsPermission(context)
        _hasUsagePermission.value = hasPermission

        if (hasPermission) {
            viewModelScope.launch(Dispatchers.IO) {
                val apps = gameDetector.getTopActiveApps(context)
                _topApps.value = apps
            }
        }
    }
}

