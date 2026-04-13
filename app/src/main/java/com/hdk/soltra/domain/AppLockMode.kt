package com.hdk.soltra.domain

enum class AppLockMode(
    val storageValue: String,
    val label: String,
) {
    NONE("none", "Aucun"),
    PIN("pin", "Code PIN"),
    ;

    companion object {
        fun fromStorage(value: String?): AppLockMode {
            return entries.firstOrNull { it.storageValue == value } ?: NONE
        }
    }
}
