package com.thermalguard.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thermalguard.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "thermal_guard_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_WARM_THRESHOLD = floatPreferencesKey("warm_threshold")
        val KEY_HOT_THRESHOLD = floatPreferencesKey("hot_threshold")
        val KEY_CRITICAL_THRESHOLD = floatPreferencesKey("critical_threshold")
        val KEY_IS_CELSIUS = booleanPreferencesKey("is_celsius")
        val KEY_ALERT_SOUND = booleanPreferencesKey("alert_sound")
        val KEY_ALERT_VIBRATION = booleanPreferencesKey("alert_vibration")
        val KEY_COOLDOWN_MINUTES = intPreferencesKey("cooldown_minutes")
        val KEY_RETENTION_DAYS = intPreferencesKey("retention_days")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_INTERVAL_MODE = stringPreferencesKey("interval_mode")
        val KEY_TEMP_OFFSET = floatPreferencesKey("temp_offset")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val KEY_QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val KEY_QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val KEY_CHARGE_FULL_ALARM = booleanPreferencesKey("charge_full_alarm")
        val KEY_TARGET_CHARGE_LEVEL = intPreferencesKey("target_charge_level")
        val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserSettings(
                warmThreshold = prefs[KEY_WARM_THRESHOLD] ?: 38.0f,
                hotThreshold = prefs[KEY_HOT_THRESHOLD] ?: 40.0f,
                criticalThreshold = prefs[KEY_CRITICAL_THRESHOLD] ?: 43.0f,
                isCelsius = prefs[KEY_IS_CELSIUS] ?: true,
                alertSound = prefs[KEY_ALERT_SOUND] ?: true,
                alertVibration = prefs[KEY_ALERT_VIBRATION] ?: true,
                cooldownMinutes = prefs[KEY_COOLDOWN_MINUTES] ?: 5,
                retentionDays = prefs[KEY_RETENTION_DAYS] ?: 30,
                theme = prefs[KEY_THEME] ?: UserSettings.THEME_SYSTEM,
                language = prefs[KEY_LANGUAGE] ?: UserSettings.LANG_SYSTEM,
                intervalMode = prefs[KEY_INTERVAL_MODE] ?: UserSettings.INTERVAL_ADAPTIVE,
                tempOffset = prefs[KEY_TEMP_OFFSET] ?: 0.0f,
                quietHoursEnabled = prefs[KEY_QUIET_HOURS_ENABLED] ?: false,
                quietHoursStartMinutes = prefs[KEY_QUIET_HOURS_START] ?: (22 * 60),
                quietHoursEndMinutes = prefs[KEY_QUIET_HOURS_END] ?: (6 * 60),
                chargeFullAlarmEnabled = prefs[KEY_CHARGE_FULL_ALARM] ?: true,
                targetChargeLevel = prefs[KEY_TARGET_CHARGE_LEVEL] ?: 80,
                isOverlayEnabled = prefs[KEY_OVERLAY_ENABLED] ?: false,
                isOnboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false,
                isMonitoringEnabled = prefs[KEY_MONITORING_ENABLED] ?: false
            )
        }

    suspend fun updateThresholds(warm: Float, hot: Float, critical: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WARM_THRESHOLD] = warm
            prefs[KEY_HOT_THRESHOLD] = hot
            prefs[KEY_CRITICAL_THRESHOLD] = critical
        }
    }

    suspend fun updateIsCelsius(isCelsius: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CELSIUS] = isCelsius
        }
    }

    suspend fun updateAlertSettings(sound: Boolean, vibration: Boolean, cooldownMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ALERT_SOUND] = sound
            prefs[KEY_ALERT_VIBRATION] = vibration
            prefs[KEY_COOLDOWN_MINUTES] = cooldownMinutes
        }
    }

    suspend fun updateRetentionDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RETENTION_DAYS] = days
        }
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    suspend fun updateIntervalMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTERVAL_MODE] = mode
        }
    }

    suspend fun updateTempOffset(offset: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEMP_OFFSET] = offset
        }
    }

    suspend fun updateQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUIET_HOURS_ENABLED] = enabled
            prefs[KEY_QUIET_HOURS_START] = startMinutes
            prefs[KEY_QUIET_HOURS_END] = endMinutes
        }
    }

    suspend fun updateChargeFullAlarm(enabled: Boolean, targetLevel: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHARGE_FULL_ALARM] = enabled
            prefs[KEY_TARGET_CHARGE_LEVEL] = targetLevel
        }
    }

    suspend fun updateOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OVERLAY_ENABLED] = enabled
        }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MONITORING_ENABLED] = enabled
        }
    }
}

