package com.thermalguard.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import java.lang.reflect.Method
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotspotDetector @Inject constructor() {

    private var hotspotStartTime: Long = 0L
    private var wasHotspotActive: Boolean = false

    /**
     * Checks if Wi-Fi hotspot (tethering) is currently enabled.
     * Uses reflection on WifiManager.isWifiApEnabled() with fallback.
     */
    fun isHotspotActive(context: Context): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager != null) {
                val method: Method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
                method.isAccessible = true
                val isEnabled = method.invoke(wifiManager) as? Boolean ?: false
                updateDuration(isEnabled)
                isEnabled
            } else {
                updateDuration(false)
                false
            }
        } catch (e: Exception) {
            // Fallback: check if tethered network interface exists
            try {
                val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val tethered = cm?.let {
                    val method = it.javaClass.getDeclaredMethod("getTetheredIfaces")
                    method.isAccessible = true
                    val ifaces = method.invoke(it) as? Array<*>
                    ifaces != null && ifaces.isNotEmpty()
                } ?: false
                updateDuration(tethered)
                tethered
            } catch (e2: Exception) {
                updateDuration(false)
                false
            }
        }
    }

    private fun updateDuration(isActive: Boolean) {
        val now = System.currentTimeMillis()
        if (isActive) {
            if (!wasHotspotActive || hotspotStartTime == 0L) {
                hotspotStartTime = now
            }
        } else {
            hotspotStartTime = 0L
        }
        wasHotspotActive = isActive
    }

    fun getHotspotDurationMillis(): Long {
        return if (wasHotspotActive && hotspotStartTime > 0L) {
            System.currentTimeMillis() - hotspotStartTime
        } else {
            0L
        }
    }
}

