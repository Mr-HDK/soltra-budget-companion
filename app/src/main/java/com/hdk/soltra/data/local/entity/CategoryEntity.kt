package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["sortOrder", "name"]),
        Index(value = ["isActive", "sortOrder", "name"]),
    ],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int,
    val isActive: Boolean = true,
    val monthlyBudgetMinor: Long = 0L,
)
