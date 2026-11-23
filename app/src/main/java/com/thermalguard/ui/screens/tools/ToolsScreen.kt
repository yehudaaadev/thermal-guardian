package com.thermalguard.ui.screens.tools

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalguard.R
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalWarm
import com.thermalguard.util.StressTestResult

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = stringResource(R.string.tools_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.tools_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Floating HUD Overlay Card
        FloatingHudCard(
            isEnabled = state.isOverlayEnabled,
            onToggle = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    showOverlayPermissionDialog = true
                } else {
                    viewModel.toggleOverlay(enabled, context)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Thermal Stress Test Card
        StressTestCard(
            state = state,
            isCelsius = settings.isCelsius,
            onDurationSelect = { viewModel.setStressDurationOption(it) },
            onStart = { viewModel.startStressTest(context) },
            onCancel = { viewModel.cancelStressTest() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Cooling Stopwatch Card
        CoolingStopwatchCard(
            state = state,
            isCelsius = settings.isCelsius,
            onStart = { viewModel.startCoolingTimer(context) },
            onStop = { viewModel.stopCoolingTimer() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Diagnostic Report Share Card
        DiagnosticReportCard(
            onShare = {
                val report = viewModel.generateDiagnosticReport()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.diag_report_subject))
                    putExtra(Intent.EXTRA_TEXT, report)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.btn_share_diag_report)))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text(text = stringResource(R.string.tools_hud_permission_dialog_title)) },
            text = { Text(text = stringResource(R.string.tools_hud_permission_dialog_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.tools_hud_permission_grant))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text(text = stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun FloatingHudCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.tools_hud_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.tools_hud_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun StressTestCard(
    state: ToolsUiState,
    isCelsius: Boolean,
    onDurationSelect: (Int) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    val formatTemp = FormatTemperatureUseCase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(ThermalHot.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ThermalHot
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.stress_test_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.stress_test_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safety Warning Alert Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThermalCritical.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ThermalCritical,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stress_test_safety_note),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duration Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair(15, stringResource(R.string.stress_test_duration_15s)),
                    Pair(30, stringResource(R.string.stress_test_duration_30s)),
                    Pair(60, stringResource(R.string.stress_test_duration_60s))
                ).forEach { (seconds, label) ->
                    val isSelected = state.stressDurationOption == seconds
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable(enabled = !state.isStressTesting) { onDurationSelect(seconds) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar & Live Temp when testing
            AnimatedVisibility(visible = state.isStressTesting) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    val progress = if (state.stressDurationOption > 0) {
                        state.stressElapsedSeconds.toFloat() / state.stressDurationOption.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ThermalHot,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.stress_test_running,
                                state.stressElapsedSeconds,
                                state.stressDurationOption
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatTemp(state.stressCurrentTemp, isCelsius),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThermalHot
                        )
                    }
                }
            }

            // Results Card
            state.stressResult?.let { result ->
                Spacer(modifier = Modifier.height(10.dp))
                StressResultBox(result = result, isCelsius = isCelsius)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = {
                    if (state.isStressTesting) onCancel() else onStart()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isStressTesting) ThermalCritical else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (state.isStressTesting) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isStressTesting) {
                        stringResource(R.string.stress_test_btn_cancel)
                    } else {
                        stringResource(R.string.stress_test_btn_start)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StressResultBox(
    result: StressTestResult,
    isCelsius: Boolean
) {
    val formatTemp = FormatTemperatureUseCase()
    val (ratingText, ratingColor) = when {
        result.dissipationScore >= 85 -> Pair(stringResource(R.string.stress_test_rating_excellent), ThermalCool)
        result.dissipationScore >= 70 -> Pair(stringResource(R.string.stress_test_rating_good), MaterialTheme.colorScheme.primary)
        result.dissipationScore >= 50 -> Pair(stringResource(R.string.stress_test_rating_moderate), ThermalWarm)
        else -> Pair(stringResource(R.string.stress_test_rating_poor), ThermalCritical)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, ratingColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stress_test_result_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .background(ratingColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${result.dissipationScore} / 100",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ratingColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = ratingText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ratingColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.stress_test_start_temp, formatTemp(result.initialTemp, isCelsius)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.stress_test_peak_temp, formatTemp(result.peakTemp, isCelsius)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.stress_test_delta, result.tempDelta),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (result.tempDelta > 2.0f) ThermalHot else ThermalCool
            )
        }

        if (result.wasAbortedForSafety) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.stress_test_aborted_warning),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ThermalCritical
            )
        }
    }
}

@Composable
private fun CoolingStopwatchCard(
    state: ToolsUiState,
    isCelsius: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val formatTemp = FormatTemperatureUseCase()
    val tempDrop = (state.coolingStartTemp - state.coolingCurrentTemp).coerceAtLeast(0f)
    val rate = if (state.coolingElapsedSeconds > 0) {
        (tempDrop / (state.coolingElapsedSeconds / 60f))
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(ThermalCool.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AvTimer,
                        contentDescription = null,
                        tint = ThermalCool
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.cooling_timer_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.cooling_timer_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        lineHeight = 15.sp
                    )
                }
            }

            if (state.isCoolingTimerRunning) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.cooling_timer_running, state.coolingElapsedSeconds),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatTemp(state.coolingCurrentTemp, isCelsius),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThermalCool
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.cooling_timer_drop, tempDrop),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.cooling_timer_rate, rate),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThermalCool
                        )
                    }

                    if (state.isCoolingTargetReached) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ThermalCool.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ " + stringResource(R.string.cooling_timer_target_reached),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThermalCool
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { if (state.isCoolingTimerRunning) onStop() else onStart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isCoolingTimerRunning) ThermalCritical else ThermalCool
                )
            ) {
                Icon(
                    imageVector = if (state.isCoolingTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isCoolingTimerRunning) {
                        stringResource(R.string.cooling_timer_stop)
                    } else {
                        stringResource(R.string.cooling_timer_start)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiagnosticReportCard(
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.tools_diag_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.tools_diag_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_share_diag_report),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

