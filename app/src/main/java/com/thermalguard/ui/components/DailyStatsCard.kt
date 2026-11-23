package com.thermalguard.ui.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.WarningAmber
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
import com.thermalguard.domain.model.DailyThermalSummary
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.theme.ThermalHot

@Composable
fun DailyStatsCard(
    dailySummary: DailyThermalSummary,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    val formatTemp = FormatTemperatureUseCase()
    val maxTempStr = formatTemp(dailySummary.maxTemp, isCelsius)

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
                text = stringResource(R.string.daily_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = dailySummary.dateString,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DailyStatItem(
                icon = Icons.Default.WarningAmber,
                label = "Kejadian Panas",
                value = "${dailySummary.overheatCount}x",
                tint = if (dailySummary.overheatCount > 0) ThermalHot else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            DailyStatItem(
                icon = Icons.Default.QueryBuilder,
                label = "Durasi Panas",
                value = "${dailySummary.minutesAboveSafeTemp} mnt",
                modifier = Modifier.weight(1f)
            )

            DailyStatItem(
                icon = Icons.Default.AccessTime,
                label = "Puncak Suhu",
                value = if (dailySummary.maxTemp > 0f) "$maxTempStr (${dailySummary.peakHour})" else "--",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DailyStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
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

