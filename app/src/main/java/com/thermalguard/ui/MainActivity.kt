package com.thermalguard.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.ui.navigation.AppNavHost
import com.thermalguard.ui.navigation.Screen
import com.thermalguard.ui.theme.ThermalGuardTheme
import com.thermalguard.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: ThermalRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val userSettings by repository.userSettings.collectAsState(initial = UserSettings())
            val currentContext = LocalContext.current
            val localizedContext = remember(userSettings.language) {
                LocaleHelper.applyLocale(currentContext, userSettings.language)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                ThermalGuardTheme(themePreference = userSettings.theme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val startDestination = if (userSettings.isOnboardingCompleted) {
                            Screen.Home.route
                        } else {
                            Screen.Onboarding.route
                        }

                        AppNavHost(startDestination = startDestination)
                    }
                }
            }
        }
    }
}

