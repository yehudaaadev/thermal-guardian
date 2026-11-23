package com.thermalguard.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class StressTestSample(
    val elapsedSeconds: Int,
    val totalSeconds: Int,
    val currentTemp: Float,
    val isSafetyAborted: Boolean = false
)

data class StressTestResult(
    val initialTemp: Float,
    val peakTemp: Float,
    val tempDelta: Float,
    val dissipationScore: Int, // 0 - 100
    val durationSeconds: Int,
    val wasAbortedForSafety: Boolean
)

@Singleton
class StressTestRunner @Inject constructor(
    private val sensorManager: ThermalSensorManager
) {
    private var stressJob: Job? = null
    private var workerJobs = mutableListOf<Job>()

    fun startTest(
        context: Context,
        durationSeconds: Int = 30,
        safetyAbortTemp: Float = 42.0f,
        scope: CoroutineScope,
        onProgress: (StressTestSample) -> Unit,
        onComplete: (StressTestResult) -> Unit
    ) {
        cancelTest()

        stressJob = scope.launch(Dispatchers.Default) {
            val startTemp = sensorManager.readBatteryState(context).temperature
            var peakTemp = startTemp
            var aborted = false

            // Start CPU stress threads
            val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
            workerJobs = (1..cores).map {
                launch(Dispatchers.Default) {
                    var x = 1.00001
                    while (isActive) {
                        for (i in 0 until 100_000) {
                            x = kotlin.math.sin(x) * kotlin.math.cos(x) + kotlin.math.sqrt(x.coerceAtLeast(0.1))
                        }
                    }
                }
            }.toMutableList()

            // Monitor loop every 1 second
            for (sec in 1..durationSeconds) {
                delay(1000L)
                if (!isActive) break

                val currentTemp = sensorManager.readBatteryState(context).temperature
                if (currentTemp > peakTemp) {
                    peakTemp = currentTemp
                }

                // Check safety threshold
                if (currentTemp >= safetyAbortTemp) {
                    aborted = true
                    onProgress(
                        StressTestSample(
                            elapsedSeconds = sec,
                            totalSeconds = durationSeconds,
                            currentTemp = currentTemp,
                            isSafetyAborted = true
                        )
                    )
                    break
                }

                onProgress(
                    StressTestSample(
                        elapsedSeconds = sec,
                        totalSeconds = durationSeconds,
                        currentTemp = currentTemp,
                        isSafetyAborted = false
                    )
                )
            }

            // Stop workers
            workerJobs.forEach { it.cancel() }
            workerJobs.clear()

            val tempDelta = (peakTemp - startTemp).coerceAtLeast(0f)
            // Score formula: lower temp rise under load = higher score
            // 0 rise -> 100 pts, 1°C rise -> 85 pts, 2°C rise -> 70 pts, >4°C -> 40 pts
            val rawScore = 100 - (tempDelta * 15).toInt()
            val finalScore = rawScore.coerceIn(30, 99)

            onComplete(
                StressTestResult(
                    initialTemp = startTemp,
                    peakTemp = peakTemp,
                    tempDelta = tempDelta,
                    dissipationScore = finalScore,
                    durationSeconds = durationSeconds,
                    wasAbortedForSafety = aborted
                )
            )
        }
    }

    fun cancelTest() {
        workerJobs.forEach { it.cancel() }
        workerJobs.clear()
        stressJob?.cancel()
        stressJob = null
    }

    fun isRunning(): Boolean {
        return stressJob?.isActive == true
    }
}

