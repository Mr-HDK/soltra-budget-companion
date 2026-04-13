package com.hdk.soltra.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.hdk.soltra.data.local.entity.ExpenseEntity

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @ColumnInfo(name = "categoryName") val categoryName: String,
    @ColumnInfo(name = "categoryColorHex") val categoryColorHex: String,
)
