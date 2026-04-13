package com.hdk.soltra.data.repository

import androidx.room.withTransaction
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.ExpenseEntity
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.RecurrenceFrequency
import com.hdk.soltra.domain.RecurringRuleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class RecurringRuleRepository(
    private val database: AppDatabase,
) {
    private val dao = database.recurringRuleDao()

    fun observeAll(): Flow<List<RecurringRuleModel>> {
        return dao.observeAll().map { list -> list.map(RecurringRuleEntity::toDomain) }
    }

    suspend fun saveRule(
        id: Long?,
        name: String,
        amountMinor: Long,
        categoryId: Long,
        paymentMethod: PaymentMethod,
        note: String?,
        frequency: RecurrenceFrequency,
        intervalValue: Int,
        nextRunEpochMillis: Long,
        isActive: Boolean,
    ) {
        val now = System.currentTimeMillis()
        val existing = id?.takeIf { it > 0L }?.let { ruleId ->
            dao.getAll().firstOrNull { it.id == ruleId }
        }
        dao.insert(
            RecurringRuleEntity(
                id = id ?: 0L,
                name = name.trim(),
                amountMinor = amountMinor,
                categoryId = categoryId,
                paymentMethod = paymentMethod.name,
                note = note?.trim()?.ifBlank { null },
                frequency = frequency.name,
                intervalValue = intervalValue.coerceIn(1, 365),
                nextRunEpochMillis = nextRunEpochMillis,
                isActive = isActive,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun deleteRule(id: Long) {
        dao.deleteById(id)
    }

    suspend fun generateDueExpenses(nowEpochMillis: Long = System.currentTimeMillis()): Int {
        return database.withTransaction {
            val rules = dao.getActive()
            var generatedCount = 0
            val expenseDao = database.expenseDao()
            val now = System.currentTimeMillis()

            rules.forEach { rule ->
                var nextRun = rule.nextRunEpochMillis
                var guard = 0
                while (nextRun <= nowEpochMillis && guard < 500) {
                    expenseDao.insert(
                        ExpenseEntity(
                            amountMinor = rule.amountMinor,
                            occurredAtEpochMillis = nextRun,
                            categoryId = rule.categoryId,
                            paymentMethod = rule.paymentMethod,
                            merchantOrLabel = rule.name.ifBlank { "Depense recurrente" },
                            note = rule.note,
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                            source = "recurring:${rule.id}",
                        ),
                    )
                    generatedCount += 1
                    nextRun = advance(
                        baseEpochMillis = nextRun,
                        frequency = RecurrenceFrequency.valueOf(rule.frequency),
                        interval = rule.intervalValue.coerceIn(1, 365),
                    )
                    guard += 1
                }
                if (nextRun != rule.nextRunEpochMillis) {
                    dao.update(
                        rule.copy(
                            nextRunEpochMillis = nextRun,
                            updatedAtEpochMillis = now,
                        ),
                    )
                }
            }
            generatedCount
        }
    }

    private fun advance(
        baseEpochMillis: Long,
        frequency: RecurrenceFrequency,
        interval: Int,
    ): Long {
        val zone = ZoneId.systemDefault()
        val zdt = Instant.ofEpochMilli(baseEpochMillis).atZone(zone)
        val shifted = when (frequency) {
            RecurrenceFrequency.DAILY -> zdt.plusDays(interval.toLong())
            RecurrenceFrequency.WEEKLY -> zdt.plusWeeks(interval.toLong())
            RecurrenceFrequency.MONTHLY -> zdt.plusMonths(interval.toLong())
        }
        return shifted.toInstant().toEpochMilli()
    }
}
