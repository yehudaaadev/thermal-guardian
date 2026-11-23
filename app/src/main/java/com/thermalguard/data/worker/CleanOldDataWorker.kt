package com.thermalguard.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thermalguard.data.repository.ThermalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CleanOldDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ThermalRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = repository.userSettings.first()
            val deletedCount = repository.deleteOldRecords(settings.retentionDays)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

