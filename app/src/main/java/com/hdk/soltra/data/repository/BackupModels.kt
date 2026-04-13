package com.hdk.soltra.data.repository

import kotlinx.serialization.Serializable

@Serializable
data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val categories: List<BackupCategory> = emptyList(),
    val expenses: List<BackupExpense> = emptyList(),
    val accounts: List<BackupAccount> = emptyList(),
    val accountTransfers: List<BackupAccountTransfer> = emptyList(),
    val checkpoints: List<BackupCheckpoint> = emptyList(),
    val budgetConfig: BackupBudgetConfig = BackupBudgetConfig(),
    val templates: List<BackupTemplate> = emptyList(),
    val recurringRules: List<BackupRecurringRule> = emptyList(),
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val colorHex: String,
    val iconName: String = "tag",
    val sortOrder: Int,
    val isActive: Boolean,
    val monthlyBudgetMinor: Long,
)

@Serializable
data class BackupExpense(
    val id: Long,
    val amountMinor: Long,
    val occurredAtEpochMillis: Long,
    val categoryId: Long,
    val paymentMethod: String,
    val merchantOrLabel: String?,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val source: String,
)

@Serializable
data class BackupAccount(
    val id: Long,
    val name: String,
    val type: String,
    val balanceMinor: Long,
    val colorHex: String,
    val isActive: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BackupAccountTransfer(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amountMinor: Long,
    val occurredAtEpochMillis: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
)

@Serializable
data class BackupCheckpoint(
    val id: Long,
    val recordedAtEpochMillis: Long,
    val bankBalanceMinor: Long,
    val cashBalanceMinor: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BackupBudgetConfig(
    val monthlyBudgetMinor: Long = 0L,
    val currencyCode: String = "EUR",
    val monthStartDay: Int = 1,
)

@Serializable
data class BackupTemplate(
    val id: Long,
    val name: String,
    val defaultAmountMinor: Long?,
    val defaultCategoryId: Long,
    val defaultPaymentMethod: String,
    val defaultNote: String?,
    val isPinned: Boolean,
)

@Serializable
data class BackupRecurringRule(
    val id: Long,
    val name: String,
    val amountMinor: Long,
    val categoryId: Long,
    val paymentMethod: String,
    val note: String?,
    val frequency: String,
    val intervalValue: Int,
    val nextRunEpochMillis: Long,
    val isActive: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
