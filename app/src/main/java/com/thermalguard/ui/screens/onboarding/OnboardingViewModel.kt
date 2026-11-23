package com.thermalguard.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ThermalRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun completeOnboarding(startMonitoring: Boolean = true) {
        viewModelScope.launch {
            repository.updateOnboardingCompleted(true)
            if (startMonitoring) {
                repository.updateMonitoringEnabled(true)
            }
        }
    }
}

