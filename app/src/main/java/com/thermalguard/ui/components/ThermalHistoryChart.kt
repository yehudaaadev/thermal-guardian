package com.thermalguard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermalguard.R
import com.thermalguard.data.local.room.TemperatureEntity
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.theme.ThermalCool
import com.thermalguard.ui.theme.ThermalCritical
import com.thermalguard.ui.theme.ThermalHot
import com.thermalguard.ui.theme.ThermalPrimary
import com.thermalguard.ui.theme.ThermalWarm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ThermalHistoryChart(
    records: List<TemperatureEntity>,
    warmThreshold: Float = 38f,
    hotThreshold: Float = 40f,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val formatTemp = remember { FormatTemperatureUseCase() }

    if (records.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_history),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
        return
    }

    val minTemp = (records.minOf { it.temperatureCelsius } - 2f).coerceAtLeast(20f)
    val maxTemp = (records.maxOf { it.temperatureCelsius } + 3f).coerceAtLeast(45f)
    val tempRange = (maxTemp - minTemp).coerceAtLeast(5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures(
                        onPress = { offset ->
                            val width = size.width
                            val stepX = width / (records.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, records.size - 1)
                            selectedIndex = index
                        },
                        onTap = { offset ->
                            val width = size.width
                            val stepX = width / (records.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, records.size - 1)
                            selectedIndex = index
                        }
                    )
                }
                .pointerInput(records) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val stepX = width / (records.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, records.size - 1)
                            selectedIndex = index
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val width = size.width
                            val stepX = width / (records.size - 1).coerceAtLeast(1)
                            val index = ((change.position.x + stepX / 2) / stepX).toInt().coerceIn(0, records.size - 1)
                            selectedIndex = index
                        },
                        onDragEnd = {
                            selectedIndex = null
                        },
                        onDragCancel = {
                            selectedIndex = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = if (records.size > 1) width / (records.size - 1) else width

            // Draw Threshold Reference Lines (Dashed)
            val warmY = height - ((warmThreshold - minTemp) / tempRange * height)
            if (warmY in 0f..height) {
                drawLine(
                    color = ThermalWarm.copy(alpha = 0.4f),
                    start = Offset(0f, warmY),
                    end = Offset(width, warmY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            val hotY = height - ((hotThreshold - minTemp) / tempRange * height)
            if (hotY in 0f..height) {
                drawLine(
                    color = ThermalHot.copy(alpha = 0.4f),
                    start = Offset(0f, hotY),
                    end = Offset(width, hotY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Build Points & Path
            val points = records.mapIndexed { index, record ->
                val x = index * stepX
                val y = height - ((record.temperatureCelsius - minTemp) / tempRange * height)
                Offset(x, y.coerceIn(0f, height))
            }

            val strokePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cx = (prev.x + curr.x) / 2
                        cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(strokePath)
                if (points.isNotEmpty()) {
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }
            }

            // Draw Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ThermalPrimary.copy(alpha = 0.35f),
                        ThermalPrimary.copy(alpha = 0.05f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Smooth Line
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(ThermalCool, ThermalWarm, ThermalHot, ThermalCritical)
                ),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Selected point scrubber
            selectedIndex?.let { index ->
                if (index in points.indices) {
                    val p = points[index]
                    // Vertical guide line
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(p.x, 0f),
                        end = Offset(p.x, height),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Outer dot
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = p
                    )
                    // Inner dot
                    drawCircle(
                        color = ThermalPrimary,
                        radius = 5.dp.toPx(),
                        center = p
                    )
                }
            }
        }

        // Tooltip pill for selected point
        selectedIndex?.let { index ->
            if (index in records.indices) {
                val record = records[index]
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timeStr = timeFormat.format(Date(record.timestamp))
                val tempStr = formatTemp(record.temperatureCelsius, isCelsius)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color(0xDD0D1B2A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$timeStr • $tempStr",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

