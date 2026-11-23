package com.thermalguard.util

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.thermalguard.domain.model.AppUsageInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDetector @Inject constructor() {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Identifies the current or most recent foreground app.
     */
    fun getForegroundApp(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 5) // Last 5 minutes

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var lastForegroundApp: String? = null
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                lastForegroundApp = event.packageName
            }
        }

        return lastForegroundApp
    }

    /**
     * Checks if a given package or the current foreground app is a game.
     */
    fun isAppGame(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Returns top active apps in the past duration with their foreground durations.
     */
    fun getTopActiveApps(context: Context, durationMillis: Long = 60 * 60 * 1000L): List<AppUsageInfo> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()
        val pm = context.packageManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - durationMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        return stats
            .filter { it.totalTimeInForeground > 30_000L && it.packageName != context.packageName }
            .map { usageStat ->
                val appName = try {
                    val appInfo = pm.getApplicationInfo(usageStat.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    usageStat.packageName
                }

                val isGame = isAppGame(context, usageStat.packageName)

                AppUsageInfo(
                    packageName = usageStat.packageName,
                    appName = appName,
                    foregroundDurationMillis = usageStat.totalTimeInForeground,
                    isGame = isGame,
                    lastTimeUsed = usageStat.lastTimeUsed
                )
            }
            .sortedByDescending { it.foregroundDurationMillis }
            .take(10)
    }
}

