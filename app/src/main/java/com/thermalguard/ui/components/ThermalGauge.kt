package com.thermalguard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermalguard.R
import com.thermalguard.domain.model.SystemThermalStatus
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalWarm

@Composable
fun ThermalGauge(
    temperatureCelsius: Float,
    thermalLevel: ThermalLevel,
    systemThermalStatus: SystemThermalStatus,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    val minTemp = 20f
    val maxTemp = 55f
    val clampedTemp = temperatureCelsius.coerceIn(minTemp, maxTemp)
    val progressFraction = (clampedTemp - minTemp) / (maxTemp - minTemp)

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "gauge_progress"
    )

    val levelColor = when (thermalLevel) {
        ThermalLevel.COOL -> ThermalCool
        ThermalLevel.WARM -> ThermalWarm
        ThermalLevel.HOT -> ThermalHot
        ThermalLevel.CRITICAL -> ThermalCritical
    }

    val animatedColor by animateColorAsState(
        targetValue = levelColor,
        animationSpec = tween(durationMillis = 600),
        label = "gauge_color"
    )

    val formatTemp = FormatTemperatureUseCase()
    val formattedTemp = formatTemp(temperatureCelsius, isCelsius, includeUnit = false)
    val unitStr = if (isCelsius) "°C" else "°F"

    val levelText = when (thermalLevel) {
        ThermalLevel.COOL -> stringResource(R.string.thermal_level_cool)
        ThermalLevel.WARM -> stringResource(R.string.thermal_level_warm)
        ThermalLevel.HOT -> stringResource(R.string.thermal_level_hot)
        ThermalLevel.CRITICAL -> stringResource(R.string.thermal_level_critical)
    }

    val statusText = when (systemThermalStatus) {
        SystemThermalStatus.NONE -> stringResource(R.string.thermal_status_none)
        SystemThermalStatus.LIGHT -> stringResource(R.string.thermal_status_light)
        SystemThermalStatus.MODERATE -> stringResource(R.string.thermal_status_moderate)
        SystemThermalStatus.SEVERE -> stringResource(R.string.thermal_status_severe)
        SystemThermalStatus.CRITICAL -> stringResource(R.string.thermal_status_critical)
        SystemThermalStatus.EMERGENCY -> stringResource(R.string.thermal_status_emergency)
        SystemThermalStatus.SHUTDOWN -> stringResource(R.string.thermal_status_shutdown)
        SystemThermalStatus.UNKNOWN -> stringResource(R.string.thermal_status_unknown)
    }

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            val startAngle = 140f
            val totalSweep = 260f

            // Background track
            drawArc(
                color = Color(0xFF2A3442),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc with Gradient
            val gradientBrush = Brush.sweepGradient(
                0.0f to ThermalCool,
                0.5f to ThermalWarm,
                0.8f to ThermalHot,
                1.0f to ThermalCritical
            )

            drawArc(
                brush = gradientBrush,
                startAngle = startAngle,
                sweepAngle = totalSweep * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTemp,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = unitStr,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                )
            }

            // Thermal Level Pill Badge
            Box(
                modifier = Modifier
                    .background(
                        color = animatedColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = levelText.uppercase(),
                    color = animatedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // System Thermal Status info
            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

