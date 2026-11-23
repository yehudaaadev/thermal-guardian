package com.thermalguard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.thermalguard.R
import com.thermalguard.domain.model.ThermalData
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.MainActivity
import com.thermalguard.util.ThermalSensorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThermalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val sensorManager = ThermalSensorManager()
        val batteryState = sensorManager.readBatteryState(context)
        val thermalStatus = sensorManager.getSystemThermalStatus(context)
        val level = ThermalLevel.fromTemperature(batteryState.temperature)

        val data = ThermalData(
            batteryState = batteryState,
            thermalLevel = level,
            systemThermalStatus = thermalStatus,
            lastUpdated = System.currentTimeMillis()
        )

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, data)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context, data: ThermalData) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, ThermalWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, data)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            data: ThermalData
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_thermal)

            val formatTemp = FormatTemperatureUseCase()
            val tempStr = formatTemp(data.batteryState.temperature, isCelsius = true)

            views.setTextViewText(R.id.widget_temp, tempStr)

            val (statusText, statusColor) = when (data.thermalLevel) {
                ThermalLevel.COOL -> Pair(context.getString(R.string.thermal_level_cool), Color.parseColor("#4CAF50"))
                ThermalLevel.WARM -> Pair(context.getString(R.string.thermal_level_warm), Color.parseColor("#FFA726"))
                ThermalLevel.HOT -> Pair(context.getString(R.string.thermal_level_hot), Color.parseColor("#FF5722"))
                ThermalLevel.CRITICAL -> Pair(context.getString(R.string.thermal_level_critical), Color.parseColor("#D32F2F"))
            }

            views.setTextViewText(R.id.widget_status, statusText)
            views.setTextColor(R.id.widget_status, statusColor)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val updatedStr = "Updated ${timeFormat.format(Date(data.lastUpdated))}"
            views.setTextViewText(R.id.widget_updated, updatedStr)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

