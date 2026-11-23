package com.thermalguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.thermalguard.R
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.MainActivity
import com.thermalguard.util.HuaweiGuideHelper
import java.util.concurrent.ConcurrentHashMap

object NotificationHelper {

    const val CHANNEL_STATUS_ID = "thermal_status_channel"
    const val CHANNEL_ALERTS_ID = "thermal_alerts_channel"

    const val NOTIFICATION_ID_STATUS = 1001
    const val NOTIFICATION_ID_WARM = 1002
    const val NOTIFICATION_ID_HOT = 1003
    const val NOTIFICATION_ID_CRITICAL = 1004
    const val NOTIFICATION_ID_GAME_RAPID = 1005
    const val NOTIFICATION_ID_HOTSPOT = 1006
    const val NOTIFICATION_ID_CHARGING = 1007
    const val NOTIFICATION_ID_BATTERY_LIMIT = 1008

    private val lastAlertTimestamps = ConcurrentHashMap<Int, Long>()

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Channel 1: Persistent Thermal Status
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS_ID,
                context.getString(R.string.notification_channel_status),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_status_desc)
                setShowBadge(false)
            }

            // Channel 2: Thermal Alerts & Warnings
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                context.getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_alerts_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(statusChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    fun buildStatusNotification(
        context: Context,
        data: ThermalData,
        settings: UserSettings
    ): Notification {
        val formatTemp = FormatTemperatureUseCase()
        val tempString = formatTemp(data.batteryState.temperature, settings.isCelsius)
        val levelName = when (data.thermalLevel) {
            ThermalLevel.COOL -> context.getString(R.string.thermal_level_cool)
            ThermalLevel.WARM -> context.getString(R.string.thermal_level_warm)
            ThermalLevel.HOT -> context.getString(R.string.thermal_level_hot)
            ThermalLevel.CRITICAL -> context.getString(R.string.thermal_level_critical)
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = StringBuilder("Status: $levelName • Baterai ${data.batteryState.level}%")
        if (data.batteryState.isCharging) {
            subtitle.append(" ⚡")
        }
        if (data.isHotspotActive) {
            subtitle.append(" 📡")
        }

        return NotificationCompat.Builder(context, CHANNEL_STATUS_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ThermalGuard: $tempString ($levelName)")
            .setContentText(subtitle.toString())
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun checkAndPostAlerts(
        context: Context,
        data: ThermalData,
        settings: UserSettings
    ) {
        val now = System.currentTimeMillis()
        val cooldownMillis = settings.cooldownMinutes * 60 * 1000L
        val formatTemp = FormatTemperatureUseCase()
        val tempString = formatTemp(data.batteryState.temperature, settings.isCelsius)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // 1. Level Alerts (WARM, HOT, CRITICAL)
        when (data.thermalLevel) {
            ThermalLevel.CRITICAL -> {
                if (canPostAlert(NOTIFICATION_ID_CRITICAL, now, cooldownMillis)) {
                    val notification = buildAlertNotification(
                        context = context,
                        notificationId = NOTIFICATION_ID_CRITICAL,
                        title = context.getString(R.string.notification_critical_title, tempString),
                        body = context.getString(R.string.notification_critical_body, tempString),
                        priority = NotificationCompat.PRIORITY_MAX,
                        settings = settings,
                        actions = listOf(createDisplayAction(context), createAppsAction(context))
                    )
                    notificationManager.notify(NOTIFICATION_ID_CRITICAL, notification)
                    triggerAlertFeedback(context, settings, isCritical = true)
                    lastAlertTimestamps[NOTIFICATION_ID_CRITICAL] = now
                }
            }
            ThermalLevel.HOT -> {
                if (canPostAlert(NOTIFICATION_ID_HOT, now, cooldownMillis)) {
                    val notification = buildAlertNotification(
                        context = context,
                        notificationId = NOTIFICATION_ID_HOT,
                        title = context.getString(R.string.notification_hot_title, tempString),
                        body = context.getString(R.string.notification_hot_body, tempString),
                        priority = NotificationCompat.PRIORITY_HIGH,
                        settings = settings,
                        actions = listOf(createDisplayAction(context), createHotspotAction(context), createAppsAction(context))
                    )
                    notificationManager.notify(NOTIFICATION_ID_HOT, notification)
                    triggerAlertFeedback(context, settings, isCritical = false)
                    lastAlertTimestamps[NOTIFICATION_ID_HOT] = now
                }
            }
            ThermalLevel.WARM -> {
                if (canPostAlert(NOTIFICATION_ID_WARM, now, cooldownMillis)) {
                    val notification = buildAlertNotification(
                        context = context,
                        notificationId = NOTIFICATION_ID_WARM,
                        title = context.getString(R.string.notification_warm_title, tempString),
                        body = context.getString(R.string.notification_warm_body, tempString),
                        priority = NotificationCompat.PRIORITY_DEFAULT,
                        settings = settings,
                        actions = listOf(createDisplayAction(context))
                    )
                    notificationManager.notify(NOTIFICATION_ID_WARM, notification)
                    triggerAlertFeedback(context, settings, isCritical = false)
                    lastAlertTimestamps[NOTIFICATION_ID_WARM] = now
                }
            }
            ThermalLevel.COOL -> {
                // Clear active level notifications if cooled down
                notificationManager.cancel(NOTIFICATION_ID_WARM)
                notificationManager.cancel(NOTIFICATION_ID_HOT)
                notificationManager.cancel(NOTIFICATION_ID_CRITICAL)
            }
        }

        // 2. Rapid Heating in Game
        if (data.isRapidHeating && data.activeGamePackage != null) {
            if (canPostAlert(NOTIFICATION_ID_GAME_RAPID, now, cooldownMillis)) {
                val notification = buildAlertNotification(
                    context = context,
                    notificationId = NOTIFICATION_ID_GAME_RAPID,
                    title = context.getString(R.string.notification_game_rapid_title, tempString),
                    body = context.getString(R.string.notification_game_rapid_body, data.activeGamePackage),
                    priority = NotificationCompat.PRIORITY_HIGH,
                    settings = settings,
                    actions = listOf(createAppsAction(context))
                )
                notificationManager.notify(NOTIFICATION_ID_GAME_RAPID, notification)
                triggerAlertFeedback(context, settings, isCritical = false)
                lastAlertTimestamps[NOTIFICATION_ID_GAME_RAPID] = now
            }
        }

        // 3. Hotspot Duration / Thermal Alert
        if (data.isHotspotActive && (data.hotspotDurationMillis > 30 * 60 * 1000L || data.thermalLevel >= ThermalLevel.WARM)) {
            if (canPostAlert(NOTIFICATION_ID_HOTSPOT, now, cooldownMillis)) {
                val mins = data.hotspotDurationMillis / 60000L
                val notification = buildAlertNotification(
                    context = context,
                    notificationId = NOTIFICATION_ID_HOTSPOT,
                    title = context.getString(R.string.notification_hotspot_title),
                    body = context.getString(R.string.notification_hotspot_body, "${mins}m", tempString),
                    priority = NotificationCompat.PRIORITY_HIGH,
                    settings = settings,
                    actions = listOf(createHotspotAction(context))
                )
                notificationManager.notify(NOTIFICATION_ID_HOTSPOT, notification)
                triggerAlertFeedback(context, settings, isCritical = false)
                lastAlertTimestamps[NOTIFICATION_ID_HOTSPOT] = now
            }
        }

        // 4. Charging Monitor: High Temp or Charging while Gaming / Hotspot
        val isChargingWhileGaming = data.batteryState.isCharging && data.activeGamePackage != null
        val isChargingWhileHotspot = data.batteryState.isCharging && data.isHotspotActive
        val isChargingWhileHot = data.batteryState.isCharging && data.batteryState.temperature >= settings.warmThreshold

        if (isChargingWhileGaming || isChargingWhileHotspot || isChargingWhileHot) {
            if (canPostAlert(NOTIFICATION_ID_CHARGING, now, cooldownMillis)) {
                val (title, body) = when {
                    isChargingWhileGaming -> Pair(
                        context.getString(R.string.notification_charging_game_title, tempString),
                        context.getString(R.string.notification_charging_game_body)
                    )
                    isChargingWhileHotspot -> Pair(
                        context.getString(R.string.notification_charging_hotspot_title, tempString),
                        context.getString(R.string.notification_charging_hotspot_body)
                    )
                    else -> Pair(
                        context.getString(R.string.notification_charging_hot_title, tempString),
                        context.getString(R.string.notification_charging_hot_body)
                    )
                }

                val notification = buildAlertNotification(
                    context = context,
                    notificationId = NOTIFICATION_ID_CHARGING,
                    title = title,
                    body = body,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    settings = settings,
                    actions = if (isChargingWhileHotspot) listOf(createHotspotAction(context)) else listOf(createAppsAction(context))
                )
                notificationManager.notify(NOTIFICATION_ID_CHARGING, notification)
                triggerAlertFeedback(context, settings, isCritical = false)
                lastAlertTimestamps[NOTIFICATION_ID_CHARGING] = now
            }
        }

        // 5. Battery Charge Limit Alarm
        if (settings.chargeFullAlarmEnabled && data.batteryState.isCharging && data.batteryState.level >= settings.targetChargeLevel) {
            if (canPostAlert(NOTIFICATION_ID_BATTERY_LIMIT, now, cooldownMillis)) {
                val notification = buildAlertNotification(
                    context = context,
                    notificationId = NOTIFICATION_ID_BATTERY_LIMIT,
                    title = context.getString(R.string.notification_battery_limit_title, settings.targetChargeLevel),
                    body = context.getString(R.string.notification_battery_limit_body, settings.targetChargeLevel),
                    priority = NotificationCompat.PRIORITY_HIGH,
                    settings = settings
                )
                notificationManager.notify(NOTIFICATION_ID_BATTERY_LIMIT, notification)
                triggerAlertFeedback(context, settings, isCritical = false)
                lastAlertTimestamps[NOTIFICATION_ID_BATTERY_LIMIT] = now
            }
        }
    }

    fun isQuietHoursActive(settings: UserSettings): Boolean {
        if (!settings.quietHoursEnabled) return false
        val cal = java.util.Calendar.getInstance()
        val currentMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        val start = settings.quietHoursStartMinutes
        val end = settings.quietHoursEndMinutes
        return if (start <= end) {
            currentMinutes in start until end
        } else {
            currentMinutes >= start || currentMinutes < end
        }
    }

    private fun canPostAlert(id: Int, now: Long, cooldownMillis: Long): Boolean {
        val last = lastAlertTimestamps[id] ?: 0L
        return (now - last) >= cooldownMillis
    }

    private fun buildAlertNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        priority: Int,
        settings: UserSettings,
        actions: List<NotificationCompat.Action> = emptyList()
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (settings.alertSound && !isQuietHoursActive(settings)) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        actions.forEach { builder.addAction(it) }

        return builder.build()
    }

    private fun createDisplayAction(context: Context): NotificationCompat.Action {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
        val pi = PendingIntent.getActivity(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_action_display),
            pi
        ).build()
    }

    private fun createHotspotAction(context: Context): NotificationCompat.Action {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        val pi = PendingIntent.getActivity(
            context,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_action_hotspot),
            pi
        ).build()
    }

    private fun createAppsAction(context: Context): NotificationCompat.Action {
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        val pi = PendingIntent.getActivity(
            context,
            2003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_action_apps),
            pi
        ).build()
    }

    private fun triggerAlertFeedback(context: Context, settings: UserSettings, isCritical: Boolean) {
        if (!isCritical && isQuietHoursActive(settings)) {
            return
        }

        if (settings.alertVibration) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = if (isCritical) {
                            longArrayOf(0, 400, 200, 400, 200, 600)
                        } else {
                            longArrayOf(0, 300, 200, 300)
                        }
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(if (isCritical) 1000L else 500L)
                    }
                }
            } catch (e: Exception) {
                // Ignore vibration errors
            }
        }

        if (settings.alertSound) {
            try {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, soundUri)
                ringtone?.play()
            } catch (e: Exception) {
                // Ignore sound errors
            }
        }
    }
}

