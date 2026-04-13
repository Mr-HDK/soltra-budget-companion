package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "balance_checkpoints",
    indices = [Index("recordedAtEpochMillis")],
)
data class BalanceCheckpointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recordedAtEpochMillis: Long,
    val bankBalanceMinor: Long,
    val cashBalanceMinor: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
