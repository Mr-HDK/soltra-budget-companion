package com.hdk.soltra.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.hdk.soltra.MainActivity
import com.hdk.soltra.R
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.util.minorToMoneyString
import com.hdk.soltra.util.monthRangeEpochMillis
import com.hdk.soltra.util.todayRangeEpochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BudgetOverviewWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAllWidgets(context)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAllWidgets(context)
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, BudgetOverviewWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)
        if (appWidgetIds.isEmpty()) return

        val snapshot = runBlocking(Dispatchers.IO) {
            loadSnapshot(context)
        }

        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_budget_overview).apply {
                setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                setTextViewText(R.id.widget_today_value, snapshot.todaySpentLabel)
                setTextViewText(R.id.widget_remaining_value, snapshot.monthRemainingLabel)
                setTextViewText(R.id.widget_period_value, snapshot.periodLabel)
                val launchIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun loadSnapshot(context: Context): WidgetSnapshot {
        val database = AppDatabase.getInstance(context)
        val expenseDao = database.expenseDao()
        val budgetDao = database.budgetConfigDao()
        val budget = budgetDao.getOrNull()
        val currency = budget?.currencyCode ?: "EUR"
        val monthStartDay = budget?.monthStartDay ?: 1
        val monthlyBudgetMinor = budget?.monthlyBudgetMinor ?: 0L
        val expenses = expenseDao.getAll()

        val now = System.currentTimeMillis()
        val (todayFrom, todayTo) = todayRangeEpochMillis(now)
        val todaySpent = expenses
            .asSequence()
            .filter { it.occurredAtEpochMillis in todayFrom..todayTo }
            .sumOf { it.amountMinor }

        val (monthFrom, monthTo) = monthRangeEpochMillis(
            nowEpochMillis = now,
            monthStartDay = monthStartDay,
        )
        val monthSpent = expenses
            .asSequence()
            .filter { it.occurredAtEpochMillis in monthFrom..monthTo }
            .sumOf { it.amountMinor }
        val monthRemaining = monthlyBudgetMinor - monthSpent

        return WidgetSnapshot(
            todaySpentLabel = todaySpent.minorToMoneyString(currency),
            monthRemainingLabel = monthRemaining.minorToMoneyString(currency),
            periodLabel = if (monthlyBudgetMinor > 0L) {
                "${monthSpent.minorToMoneyString(currency)} / ${monthlyBudgetMinor.minorToMoneyString(currency)}"
            } else {
                "${monthSpent.minorToMoneyString(currency)} (${context.getString(R.string.widget_period_budget_unset)})"
            },
        )
    }

    companion object {
        private const val ACTION_REFRESH = "com.hdk.soltra.widget.ACTION_REFRESH"

        fun refresh(context: Context) {
            val intent = Intent(context, BudgetOverviewWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}

private data class WidgetSnapshot(
    val todaySpentLabel: String,
    val monthRemainingLabel: String,
    val periodLabel: String,
)
