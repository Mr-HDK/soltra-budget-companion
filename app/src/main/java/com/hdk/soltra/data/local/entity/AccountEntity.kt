package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index("isActive"), Index("type")],
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String,
    val balanceMinor: Long,
    val colorHex: String,
    val isActive: Boolean = true,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
