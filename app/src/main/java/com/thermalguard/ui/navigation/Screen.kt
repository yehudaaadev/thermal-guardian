package com.thermalguard.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.thermalguard.R

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Thermostat)
    data object History : Screen("history", R.string.nav_history, Icons.Default.Timeline)
    data object Causes : Screen("causes", R.string.nav_causes, Icons.Default.Whatshot)
    data object Tools : Screen("tools", R.string.nav_tools, Icons.Default.Build)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    data object Onboarding : Screen("onboarding", R.string.onboarding_title, Icons.Default.Info)

    companion object {
        val bottomNavItems = listOf(Home, History, Causes, Tools, Settings)
    }
}

