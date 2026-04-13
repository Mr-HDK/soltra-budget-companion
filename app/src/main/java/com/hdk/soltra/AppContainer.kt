package com.hdk.soltra

import android.content.Context
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.repository.AccountRepository
import com.hdk.soltra.data.repository.BalanceCheckpointRepository
import com.hdk.soltra.data.repository.BudgetRepository
import com.hdk.soltra.data.repository.CategoryRepository
import com.hdk.soltra.data.repository.ExpenseRepository
import com.hdk.soltra.data.repository.ImportExportRepository
import com.hdk.soltra.data.repository.QuickTemplateRepository
import com.hdk.soltra.data.repository.RecurringRuleRepository
import com.hdk.soltra.data.repository.UserSettingsRepository

class AppContainer(
    appContext: Context,
) {
    val context: Context = appContext.applicationContext
    private val database = AppDatabase.getInstance(appContext)

    val expenseRepository = ExpenseRepository(database.expenseDao())
    val categoryRepository = CategoryRepository(database)
    val accountRepository = AccountRepository(database)
    val balanceCheckpointRepository = BalanceCheckpointRepository(database.balanceCheckpointDao())
    val budgetRepository = BudgetRepository(database.budgetConfigDao())
    val quickTemplateRepository = QuickTemplateRepository(database.quickTemplateDao())
    val recurringRuleRepository = RecurringRuleRepository(database)
    val userSettingsRepository = UserSettingsRepository(appContext)
    val importExportRepository = ImportExportRepository(appContext, database)
}
