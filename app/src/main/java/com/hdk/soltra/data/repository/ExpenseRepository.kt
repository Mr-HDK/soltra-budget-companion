package com.hdk.soltra.data.repository

import com.hdk.soltra.data.local.dao.ExpenseDao
import com.hdk.soltra.data.local.entity.ExpenseEntity
import com.hdk.soltra.domain.CategorySpend
import com.hdk.soltra.domain.ExpenseFilter
import com.hdk.soltra.domain.ExpenseRecord
import com.hdk.soltra.domain.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
) {
    fun observeExpenses(filter: ExpenseFilter): Flow<List<ExpenseRecord>> {
        return expenseDao.observeFiltered(
            search = filter.search,
            categoryId = filter.categoryId,
            paymentMethod = filter.paymentMethod?.name,
            fromEpochMillis = filter.fromEpochMillis,
            toEpochMillis = filter.toEpochMillis,
        ).map { list -> list.map { it.toDomain() } }
    }

    suspend fun addExpense(
        amountMinor: Long,
        occurredAtEpochMillis: Long,
        categoryId: Long,
        paymentMethod: PaymentMethod,
        merchantOrLabel: String?,
        note: String?,
        amountExpression: String? = null,
    ) {
        val now = System.currentTimeMillis()
        expenseDao.insert(
            ExpenseEntity(
                amountMinor = amountMinor,
                occurredAtEpochMillis = occurredAtEpochMillis,
                categoryId = categoryId,
                paymentMethod = paymentMethod.name,
                merchantOrLabel = merchantOrLabel?.trim()?.ifBlank { null },
                note = note?.trim()?.ifBlank { null },
                amountExpression = amountExpression?.trim()?.ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun updateExpense(
        id: Long,
        amountMinor: Long,
        occurredAtEpochMillis: Long,
        categoryId: Long,
        paymentMethod: PaymentMethod,
        merchantOrLabel: String?,
        note: String?,
        amountExpression: String? = null,
    ) {
        val existing = expenseDao.getById(id) ?: return
        val now = System.currentTimeMillis()
        expenseDao.update(
            ExpenseEntity(
                id = existing.id,
                amountMinor = amountMinor,
                occurredAtEpochMillis = occurredAtEpochMillis,
                categoryId = categoryId,
                paymentMethod = paymentMethod.name,
                merchantOrLabel = merchantOrLabel?.trim()?.ifBlank { null },
                note = note?.trim()?.ifBlank { null },
                amountExpression = amountExpression?.trim()?.ifBlank { null },
                createdAtEpochMillis = existing.createdAtEpochMillis,
                updatedAtEpochMillis = now,
                source = existing.source,
            ),
        )
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.deleteById(id)
    }

    suspend fun monthTotal(fromEpochMillis: Long, toEpochMillis: Long): Long {
        return expenseDao.sumAmountBetween(fromEpochMillis, toEpochMillis)
    }

    suspend fun totalsByCategory(fromEpochMillis: Long, toEpochMillis: Long): List<CategorySpend> {
        return expenseDao.totalsByCategory(fromEpochMillis, toEpochMillis)
            .map { CategorySpend(it.categoryName, it.totalMinor) }
    }

    suspend fun latestOccurredAtEpochMillisOrNull(): Long? {
        return expenseDao.latestOccurredAtEpochMillisOrNull()
    }
}
