package com.hdk.soltra.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hdk.soltra.BudgetCompanionApp
import com.hdk.soltra.util.monthRangeEpochMillis
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? BudgetCompanionApp ?: return Result.retry()
        val settings = app.container.userSettingsRepository

        if (!settings.remindersEnabled.first()) return Result.success()

        val now = System.currentTimeMillis()
        val messages = mutableListOf<String>()

        if (settings.noExpenseReminderEnabled.first()) {
            val thresholdDays = settings.noExpenseReminderDays.first().coerceIn(1, 30)
            val latestExpense = app.container.expenseRepository.latestOccurredAtEpochMillisOrNull()
            val inactiveDays = latestExpense?.let { millisToDays(now - it) } ?: Long.MAX_VALUE
            if (latestExpense == null || inactiveDays >= thresholdDays) {
                messages += "Aucune depense enregistree depuis $thresholdDays jours."
            }
        }

        if (settings.checkpointReminderEnabled.first()) {
            val thresholdDays = settings.checkpointReminderDays.first().coerceIn(1, 90)
            val latestCheckpoint = app.container.balanceCheckpointRepository.latestRecordedAtEpochMillisOrNull()
            val inactiveDays = latestCheckpoint?.let { millisToDays(now - it) } ?: Long.MAX_VALUE
            if (latestCheckpoint == null || inactiveDays >= thresholdDays) {
                messages += "Pense a enregistrer un checkpoint banque/liquide."
            }
        }

        val budget = app.container.budgetRepository.observeConfig().first()
        if (budget.monthlyBudgetMinor > 0L) {
            val (from, to) = monthRangeEpochMillis(
                nowEpochMillis = now,
                monthStartDay = budget.monthStartDay,
            )
            val monthSpent = app.container.expenseRepository.monthTotal(from, to)
            val usagePercent = ((monthSpent * 100) / budget.monthlyBudgetMinor).toInt()
            val warningPercent = settings.budgetWarningPercent.first().coerceIn(1, 100)
            when {
                usagePercent >= 100 -> {
                    messages += "Budget mensuel depasse (${usagePercent}% utilise)."
                }
                usagePercent >= warningPercent -> {
                    messages += "Alerte budget: ${usagePercent}% du budget mensuel deja utilise."
                }
            }
        }

        if (messages.isEmpty()) return Result.success()

        ReminderNotifications.ensureChannel(applicationContext)
        ReminderNotifications.showReminder(applicationContext, messages.joinToString(" "))
        return Result.success()
    }

    private fun millisToDays(durationMillis: Long): Long {
        return TimeUnit.MILLISECONDS.toDays(durationMillis.coerceAtLeast(0L))
    }

    companion object {
        const val WORK_NAME = "budget_reminder_work"
    }
}
