package com.thermalguard.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalWarm

@Composable
fun ThermalHealthCard(
    score: Int,
    isChargingPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val (scoreColor, statusText, subText) = when {
        score >= 90 -> Triple(ThermalCool, "Kondisi Sangat Optimal", "Risiko degradasi panas: Sangat Rendah")
        score >= 75 -> Triple(ThermalWarm, "Beban Termal Wajar", "Risiko degradasi panas: Rendah - Sedang")
        score >= 60 -> Triple(ThermalHot, "Waspada Suhu Tinggi", "Risiko degradasi panas: Signifikan")
        else -> Triple(ThermalCritical, "Stres Termal Ekstrem", "Risiko degradasi sel baterai: Tinggi")
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Health",
                        tint = scoreColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Skor Kesehatan Termal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scoreColor
                )
                Text(
                    text = subText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Circular Progress Meter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = scoreColor,
                    strokeWidth = 5.dp,
                    trackColor = scoreColor.copy(alpha = 0.15f)
                )
                Text(
                    text = "$score",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Fast Charging Standby Indicator
        AnimatedVisibility(visible = isChargingPaused) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mode Cas Aktif: Pemantauan intensif dijeda agar pengisian baterai maksimal & cepat.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HotspotRecoveryCard(
    isHotspotActive: Boolean,
    tempCelsius: Float,
    cooldownMinutes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isHot = tempCelsius >= 39.0f

    // Show card if hotspot is active OR if temperature is hot (> 39C) where hotspot might have been throttled
    if (!isHotspotActive && !isHot) return

    val (bgBorder, icon, title, desc) = if (isHot) {
        val minStr = if (cooldownMinutes > 0) "~$cooldownMinutes menit" else "segera"
        Quadruple(
            ThermalHot,
            Icons.Default.HourglassTop,
            "Hotspot Cooldown Estimator",
            "Suhu perangkat saat ini ${String.format("%.1f", tempCelsius)}°C. Diperkirakan aman dinyalakan kembali dalam $minStr (target < 39°C)."
        )
    } else {
        Quadruple(
            ThermalCool,
            Icons.Default.CheckCircle,
            "Hotspot Siap Digunakan",
            "Suhu perangkat ${String.format("%.1f", tempCelsius)}°C sudah normal & aman. Anda dapat mengaktifkan kembali hotspot sekarang."
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .border(1.dp, bgBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = "Hotspot Status",
                tint = bgBorder,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = desc,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = bgBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Buka Pengaturan Hotspot",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
