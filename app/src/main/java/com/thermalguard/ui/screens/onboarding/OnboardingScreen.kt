package com.thermalguard.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalguard.R
import com.thermalguard.service.ThermalMonitorService
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalWarm
import com.thermalguard.util.HuaweiGuideHelper

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationGranted = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Shield Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step 1: Notifications
        OnboardingStepCard(
            stepNumber = 1,
            icon = Icons.Default.NotificationsActive,
            title = stringResource(R.string.onboarding_permission_notification_title),
            desc = stringResource(R.string.onboarding_permission_notification_desc),
            isDone = notificationGranted,
            actionButton = {
                if (!notificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(
                        onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Izinkan Notifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Usage Access
        OnboardingStepCard(
            stepNumber = 2,
            icon = Icons.Default.Security,
            title = stringResource(R.string.onboarding_permission_usage_title),
            desc = stringResource(R.string.onboarding_permission_usage_desc),
            actionButton = {
                OutlinedButton(
                    onClick = { HuaweiGuideHelper.openUsageAccessSettings(context) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_grant_permission), fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Huawei Guide
        OnboardingStepCard(
            stepNumber = 3,
            icon = Icons.Default.BatteryAlert,
            title = stringResource(R.string.onboarding_huawei_title),
            desc = stringResource(R.string.onboarding_huawei_desc),
            extraContent = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = stringResource(R.string.huawei_step_1), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(text = stringResource(R.string.huawei_step_2), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(text = stringResource(R.string.huawei_step_3), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            },
            actionButton = {
                Button(
                    onClick = { HuaweiGuideHelper.openAppLaunchSettings(context) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThermalWarm.copy(alpha = 0.2f),
                        contentColor = ThermalWarm
                    )
                ) {
                    Text(stringResource(R.string.btn_open_app_launch), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Finish button
        Button(
            onClick = {
                viewModel.completeOnboarding(startMonitoring = true)
                // Start foreground service
                val serviceIntent = Intent(context, ThermalMonitorService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
                onFinished()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.btn_get_started),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    desc: String,
    isDone: Boolean = false,
    extraContent: (@Composable () -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null
) {
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = ThermalCool,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = desc,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        extraContent?.invoke()

        if (actionButton != null) {
            Spacer(modifier = Modifier.height(10.dp))
            actionButton()
        }
    }
}

