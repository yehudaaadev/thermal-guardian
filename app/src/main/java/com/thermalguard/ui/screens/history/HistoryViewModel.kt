package com.thermalguard.ui.screens.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermalguard.R
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.DailyThermalSummary
import com.thermalguard.domain.model.UserSettings
import com.thermalguard.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class HistoryFilter {
    HOUR_1,
    HOURS_24,
    DAYS_7
}

data class HistoryUiState(
    val selectedFilter: HistoryFilter = HistoryFilter.HOURS_24,
    val records: List<TemperatureEntity> = emptyList(),
    val maxTemp: Float = 0.0f,
    val minTemp: Float = 0.0f,
    val avgTemp: Float = 0.0f,
    val dailySummary: DailyThermalSummary? = null,
    val userSettings: UserSettings = UserSettings(),
    val exportUri: Uri? = null,
    val isExporting: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ThermalRepository,
    private val csvExporter: CsvExporter
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilter.HOURS_24)
    private val _dailySummary = MutableStateFlow<DailyThermalSummary?>(null)
    private val _isExporting = MutableStateFlow(false)

    init {
        loadDailySummary()
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedFilter,
        _selectedFilter.flatMapLatest { filter ->
            val now = System.currentTimeMillis()
            val start = when (filter) {
                HistoryFilter.HOUR_1 -> now - (60 * 60 * 1000L)
                HistoryFilter.HOURS_24 -> now - (24 * 60 * 60 * 1000L)
                HistoryFilter.DAYS_7 -> now - (7 * 24 * 60 * 60 * 1000L)
            }
            repository.getHistory(start, now)
        },
        _dailySummary,
        repository.userSettings,
        _isExporting
    ) { filter, records, daily, settings, exporting ->
        val max = if (records.isNotEmpty()) records.maxOf { it.temperatureCelsius } else 0f
        val min = if (records.isNotEmpty()) records.minOf { it.temperatureCelsius } else 0f
        val avg = if (records.isNotEmpty()) (records.map { it.temperatureCelsius.toDouble() }.average()).toFloat() else 0f

        HistoryUiState(
            selectedFilter = filter,
            records = records,
            maxTemp = max,
            minTemp = min,
            avgTemp = avg,
            dailySummary = daily,
            userSettings = settings,
            isExporting = exporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun selectFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    private fun loadDailySummary() {
        viewModelScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            val now = System.currentTimeMillis()
            val summary = repository.calculateDailySummary(startOfDay, now, 38.0f)
            _dailySummary.value = summary
        }
    }

    fun exportAndShareCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isExporting.value = true
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000L)
            val allRecords = repository.getHistorySync(thirtyDaysAgo, now)

            val uri = csvExporter.exportToCsv(context, allRecords)
            _isExporting.value = false

            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, context.getString(R.string.csv_share_chooser)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        }
    }
}

