package com.thermalguard.domain.model

enum class SystemThermalStatus(val code: Int) {
    NONE(0),
    LIGHT(1),
    MODERATE(2),
    SEVERE(3),
    CRITICAL(4),
    EMERGENCY(5),
    SHUTDOWN(6),
    UNKNOWN(-1);

    companion object {
        fun fromCode(code: Int): SystemThermalStatus {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

