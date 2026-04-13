package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("nextRunEpochMillis")],
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val amountMinor: Long,
    val categoryId: Long,
    val paymentMethod: String,
    val note: String?,
    val frequency: String,
    val intervalValue: Int,
    val nextRunEpochMillis: Long,
    val isActive: Boolean = true,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
