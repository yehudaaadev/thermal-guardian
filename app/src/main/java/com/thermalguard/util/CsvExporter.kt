package com.thermalguard.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.domain.model.SystemThermalStatus
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {

    fun exportToCsv(context: Context, records: List<TemperatureEntity>): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "thermalguard_history_${timestampFormat.format(Date())}.csv"
            val file = File(exportDir, fileName)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

            FileWriter(file).use { writer ->
                // Header
                writer.append("Timestamp,Date,Time,Temperature_C,Thermal_Status,Battery_Level,Is_Charging,Is_Hotspot_Active,Is_Gaming\n")

                for (record in records) {
                    val date = Date(record.timestamp)
                    val statusName = SystemThermalStatus.fromCode(record.thermalStatus).name

                    writer.append("${record.timestamp},")
                    writer.append("${dateFormat.format(date)},")
                    writer.append("${timeFormat.format(date)},")
                    writer.append(String.format(Locale.US, "%.1f,", record.temperatureCelsius))
                    writer.append("$statusName,")
                    writer.append("${record.batteryLevel}%,")
                    writer.append("${record.isCharging},")
                    writer.append("${record.isHotspotOn},")
                    writer.append("${record.isGaming}\n")
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}

