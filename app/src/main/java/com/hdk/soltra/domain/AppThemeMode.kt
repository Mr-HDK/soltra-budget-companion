package com.hdk.soltra.domain

enum class AppThemeMode(
    val storageValue: String,
    val label: String,
) {
    SYSTEM("system", "Systeme"),
    LIGHT("light", "Clair"),
    COLORFUL("colorful", "Vivid"),
    ;

    companion object {
        fun fromStorage(value: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}
