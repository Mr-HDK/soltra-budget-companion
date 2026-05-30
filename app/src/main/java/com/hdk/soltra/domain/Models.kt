package com.hdk.soltra.domain

data class ExpenseRecord(
    val id: Long,
    val amountMinor: Long,
    val occurredAtEpochMillis: Long,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val paymentMethod: PaymentMethod,
    val merchantOrLabel: String?,
    val note: String?,
    val amountExpression: String? = null,
)

data class CategoryModel(
    val id: Long,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val monthlyBudgetMinor: Long,
)

data class BalanceCheckpointModel(
    val id: Long,
    val recordedAtEpochMillis: Long,
    val bankBalanceMinor: Long,
    val cashBalanceMinor: Long,
    val note: String?,
)

data class BudgetConfigModel(
    val monthlyBudgetMinor: Long,
    val currencyCode: String,
    val monthStartDay: Int,
)

data class CategorySpend(
    val categoryName: String,
    val totalMinor: Long,
)

data class ExpenseFilter(
    val search: String = "",
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val fromEpochMillis: Long? = null,
    val toEpochMillis: Long? = null,
)

data class DashboardSummary(
    val todayTotalMinor: Long,
    val monthTotalMinor: Long,
    val monthBudgetMinor: Long,
    val monthRemainingMinor: Long,
    val latestCheckpoint: BalanceCheckpointModel?,
    val byCategory: List<CategorySpend>,
)

data class QuickTemplateModel(
    val id: Long,
    val name: String,
    val defaultAmountMinor: Long?,
    val defaultCategoryId: Long,
    val defaultPaymentMethod: PaymentMethod,
    val defaultNote: String?,
    val isPinned: Boolean,
)

data class RecurringRuleModel(
    val id: Long,
    val name: String,
    val amountMinor: Long,
    val categoryId: Long,
    val paymentMethod: PaymentMethod,
    val note: String?,
    val frequency: RecurrenceFrequency,
    val intervalValue: Int,
    val nextRunEpochMillis: Long,
    val isActive: Boolean,
)

data class AccountModel(
    val id: Long,
    val name: String,
    val type: AccountType,
    val balanceMinor: Long,
    val colorHex: String,
    val isActive: Boolean,
)

data class AccountTransferModel(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val fromAccountName: String,
    val toAccountName: String,
    val amountMinor: Long,
    val occurredAtEpochMillis: Long,
    val note: String?,
)
