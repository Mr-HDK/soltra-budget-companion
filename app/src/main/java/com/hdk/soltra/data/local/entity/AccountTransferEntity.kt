package com.hdk.soltra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account_transfers",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("fromAccountId"), Index("toAccountId"), Index("occurredAtEpochMillis")],
)
data class AccountTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amountMinor: Long,
    val occurredAtEpochMillis: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
)
