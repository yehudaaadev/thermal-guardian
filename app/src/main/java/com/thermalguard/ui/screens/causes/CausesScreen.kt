package com.thermalguard.ui.screens.causes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalWarm
import com.thermalguard.util.HuaweiGuideHelper

@Composable
fun CausesScreen(
    viewModel: CausesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.refreshState(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.causes_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Hotspot Guard Card
        HotspotGuardCard(
            isActive = state.isHotspotActive,
            durationMillis = state.hotspotDurationMillis,
            onOpenHotspot = { HuaweiGuideHelper.openHotspotSettings(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Game Mode Card
        GameModeCard(
            activeGame = state.activeGamePackage,
            isRapidHeating = state.isRapidHeating,
            ratePerMinute = state.thermalData.tempRatePerMinute
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Top Active Apps / Heat Source Detector
        if (!state.hasUsageStatsPermission) {
            UsagePermissionCard(
                onGrantPermission = {
                    HuaweiGuideHelper.openUsageAccessSettings(context)
                }
            )
        } else {
            TopActiveAppsCard(apps = state.topActiveApps)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HotspotGuardCard(
    isActive: Boolean,
    durationMillis: Long,
    onOpenHotspot: () -> Unit
) {
    val mins = durationMillis / 60000L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    tint = if (isActive) ThermalHot else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hotspot_guard_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (isActive) ThermalHot.copy(alpha = 0.2f) else ThermalCool.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isActive) stringResource(R.string.hotspot_active) else stringResource(R.string.hotspot_inactive),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) ThermalHot else ThermalCool
                )
            }
        }

        if (isActive) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.hotspot_duration, "${mins} menit"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (mins >= 30) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hotspot_warning, 30),
                    fontSize = 12.sp,
                    color = ThermalHot
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenHotspot,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.action_hotspot_settings),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameModeCard(
    activeGame: String?,
    isRapidHeating: Boolean,
    ratePerMinute: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Gamepad,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.game_mode_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeGame != null) {
            Text(
                text = stringResource(R.string.game_detected, activeGame),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = "Tidak ada game terdeteksi di latar depan.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isRapidHeating) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ThermalHot,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.rapid_heating_alert, ratePerMinute, "1 mnt"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThermalHot
                )
            }
        }
    }
}

@Composable
private fun UsagePermissionCard(
    onGrantPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = ThermalWarm
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.causes_permission_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.causes_permission_desc),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onGrantPermission,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.action_grant_permission),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TopActiveAppsCard(
    apps: List<com.thermalguard.domain.model.AppUsageInfo>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.top_active_apps),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (apps.isEmpty()) {
            Text(
                text = stringResource(R.string.no_apps_found),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            apps.forEach { app ->
                val estimatedLoad = (app.foregroundMinutes * (if (app.isGame) 2.5f else 1.0f)).toInt()
                val (impactText, impactColor) = when {
                    estimatedLoad >= 40 -> Pair(stringResource(R.string.causes_heat_impact_high), ThermalCritical)
                    estimatedLoad >= 15 -> Pair(stringResource(R.string.causes_heat_impact_med), ThermalWarm)
                    else -> Pair(stringResource(R.string.causes_heat_impact_low), ThermalCool)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.appName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (app.isGame) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(ThermalWarm.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GAME",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThermalWarm
                                    )
                                }
                            }
                        }
                        Text(
                            text = app.packageName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.foreground_time, app.foregroundMinutes),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(impactColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = impactText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = impactColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informational tip banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .padding(10.dp),
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
                    text = stringResource(R.string.causes_heat_tip),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

