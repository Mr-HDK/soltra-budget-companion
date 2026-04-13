package com.hdk.soltra.data.repository

import com.hdk.soltra.data.local.dao.BudgetConfigDao
import com.hdk.soltra.data.local.entity.BudgetConfigEntity
import com.hdk.soltra.domain.BudgetConfigModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(
    private val dao: BudgetConfigDao,
) {
    fun observeConfig(): Flow<BudgetConfigModel> {
        return dao.observe().map { entity ->
            (entity ?: BudgetConfigEntity()).toDomain()
        }
    }

    suspend fun updateGlobalMonthlyBudget(monthlyBudgetMinor: Long) {
        val current = dao.getOrNull() ?: BudgetConfigEntity()
        dao.upsert(current.copy(monthlyBudgetMinor = monthlyBudgetMinor))
    }

    suspend fun updateMonthStartDay(day: Int) {
        val current = dao.getOrNull() ?: BudgetConfigEntity()
        dao.upsert(current.copy(monthStartDay = day.coerceIn(1, 28)))
    }

    suspend fun updateCurrencyCode(currencyCode: String) {
        val current = dao.getOrNull() ?: BudgetConfigEntity()
        dao.upsert(current.copy(currencyCode = currencyCode))
    }
}
