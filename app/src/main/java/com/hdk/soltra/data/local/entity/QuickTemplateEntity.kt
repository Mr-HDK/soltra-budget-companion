package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quick_templates",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultCategoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("defaultCategoryId")],
)
data class QuickTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val defaultAmountMinor: Long?,
    val defaultCategoryId: Long,
    val defaultPaymentMethod: String,
    val defaultNote: String?,
    val isPinned: Boolean = false,
)
