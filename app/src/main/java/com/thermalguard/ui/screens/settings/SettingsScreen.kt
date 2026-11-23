package com.thermalguard.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalguard.R
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalWarm
import com.thermalguard.util.HuaweiGuideHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Thresholds Card
        SettingsSection(
            icon = Icons.Default.Thermostat,
            title = stringResource(R.string.settings_category_thresholds)
        ) {
            ThresholdSliderItem(
                label = stringResource(R.string.settings_threshold_warm),
                value = settings.warmThreshold,
                min = 35f,
                max = 42f,
                color = ThermalWarm,
                onValueChange = { viewModel.updateThresholds(it, settings.hotThreshold, settings.criticalThreshold) }
            )

            ThresholdSliderItem(
                label = stringResource(R.string.settings_threshold_hot),
                value = settings.hotThreshold,
                min = 38f,
                max = 45f,
                color = ThermalHot,
                onValueChange = { viewModel.updateThresholds(settings.warmThreshold, it, settings.criticalThreshold) }
            )

            ThresholdSliderItem(
                label = stringResource(R.string.settings_threshold_critical),
                value = settings.criticalThreshold,
                min = 40f,
                max = 48f,
                color = ThermalCritical,
                onValueChange = { viewModel.updateThresholds(settings.warmThreshold, settings.hotThreshold, it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Unit Preference Card
        SettingsSection(
            icon = Icons.Default.Thermostat,
            title = stringResource(R.string.settings_category_general)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_unit),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.updateIsCelsius(true) }
                    ) {
                        RadioButton(
                            selected = settings.isCelsius,
                            onClick = { viewModel.updateIsCelsius(true) }
                        )
                        Text(text = "°C", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.updateIsCelsius(false) }
                    ) {
                        RadioButton(
                            selected = !settings.isCelsius,
                            onClick = { viewModel.updateIsCelsius(false) }
                        )
                        Text(text = "°F", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interval setting
            Text(
                text = stringResource(R.string.settings_interval),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionItem(
                    label = stringResource(R.string.settings_interval_adaptive),
                    selected = settings.intervalMode == UserSettings.INTERVAL_ADAPTIVE,
                    onClick = { viewModel.updateIntervalMode(UserSettings.INTERVAL_ADAPTIVE) }
                )
                ThemeOptionItem(
                    label = "15s",
                    selected = settings.intervalMode == UserSettings.INTERVAL_15S,
                    onClick = { viewModel.updateIntervalMode(UserSettings.INTERVAL_15S) }
                )
                ThemeOptionItem(
                    label = "30s",
                    selected = settings.intervalMode == UserSettings.INTERVAL_30S,
                    onClick = { viewModel.updateIntervalMode(UserSettings.INTERVAL_30S) }
                )
                ThemeOptionItem(
                    label = "60s",
                    selected = settings.intervalMode == UserSettings.INTERVAL_60S,
                    onClick = { viewModel.updateIntervalMode(UserSettings.INTERVAL_60S) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Alerts & Feedback Card
        SettingsSection(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.settings_category_alerts)
        ) {
            SwitchRowItem(
                title = stringResource(R.string.settings_sound),
                subtitle = stringResource(R.string.settings_sound_desc),
                checked = settings.alertSound,
                onCheckedChange = { viewModel.updateAlertSettings(it, settings.alertVibration, settings.cooldownMinutes) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SwitchRowItem(
                title = stringResource(R.string.settings_vibration),
                subtitle = stringResource(R.string.settings_vibration_desc),
                checked = settings.alertVibration,
                onCheckedChange = { viewModel.updateAlertSettings(settings.alertSound, it, settings.cooldownMinutes) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Thermal Tools & Protections Card
        SettingsSection(
            icon = Icons.Default.Tune,
            title = stringResource(R.string.settings_category_advanced)
        ) {
            // Calibration Offset Slider
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_calibration_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_calibration_offset, settings.tempOffset),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.settings_calibration_desc),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Slider(
                    value = settings.tempOffset,
                    onValueChange = {
                        val rounded = (Math.round(it * 2f) / 2f)
                        viewModel.updateTempOffset(rounded)
                    },
                    valueRange = -2.0f..2.0f,
                    steps = 7
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quiet Hours Switch
            SwitchRowItem(
                title = stringResource(R.string.settings_quiet_hours_title),
                subtitle = stringResource(R.string.settings_quiet_hours_desc),
                checked = settings.quietHoursEnabled,
                onCheckedChange = { viewModel.updateQuietHours(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Charge Limit Alarm Switch
            SwitchRowItem(
                title = stringResource(R.string.settings_charge_alarm_title),
                subtitle = stringResource(R.string.settings_charge_alarm_desc),
                checked = settings.chargeFullAlarmEnabled,
                onCheckedChange = { viewModel.updateChargeFullAlarm(it, settings.targetChargeLevel) }
            )

            if (settings.chargeFullAlarmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThemeOptionItem(
                        label = stringResource(R.string.settings_charge_alarm_target, 80),
                        selected = settings.targetChargeLevel == 80,
                        onClick = { viewModel.updateChargeFullAlarm(true, 80) }
                    )
                    ThemeOptionItem(
                        label = stringResource(R.string.settings_charge_alarm_target, 100),
                        selected = settings.targetChargeLevel == 100,
                        onClick = { viewModel.updateChargeFullAlarm(true, 100) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Huawei & Battery Optimization Card
        SettingsSection(
            icon = Icons.Default.BatterySaver,
            title = stringResource(R.string.onboarding_huawei_title)
        ) {
            Text(
                text = stringResource(R.string.onboarding_huawei_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { HuaweiGuideHelper.openAppLaunchSettings(context) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = stringResource(R.string.btn_open_app_launch), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Data & Retention
        SettingsSection(
            icon = Icons.Default.Storage,
            title = stringResource(R.string.settings_category_data)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_retention),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.settings_retention_days, settings.retentionDays),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThermalCritical.copy(alpha = 0.15f),
                    contentColor = ThermalCritical
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.settings_clear_history), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Theme & Language
        SettingsSection(
            icon = Icons.Default.ColorLens,
            title = stringResource(R.string.settings_category_appearance)
        ) {
            // Theme selection
            Text(text = stringResource(R.string.settings_theme), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionItem(
                    label = stringResource(R.string.settings_theme_system),
                    selected = settings.theme == UserSettings.THEME_SYSTEM,
                    onClick = { viewModel.updateTheme(UserSettings.THEME_SYSTEM) }
                )
                ThemeOptionItem(
                    label = stringResource(R.string.settings_theme_light),
                    selected = settings.theme == UserSettings.THEME_LIGHT,
                    onClick = { viewModel.updateTheme(UserSettings.THEME_LIGHT) }
                )
                ThemeOptionItem(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = settings.theme == UserSettings.THEME_DARK,
                    onClick = { viewModel.updateTheme(UserSettings.THEME_DARK) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language selection
            Text(text = stringResource(R.string.settings_language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionItem(
                    label = stringResource(R.string.settings_language_id),
                    selected = settings.language == UserSettings.LANG_ID,
                    onClick = { viewModel.updateLanguage(UserSettings.LANG_ID) }
                )
                ThemeOptionItem(
                    label = stringResource(R.string.settings_language_en),
                    selected = settings.language == UserSettings.LANG_EN,
                    onClick = { viewModel.updateLanguage(UserSettings.LANG_EN) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. About Card
        SettingsSection(
            icon = Icons.Default.Info,
            title = stringResource(R.string.settings_category_about)
        ) {
            Text(
                text = stringResource(R.string.settings_about_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_version, "1.0.0"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_history)) },
            text = { Text(stringResource(R.string.settings_clear_history_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThermalCritical)
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ThresholdSliderItem(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    color: androidx.compose.ui.graphics.Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = String.format("%.0f°C", value), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            steps = ((max - min) - 1).toInt()
        )
    }
}

@Composable
private fun SwitchRowItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

