package com.hdk.soltra.data.repository

import com.hdk.soltra.data.local.AccountTransferWithAccounts
import com.hdk.soltra.data.local.ExpenseWithCategory
import com.hdk.soltra.data.local.entity.AccountEntity
import com.hdk.soltra.data.local.entity.BalanceCheckpointEntity
import com.hdk.soltra.data.local.entity.BudgetConfigEntity
import com.hdk.soltra.data.local.entity.CategoryEntity
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import com.hdk.soltra.domain.BalanceCheckpointModel
import com.hdk.soltra.domain.BudgetConfigModel
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.ExpenseRecord
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.QuickTemplateModel
import com.hdk.soltra.domain.RecurrenceFrequency
import com.hdk.soltra.domain.RecurringRuleModel

fun ExpenseWithCategory.toDomain(): ExpenseRecord {
    return ExpenseRecord(
        id = expense.id,
        amountMinor = expense.amountMinor,
        occurredAtEpochMillis = expense.occurredAtEpochMillis,
        categoryId = expense.categoryId,
        categoryName = categoryName,
        categoryColorHex = categoryColorHex,
        paymentMethod = PaymentMethod.valueOf(expense.paymentMethod),
        merchantOrLabel = expense.merchantOrLabel,
        note = expense.note,
        amountExpression = expense.amountExpression,
    )
}

fun CategoryEntity.toDomain(): CategoryModel {
    return CategoryModel(
        id = id,
        name = name,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = isActive,
        monthlyBudgetMinor = monthlyBudgetMinor,
    )
}

fun CategoryModel.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = isActive,
        monthlyBudgetMinor = monthlyBudgetMinor,
    )
}

fun BalanceCheckpointEntity.toDomain(): BalanceCheckpointModel {
    return BalanceCheckpointModel(
        id = id,
        recordedAtEpochMillis = recordedAtEpochMillis,
        bankBalanceMinor = bankBalanceMinor,
        cashBalanceMinor = cashBalanceMinor,
        note = note,
    )
}

fun BudgetConfigEntity.toDomain(): BudgetConfigModel {
    return BudgetConfigModel(
        monthlyBudgetMinor = monthlyBudgetMinor,
        currencyCode = currencyCode,
        monthStartDay = monthStartDay,
    )
}

fun QuickTemplateEntity.toDomain(): QuickTemplateModel {
    return QuickTemplateModel(
        id = id,
        name = name,
        defaultAmountMinor = defaultAmountMinor,
        defaultCategoryId = defaultCategoryId,
        defaultPaymentMethod = PaymentMethod.valueOf(defaultPaymentMethod),
        defaultNote = defaultNote,
        isPinned = isPinned,
    )
}

fun RecurringRuleEntity.toDomain(): RecurringRuleModel {
    return RecurringRuleModel(
        id = id,
        name = name,
        amountMinor = amountMinor,
        categoryId = categoryId,
        paymentMethod = PaymentMethod.valueOf(paymentMethod),
        note = note,
        frequency = RecurrenceFrequency.valueOf(frequency),
        intervalValue = intervalValue,
        nextRunEpochMillis = nextRunEpochMillis,
        isActive = isActive,
    )
}

fun AccountEntity.toDomain(): AccountModel {
    val type = runCatching { AccountType.valueOf(type) }.getOrDefault(AccountType.OTHER)
    return AccountModel(
        id = id,
        name = name,
        type = type,
        balanceMinor = balanceMinor,
        colorHex = colorHex,
        isActive = isActive,
    )
}

fun AccountTransferWithAccounts.toDomain(): AccountTransferModel {
    return AccountTransferModel(
        id = transfer.id,
        fromAccountId = transfer.fromAccountId,
        toAccountId = transfer.toAccountId,
        fromAccountName = fromAccountName,
        toAccountName = toAccountName,
        amountMinor = transfer.amountMinor,
        occurredAtEpochMillis = transfer.occurredAtEpochMillis,
        note = transfer.note,
    )
}
