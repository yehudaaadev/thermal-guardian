package com.thermalguard.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.thermalguard.data.repository.ThermalRepository
import com.thermalguard.domain.model.ThermalLevel
import com.thermalguard.domain.usecase.FormatTemperatureUseCase
import com.thermalguard.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ThermalOverlayService : Service() {

    @Inject
    lateinit var repository: ThermalRepository

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var observeJob: Job? = null
    private val formatTemp = FormatTemperatureUseCase()

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: run {
            stopSelf()
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        // Programmatic lightweight HUD view (zero extra xml needed)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 14, 24, 14)
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#CC121820"))
                cornerRadius = 32f
                setStroke(2, Color.parseColor("#44FFFFFF"))
            }
            background = bg
        }

        val tvIcon = TextView(this).apply {
            text = "🛡️"
            textSize = 14f
            setPadding(0, 0, 12, 0)
        }

        val tvTemp = TextView(this).apply {
            text = "--.-°C"
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvRate = TextView(this).apply {
            text = ""
            textSize = 11f
            setTextColor(Color.parseColor("#88FFFFFF"))
            setPadding(8, 0, 0, 0)
        }

        container.addView(tvIcon)
        container.addView(tvTemp)
        container.addView(tvRate)

        // Drag & Touch handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        isClick = false
                    }
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        val appIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(appIntent)
                    }
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(container, params)
        overlayView = container

        // Observe live temperature
        observeJob = serviceScope.launch {
            repository.liveThermalData.collect { data ->
                val settings = repository.userSettings.first()
                val tempStr = formatTemp(data.batteryState.temperature, settings.isCelsius)
                tvTemp.text = tempStr

                val levelColor = when (data.thermalLevel) {
                    ThermalLevel.COOL -> Color.parseColor("#4CAF50")
                    ThermalLevel.WARM -> Color.parseColor("#FFA726")
                    ThermalLevel.HOT -> Color.parseColor("#FF5722")
                    ThermalLevel.CRITICAL -> Color.parseColor("#E53935")
                }
                tvTemp.setTextColor(levelColor)

                val rate = data.tempRatePerMinute
                tvRate.text = if (rate > 0.3f) "+${String.format("%.1f", rate)}" else ""
            }
        }
    }

    override fun onDestroy() {
        observeJob?.cancel()
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            overlayView = null
        }
        super.onDestroy()
    }
}

