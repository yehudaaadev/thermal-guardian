package com.thermalguard.ui.screens.home
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalguard.R
import com.thermalguard.ui.components.BatteryInfoCard
import com.thermalguard.ui.components.CoolingSuggestionCard
import com.thermalguard.ui.components.HotspotRecoveryCard
import com.thermalguard.ui.components.ThermalGauge
import com.thermalguard.ui.components.ThermalHealthCard
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Running Indicator Pill
            Row(
                modifier = Modifier
                    .background(
                        color = if (state.isServiceRunning) ThermalCool.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (state.isServiceRunning) ThermalCool else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isServiceRunning) stringResource(R.string.service_running) else stringResource(R.string.service_stopped),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isServiceRunning) ThermalCool else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Thermal Gauge Meter
        ThermalGauge(
            temperatureCelsius = state.thermalData.batteryState.temperature,
            thermalLevel = state.thermalData.thermalLevel,
            systemThermalStatus = state.thermalData.systemThermalStatus,
            isCelsius = state.userSettings.isCelsius
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Thermal Health Score Card
        ThermalHealthCard(
            score = state.thermalData.thermalHealthScore,
            isChargingPaused = state.thermalData.isChargingPaused
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hotspot Recovery Cooldown Card
        HotspotRecoveryCard(
            isHotspotActive = state.thermalData.isHotspotActive,
            tempCelsius = state.thermalData.batteryState.temperature,
            cooldownMinutes = state.thermalData.hotspotCooldownEstimateMinutes
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Quick Row (Rate & Headroom)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rate of change
            val rate = state.thermalData.tempRatePerMinute
            val (rateIcon, rateColor, rateDesc) = when {
                rate > 0.3f -> Triple(Icons.AutoMirrored.Filled.TrendingUp, ThermalHot, String.format("+%.1f°C/mnt", rate))
                rate < -0.3f -> Triple(Icons.AutoMirrored.Filled.TrendingDown, ThermalCool, String.format("%.1f°C/mnt", rate))
                else -> Triple(Icons.AutoMirrored.Filled.TrendingFlat, MaterialTheme.colorScheme.primary, "Stabil")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = rateIcon, contentDescription = "Rate", tint = rateColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.label_temp_rate),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = rateDesc,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Headroom
            val headroom = state.thermalData.thermalHeadroom
            val headroomStr = if (!headroom.isNaN()) String.format("%.1f", headroom) else "N/A"

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.label_temp_headroom),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = headroomStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CPU vs Battery thermal status note (PRD Section 8)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.cpu_vs_battery_note),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start / Stop Monitoring Button
        Button(
            onClick = { viewModel.toggleMonitoringService(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isServiceRunning) ThermalCritical else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (state.isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isServiceRunning) stringResource(R.string.btn_stop_service) else stringResource(R.string.btn_start_service),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Action Row: Floating HUD Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.tools_hud_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.tools_hud_desc),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = state.userSettings.isOverlayEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    } else {
                        viewModel.toggleOverlay(context, enabled)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Info Card
        BatteryInfoCard(batteryState = state.thermalData.batteryState)

        if (state.suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            CoolingSuggestionCard(suggestions = state.suggestions)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

