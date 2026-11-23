package com.thermalguard.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.thermalguard.domain.model.UserSettings
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context, languageCode: String): Context {
        if (languageCode == UserSettings.LANG_SYSTEM) {
            return context
        }

        val locale = when (languageCode) {
            UserSettings.LANG_ID -> Locale("in")
            UserSettings.LANG_EN -> Locale("en")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return context.createConfigurationContext(config)
    }
}

