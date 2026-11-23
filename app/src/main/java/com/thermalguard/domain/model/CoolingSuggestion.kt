package com.thermalguard.domain.model

import androidx.annotation.StringRes
import com.thermalguard.R

enum class SuggestionType {
    BRIGHTNESS,
    HOTSPOT,
    CHARGING,
    REMOVE_CASE,
    CLOSE_APPS,
    REFRESH_RATE
}

data class CoolingSuggestion(
    val type: SuggestionType,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    @StringRes val actionResId: Int? = null,
    val isApplicable: Boolean = true
) {
    companion object {
        fun getApplicableSuggestions(
            thermalData: ThermalData
        ): List<CoolingSuggestion> {
            val list = mutableListOf<CoolingSuggestion>()

            if (thermalData.thermalLevel != ThermalLevel.COOL) {
                list.add(
                    CoolingSuggestion(
                        type = SuggestionType.BRIGHTNESS,
                        titleResId = R.string.suggestion_brightness_title,
                        descriptionResId = R.string.suggestion_brightness_desc,
                        actionResId = R.string.action_display_settings
                    )
                )

                if (thermalData.isHotspotActive) {
                    list.add(
                        CoolingSuggestion(
                            type = SuggestionType.HOTSPOT,
                            titleResId = R.string.suggestion_hotspot_title,
                            descriptionResId = R.string.suggestion_hotspot_desc,
                            actionResId = R.string.action_hotspot_settings
                        )
                    )
                }

                if (thermalData.batteryState.isCharging) {
                    list.add(
                        CoolingSuggestion(
                            type = SuggestionType.CHARGING,
                            titleResId = R.string.suggestion_charging_title,
                            descriptionResId = R.string.suggestion_charging_desc
                        )
                    )
                }

                list.add(
                    CoolingSuggestion(
                        type = SuggestionType.CLOSE_APPS,
                        titleResId = R.string.suggestion_apps_title,
                        descriptionResId = R.string.suggestion_apps_desc,
                        actionResId = R.string.action_apps_settings
                    )
                )

                list.add(
                    CoolingSuggestion(
                        type = SuggestionType.REFRESH_RATE,
                        titleResId = R.string.suggestion_refresh_rate_title,
                        descriptionResId = R.string.suggestion_refresh_rate_desc,
                        actionResId = R.string.action_display_settings
                    )
                )

                if (thermalData.thermalLevel == ThermalLevel.HOT || thermalData.thermalLevel == ThermalLevel.CRITICAL) {
                    list.add(
                        CoolingSuggestion(
                            type = SuggestionType.REMOVE_CASE,
                            titleResId = R.string.suggestion_case_title,
                            descriptionResId = R.string.suggestion_case_desc
                        )
                    )
                }
            }

            return list
        }
    }
}

