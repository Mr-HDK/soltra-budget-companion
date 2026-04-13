package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_config")
data class BudgetConfigEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyBudgetMinor: Long = 0L,
    val currencyCode: String = "EUR",
    val monthStartDay: Int = 1,
)
