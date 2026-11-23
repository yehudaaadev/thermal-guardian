package com.thermalguard.ui.components

import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermalguard.R
import com.thermalguard.domain.model.BatteryState

@Composable
fun BatteryInfoCard(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val healthText = when (batteryState.health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> stringResource(R.string.battery_health_good)
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> stringResource(R.string.battery_health_overheat)
        BatteryManager.BATTERY_HEALTH_DEAD -> stringResource(R.string.battery_health_dead)
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> stringResource(R.string.battery_health_over_voltage)
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> stringResource(R.string.battery_health_failure)
        BatteryManager.BATTERY_HEALTH_COLD -> stringResource(R.string.battery_health_cold)
        else -> stringResource(R.string.battery_health_unknown)
    }

    val statusText = when (batteryState.status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> stringResource(R.string.battery_status_charging)
        BatteryManager.BATTERY_STATUS_DISCHARGING -> stringResource(R.string.battery_status_discharging)
        BatteryManager.BATTERY_STATUS_FULL -> stringResource(R.string.battery_status_full)
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> stringResource(R.string.battery_status_not_charging)
        else -> stringResource(R.string.battery_status_unknown)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Informasi Baterai",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${batteryState.level}% • $statusText",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (batteryState.isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BatteryMetricItem(
                icon = Icons.Default.Bolt,
                label = stringResource(R.string.label_voltage),
                value = String.format("%.2f V", batteryState.voltageVolts),
                modifier = Modifier.weight(1f)
            )

            BatteryMetricItem(
                icon = Icons.Default.Favorite,
                label = stringResource(R.string.label_health),
                value = healthText,
                modifier = Modifier.weight(1f)
            )

            BatteryMetricItem(
                icon = Icons.Default.Memory,
                label = stringResource(R.string.label_technology),
                value = batteryState.technology,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BatteryMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

