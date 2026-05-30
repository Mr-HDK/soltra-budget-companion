package com.hdk.soltra.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hdk.soltra.AppContainer
import com.hdk.soltra.data.repository.CsvImportResult
import com.hdk.soltra.data.repository.CsvMapping
import com.hdk.soltra.data.repository.CsvPreview
import com.hdk.soltra.data.repository.ExportResult
import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import com.hdk.soltra.domain.AppLockMode
import com.hdk.soltra.domain.AppThemeMode
import com.hdk.soltra.domain.BalanceCheckpointModel
import com.hdk.soltra.domain.BudgetConfigModel
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.CategorySpend
import com.hdk.soltra.domain.ExpenseFilter
import com.hdk.soltra.domain.ExpenseRecord
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphWidgetConfigModel
import com.hdk.soltra.domain.GraphType
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.QuickTemplateModel
import com.hdk.soltra.domain.RecurrenceFrequency
import com.hdk.soltra.domain.RecurringRuleModel
import com.hdk.soltra.i18n.AppLanguagePreference
import com.hdk.soltra.i18n.resolveLocale
import com.hdk.soltra.sync.BackupScheduler
import com.hdk.soltra.sync.ReminderScheduler
import com.hdk.soltra.util.amountExpressionToMinorOrNull
import com.hdk.soltra.util.isAmountExpression
import com.hdk.soltra.util.minorToInputString
import com.hdk.soltra.util.monthRangeEpochMillis
import com.hdk.soltra.util.moneyInputToMinorOrNull
import com.hdk.soltra.util.todayRangeEpochMillis
import com.hdk.soltra.widget.BudgetOverviewWidgetProvider
import com.hdk.soltra.widget.ChartWidgetProvider
import com.hdk.soltra.widget.QuickAddWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class DashboardUiState(
    val todayTotalMinor: Long = 0L,
    val monthTotalMinor: Long = 0L,
    val monthBudgetMinor: Long = 0L,
    val monthRemainingMinor: Long = 0L,
    val budgetUsagePercent: Int = 0,
    val previousPeriodTotalMinor: Long = 0L,
    val trendVsPreviousMinor: Long = 0L,
    val trendVsPreviousPercent: Int? = null,
    val rolling7DaysTotalMinor: Long = 0L,
    val rolling30DaysTotalMinor: Long = 0L,
    val projectedPeriodTotalMinor: Long? = null,
    val projectedOverBudgetMinor: Long? = null,
    val isCurrentPeriod: Boolean = true,
    val latestCheckpoint: BalanceCheckpointModel? = null,
    val latestCheckpointAudit: CheckpointAuditUiState? = null,
    val spentSinceLatestCheckpointMinor: Long? = null,
    val byCategory: List<CategorySpend> = emptyList(),
    val futureProjection: ProjectionTimelineUiState? = null,
)

data class ProjectionTimelineUiState(
    val startBalanceMinor: Long,
    val endBalanceMinor: Long,
    val horizonDays: Int,
    val events: List<ProjectionEventUiState> = emptyList(),
)

data class ProjectionEventUiState(
    val dateEpochMillis: Long,
    val label: String,
    val amountMinor: Long,
    val projectedBalanceAfterMinor: Long,
)

data class AddExpenseUiState(
    val amountInput: String = "",
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CARTE_TPE,
    val merchantOrLabel: String = "",
    val note: String = "",
    val editingExpenseId: Long? = null,
)

data class ExpenseFilterUiState(
    val search: String = "",
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val fromEpochMillis: Long? = null,
    val toEpochMillis: Long? = null,
)

data class AddCheckpointUiState(
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val bankInput: String = "",
    val cashInput: String = "",
    val note: String = "",
    val editingCheckpointId: Long? = null,
)

data class CheckpointAuditUiState(
    val checkpointId: Long,
    val currentCheckpointEpochMillis: Long,
    val previousCheckpointEpochMillis: Long? = null,
    val previousTotalMinor: Long? = null,
    val expensesBetweenMinor: Long? = null,
    val expectedCurrentTotalMinor: Long? = null,
    val actualCurrentTotalMinor: Long,
    val uncontrolledMinor: Long? = null,
)

data class CheckpointHistoryItemUiState(
    val checkpoint: BalanceCheckpointModel,
    val audit: CheckpointAuditUiState? = null,
)

data class LatestCheckpointInsightsUiState(
    val latestCheckpoint: BalanceCheckpointModel? = null,
    val latestCheckpointAudit: CheckpointAuditUiState? = null,
    val spentSinceLatestCheckpointMinor: Long? = null,
)

data class MonthPickerUiState(
    val label: String = "",
    val isCurrentMonth: Boolean = true,
)

data class GraphEditorUiState(
    val config: GraphConfigModel = GraphConfigModel(),
    val editingWidgetId: Long? = null,
)

data class GraphPointUiState(
    val key: String,
    val label: String,
    val valueMinor: Long,
    val colorHex: String,
)

data class GraphPreviewUiState(
    val title: String = "",
    val chartType: GraphType = GraphType.PIE,
    val periodLabel: String = "",
    val groupingLabel: String = "",
    val fromEpochMillis: Long = 0L,
    val toEpochMillis: Long = 0L,
    val totalMinor: Long = 0L,
    val points: List<GraphPointUiState> = emptyList(),
) {
    val hasData: Boolean get() = points.any { it.valueMinor > 0L }
}

data class GraphWidgetUiState(
    val id: Long,
    val order: Int,
    val config: GraphConfigModel,
    val preview: GraphPreviewUiState,
)

private data class DashboardComputedData(
    val monthTotalMinor: Long = 0L,
    val monthBudgetMinor: Long = 0L,
    val monthRemainingMinor: Long = 0L,
    val budgetUsagePercent: Int = 0,
    val previousPeriodTotalMinor: Long = 0L,
    val trendVsPreviousMinor: Long = 0L,
    val trendVsPreviousPercent: Int? = null,
    val rolling7DaysTotalMinor: Long = 0L,
    val rolling30DaysTotalMinor: Long = 0L,
    val projectedPeriodTotalMinor: Long? = null,
    val projectedOverBudgetMinor: Long? = null,
    val isCurrentPeriod: Boolean = true,
    val byCategory: List<CategorySpend> = emptyList(),
)

data class TemplateEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val amountInput: String = "",
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CARTE_TPE,
    val note: String = "",
    val isPinned: Boolean = false,
)

data class RecurringRuleEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val amountInput: String = "",
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CARTE_TPE,
    val note: String = "",
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val intervalValueInput: String = "1",
    val nextRunEpochMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
)

data class CsvImportUiState(
    val sourceUriString: String? = null,
    val preview: CsvPreview? = null,
    val dateColumn: String? = null,
    val amountColumn: String? = null,
    val categoryColumn: String? = null,
    val paymentColumn: String? = null,
    val merchantColumn: String? = null,
    val noteColumn: String? = null,
    val lastResult: CsvImportResult? = null,
)

data class QuickAddOpenRequest(
    val requestId: Long,
    val focusAmount: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val graphConfigJson = Json { ignoreUnknownKeys = true }
    private val maxGraphWidgets = 12

    val categories: StateFlow<List<CategoryModel>> =
        container.categoryRepository.observeActive()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCategories: StateFlow<List<CategoryModel>> =
        container.categoryRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accounts: StateFlow<List<AccountModel>> =
        container.accountRepository.observeAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentTransfers: StateFlow<List<AccountTransferModel>> =
        container.accountRepository.observeRecentTransfers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allTransfersForTracking: StateFlow<List<AccountTransferModel>> =
        container.accountRepository.observeAllTransfers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accountTrends: StateFlow<Map<Long, AccountTrendUiState>> =
        combine(accounts, allTransfersForTracking) { currentAccounts, transfers ->
            buildAccountTrendMap(
                accounts = currentAccounts,
                transfers = transfers,
                zoneId = zoneId,
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val checkpoints: StateFlow<List<BalanceCheckpointModel>> =
        container.balanceCheckpointRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgetConfig: StateFlow<BudgetConfigModel> =
        container.budgetRepository.observeConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetConfigModel(0L, "EUR", 1))

    val templates: StateFlow<List<QuickTemplateModel>> =
        container.quickTemplateRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recurringRules: StateFlow<List<RecurringRuleModel>> =
        container.recurringRuleRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val appLanguagePreference: StateFlow<AppLanguagePreference> =
        container.userSettingsRepository.appLanguagePreference
            .map(AppLanguagePreference::fromStorage)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguagePreference.SYSTEM)

    val appLanguageTag: StateFlow<String?> =
        appLanguagePreference
            .map { preference -> preference.forcedLanguageTag }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val expenseFilterUi = MutableStateFlow(ExpenseFilterUiState())
    val expenseFilter: StateFlow<ExpenseFilterUiState> = expenseFilterUi

    val addExpenseUi = MutableStateFlow(AddExpenseUiState())
    val addCheckpointUi = MutableStateFlow(AddCheckpointUiState())
    val templateEditorUi = MutableStateFlow(TemplateEditorUiState())
    val recurringEditorUi = MutableStateFlow(RecurringRuleEditorUiState())
    val csvImportUi = MutableStateFlow(CsvImportUiState())
    private val graphWidgetsConfigState = MutableStateFlow<List<GraphWidgetConfigModel>>(emptyList())
    private val graphEditorUi = MutableStateFlow(GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle())))
    val graphEditor: StateFlow<GraphEditorUiState> = graphEditorUi

    val bannerMessage = MutableStateFlow<String?>(null)
    private val selectedMonthAnchorEpochMillis = MutableStateFlow(defaultMonthAnchorEpochMillis())
    private val quickAddOpenRequestState = MutableStateFlow<QuickAddOpenRequest?>(null)
    val quickAddOpenRequest: StateFlow<QuickAddOpenRequest?> = quickAddOpenRequestState
    private var quickAddOpenRequestCounter = 0L

    val expenses: StateFlow<List<ExpenseRecord>> =
        expenseFilterUi
            .flatMapLatest { ui ->
                container.expenseRepository.observeExpenses(
                    ExpenseFilter(
                        search = ui.search,
                        categoryId = ui.categoryId,
                        paymentMethod = ui.paymentMethod,
                        fromEpochMillis = ui.fromEpochMillis,
                        toEpochMillis = ui.toEpochMillis,
                    ),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allExpensesForAudit: StateFlow<List<ExpenseRecord>> =
        container.expenseRepository.observeExpenses(ExpenseFilter())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val monthFilter: StateFlow<ExpenseFilter> =
        combine(budgetConfig, selectedMonthAnchorEpochMillis) { budget, monthAnchor ->
            val (from, to) = monthRangeEpochMillis(
                nowEpochMillis = monthAnchor,
                monthStartDay = budget.monthStartDay,
            )
            ExpenseFilter(fromEpochMillis = from, toEpochMillis = to)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseFilter())

    private val monthExpenses: StateFlow<List<ExpenseRecord>> =
        monthFilter
            .flatMapLatest { filter -> container.expenseRepository.observeExpenses(filter) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val todayExpenses: StateFlow<List<ExpenseRecord>> =
        MutableStateFlow(Unit)
            .flatMapLatest {
                val (from, to) = todayRangeEpochMillis()
                container.expenseRepository.observeExpenses(
                    ExpenseFilter(fromEpochMillis = from, toEpochMillis = to),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthPickerUi: StateFlow<MonthPickerUiState> =
        combine(selectedMonthAnchorEpochMillis, appLanguagePreference) { anchor, languagePreference ->
                val selectedMonth = Instant.ofEpochMilli(anchor).atZone(zoneId).toLocalDate().withDayOfMonth(1)
                val nowMonth = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate().withDayOfMonth(1)
                val locale = resolveLocale(languagePreference, Locale.getDefault())
                val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
                MonthPickerUiState(
                    label = selectedMonth.format(monthLabelFormatter).replaceFirstChar { c -> c.titlecase(locale) },
                    isCurrentMonth = selectedMonth == nowMonth,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthPickerUiState())

    val checkpointAuditsById: StateFlow<Map<Long, CheckpointAuditUiState>> =
        combine(checkpoints, allExpensesForAudit) { cps, allExpenses ->
            buildCheckpointAuditMap(checkpoints = cps, expenses = allExpenses)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val checkpointHistory: StateFlow<List<CheckpointHistoryItemUiState>> =
        combine(checkpoints, checkpointAuditsById) { cps, audits ->
            cps.map { checkpoint ->
                CheckpointHistoryItemUiState(
                    checkpoint = checkpoint,
                    audit = audits[checkpoint.id],
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val latestCheckpointInsights: StateFlow<LatestCheckpointInsightsUiState> =
        combine(checkpoints, allExpensesForAudit, checkpointAuditsById) { cps, allExpenses, audits ->
            val latestCheckpoint = cps.firstOrNull()
            LatestCheckpointInsightsUiState(
                latestCheckpoint = latestCheckpoint,
                latestCheckpointAudit = latestCheckpoint?.let { audits[it.id] },
                spentSinceLatestCheckpointMinor = latestCheckpoint?.let { checkpoint ->
                    allExpenses
                        .asSequence()
                        .filter { expense -> expense.occurredAtEpochMillis > checkpoint.recordedAtEpochMillis }
                        .sumOf { expense -> expense.amountMinor }
                },
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LatestCheckpointInsightsUiState())

    private val dashboardComputed: StateFlow<DashboardComputedData> = combine(
        monthExpenses,
        budgetConfig,
        allExpensesForAudit,
        monthFilter,
        selectedMonthAnchorEpochMillis,
    ) { monthList, budget, allExpenses, currentMonthFilter, selectedMonthAnchor ->
        val monthTotal = monthList.sumOf { it.amountMinor }
        val byCategory = monthList.groupBy { it.categoryName }
            .map { (name, list) -> CategorySpend(name, list.sumOf { it.amountMinor }) }
            .sortedByDescending { it.totalMinor }
            .take(6)
        val monthBudget = budget.monthlyBudgetMinor
        val budgetUsagePercent = if (monthBudget > 0L) ((monthTotal * 100) / monthBudget).toInt() else 0

        val from = currentMonthFilter.fromEpochMillis ?: 0L
        val to = currentMonthFilter.toEpochMillis ?: System.currentTimeMillis()
        val periodDurationMillis = (to - from + 1L).coerceAtLeast(1L)
        val previousFrom = from - periodDurationMillis
        val previousTo = from - 1L
        val previousPeriodTotal = allExpenses
            .asSequence()
            .filter { expense -> expense.occurredAtEpochMillis in previousFrom..previousTo }
            .sumOf { expense -> expense.amountMinor }
        val trendVsPreviousMinor = monthTotal - previousPeriodTotal
        val trendVsPreviousPercent = if (previousPeriodTotal > 0L) {
            ((trendVsPreviousMinor.toDouble() * 100.0) / previousPeriodTotal.toDouble()).toInt()
        } else {
            null
        }

        val nowDate = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate()
        val rolling7Start = nowDate.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val rolling30Start = nowDate.minusDays(29).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val rolling7Total = allExpenses
            .asSequence()
            .filter { expense -> expense.occurredAtEpochMillis >= rolling7Start }
            .sumOf { expense -> expense.amountMinor }
        val rolling30Total = allExpenses
            .asSequence()
            .filter { expense -> expense.occurredAtEpochMillis >= rolling30Start }
            .sumOf { expense -> expense.amountMinor }

        val selectedMonth = Instant.ofEpochMilli(selectedMonthAnchor).atZone(zoneId).toLocalDate().withDayOfMonth(1)
        val nowMonth = nowDate.withDayOfMonth(1)
        val isCurrentPeriod = selectedMonth == nowMonth

        val projectedPeriodTotal = if (isCurrentPeriod && monthTotal > 0L) {
            val periodStartDate = Instant.ofEpochMilli(from).atZone(zoneId).toLocalDate()
            val periodEndDate = Instant.ofEpochMilli(to).atZone(zoneId).toLocalDate()
            val totalDays = ChronoUnit.DAYS.between(periodStartDate, periodEndDate).toInt() + 1
            val elapsedDays = when {
                nowDate.isBefore(periodStartDate) -> 1
                nowDate.isAfter(periodEndDate) -> totalDays
                else -> ChronoUnit.DAYS.between(periodStartDate, nowDate).toInt() + 1
            }.coerceAtLeast(1)
            (monthTotal.toDouble() / elapsedDays.toDouble() * totalDays.toDouble()).toLong()
        } else {
            null
        }
        val projectedOverBudget = projectedPeriodTotal
            ?.takeIf { projected -> monthBudget > 0L && projected > monthBudget }
            ?.minus(monthBudget)

        DashboardComputedData(
            monthTotalMinor = monthTotal,
            monthBudgetMinor = monthBudget,
            monthRemainingMinor = monthBudget - monthTotal,
            budgetUsagePercent = budgetUsagePercent,
            previousPeriodTotalMinor = previousPeriodTotal,
            trendVsPreviousMinor = trendVsPreviousMinor,
            trendVsPreviousPercent = trendVsPreviousPercent,
            rolling7DaysTotalMinor = rolling7Total,
            rolling30DaysTotalMinor = rolling30Total,
            projectedPeriodTotalMinor = projectedPeriodTotal,
            projectedOverBudgetMinor = projectedOverBudget,
            isCurrentPeriod = isCurrentPeriod,
            byCategory = byCategory,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardComputedData())

    private val futureProjectionUi: StateFlow<ProjectionTimelineUiState?> =
        combine(recurringRules, accounts) { rules, currentAccounts ->
            val activeRules = rules.filter { it.isActive }
            val activeAccounts = currentAccounts.filter { it.isActive }
            val startBalance = activeAccounts.sumOf { it.balanceMinor }
            if (activeRules.isEmpty()) {
                return@combine ProjectionTimelineUiState(
                    startBalanceMinor = startBalance,
                    endBalanceMinor = startBalance,
                    horizonDays = 30,
                    events = emptyList(),
                )
            }

            val now = System.currentTimeMillis()
            val nowDateTime = Instant.ofEpochMilli(now).atZone(zoneId)
            val horizonDateTime = nowDateTime.plusDays(30)
            val horizonEpoch = horizonDateTime.toInstant().toEpochMilli()

            val rawEvents = mutableListOf<Pair<Long, Pair<String, Long>>>()
            activeRules.forEach { rule ->
                var nextRun = rule.nextRunEpochMillis
                var guard = 0
                while (nextRun <= horizonEpoch && guard < 500) {
                    if (nextRun >= now) {
                        rawEvents += nextRun to (rule.name to rule.amountMinor)
                    }
                    nextRun = advanceRecurring(nextRun, rule.frequency, rule.intervalValue)
                    guard += 1
                }
            }

            val sorted = rawEvents.sortedBy { it.first }
            var running = startBalance
            val events = sorted.map { (epoch, payload) ->
                val (label, amountMinor) = payload
                running -= amountMinor
                ProjectionEventUiState(
                    dateEpochMillis = epoch,
                    label = label,
                    amountMinor = amountMinor,
                    projectedBalanceAfterMinor = running,
                )
            }

            ProjectionTimelineUiState(
                startBalanceMinor = startBalance,
                endBalanceMinor = running,
                horizonDays = 30,
                events = events,
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val dashboardUi: StateFlow<DashboardUiState> = combine(
        todayExpenses,
        latestCheckpointInsights,
        dashboardComputed,
        futureProjectionUi,
    ) { todayList, insights, computed, projection ->
        DashboardUiState(
            todayTotalMinor = todayList.sumOf { it.amountMinor },
            monthTotalMinor = computed.monthTotalMinor,
            monthBudgetMinor = computed.monthBudgetMinor,
            monthRemainingMinor = computed.monthRemainingMinor,
            budgetUsagePercent = computed.budgetUsagePercent,
            previousPeriodTotalMinor = computed.previousPeriodTotalMinor,
            trendVsPreviousMinor = computed.trendVsPreviousMinor,
            trendVsPreviousPercent = computed.trendVsPreviousPercent,
            rolling7DaysTotalMinor = computed.rolling7DaysTotalMinor,
            rolling30DaysTotalMinor = computed.rolling30DaysTotalMinor,
            projectedPeriodTotalMinor = computed.projectedPeriodTotalMinor,
            projectedOverBudgetMinor = computed.projectedOverBudgetMinor,
            isCurrentPeriod = computed.isCurrentPeriod,
            latestCheckpoint = insights.latestCheckpoint,
            latestCheckpointAudit = insights.latestCheckpointAudit,
            spentSinceLatestCheckpointMinor = insights.spentSinceLatestCheckpointMinor,
            byCategory = computed.byCategory,
            futureProjection = projection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    val graphWidgets: StateFlow<List<GraphWidgetUiState>> = combine(
        graphWidgetsConfigState,
        allExpensesForAudit,
        allCategories,
        budgetConfig,
        appLanguagePreference,
    ) { widgetConfigs, allExpenses, categoriesList, budget, languagePreference ->
        val locale = resolveLocale(languagePreference, Locale.getDefault())
        widgetConfigs
            .sortedBy { it.order }
            .map { widget ->
                GraphWidgetUiState(
                    id = widget.id,
                    order = widget.order,
                    config = widget.config,
                    preview = buildGraphPreview(
                        config = widget.config,
                        monthStartDay = budget.monthStartDay,
                        allExpenses = allExpenses,
                        categoriesList = categoriesList,
                        locale = locale,
                    ),
                )
            }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exportFolderUri: StateFlow<String?> =
        container.userSettingsRepository.exportFolderUri
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val backupFileUri: StateFlow<String?> =
        container.userSettingsRepository.backupFileUri
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val autoBackupEnabled: StateFlow<Boolean> =
        container.userSettingsRepository.autoBackupEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val backupIntervalHours: StateFlow<Int> =
        container.userSettingsRepository.backupIntervalHours
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 24)

    val remindersEnabled: StateFlow<Boolean> =
        container.userSettingsRepository.remindersEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val noExpenseReminderEnabled: StateFlow<Boolean> =
        container.userSettingsRepository.noExpenseReminderEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val checkpointReminderEnabled: StateFlow<Boolean> =
        container.userSettingsRepository.checkpointReminderEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val noExpenseReminderDays: StateFlow<Int> =
        container.userSettingsRepository.noExpenseReminderDays
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2)

    val checkpointReminderDays: StateFlow<Int> =
        container.userSettingsRepository.checkpointReminderDays
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 7)

    val budgetWarningPercent: StateFlow<Int> =
        container.userSettingsRepository.budgetWarningPercent
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 80)

    val appLockMode: StateFlow<AppLockMode> =
        container.userSettingsRepository.appLockMode
            .map(AppLockMode::fromStorage)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLockMode.NONE)

    val appLockPin: StateFlow<String?> =
        container.userSettingsRepository.appLockPin
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val appThemeMode: StateFlow<AppThemeMode> =
        container.userSettingsRepository.appThemeMode
            .map(AppThemeMode::fromStorage)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeMode.SYSTEM)

    val quickWidgetDefaultCategoryId: StateFlow<Long?> =
        container.userSettingsRepository.quickWidgetDefaultCategoryId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val quickWidgetDefaultPaymentMethod: StateFlow<PaymentMethod> =
        container.userSettingsRepository.quickWidgetDefaultPaymentMethod
            .map { stored ->
                runCatching { PaymentMethod.valueOf(stored) }.getOrDefault(PaymentMethod.LIQUIDE)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaymentMethod.LIQUIDE)

    init {
        viewModelScope.launch {
            val savedWidgetsJson = container.userSettingsRepository.graphWidgetsJson.first()
            val decodedWidgets = savedWidgetsJson
                ?.let { json ->
                    runCatching {
                        graphConfigJson.decodeFromString(
                            ListSerializer(GraphWidgetConfigModel.serializer()),
                            json,
                        )
                    }.getOrNull()
                }
                ?.map { widget ->
                    widget.copy(config = sanitizeGraphConfig(widget.config))
                }
                .orEmpty()

            val initialWidgets = if (decodedWidgets.isNotEmpty()) {
                normalizeWidgetOrders(decodedWidgets)
            } else {
                val legacyConfig = container.userSettingsRepository.graphPreviewConfigJson.first()
                    ?.let { json -> runCatching { graphConfigJson.decodeFromString(GraphConfigModel.serializer(), json) }.getOrNull() }
                    ?.let(::sanitizeGraphConfig)
                    ?: GraphConfigModel()
                listOf(
                    GraphWidgetConfigModel(
                        id = System.currentTimeMillis(),
                        order = 0,
                        config = legacyConfig,
                    ),
                )
            }
            graphWidgetsConfigState.value = initialWidgets
            persistGraphWidgets(initialWidgets)
            graphEditorUi.value = GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle()))
        }

        viewModelScope.launch {
            val generated = withContext(Dispatchers.IO) {
                container.recurringRuleRepository.generateDueExpenses()
            }
            if (generated > 0) {
                bannerMessage.value = tr(
                    fr = "$generated depenses recurrentes ajoutees",
                    en = "$generated recurring expenses added",
                )
                triggerAutoExportIfConfigured()
                refreshWidget()
            }
        }
    }

    fun setSearch(value: String) {
        expenseFilterUi.value = expenseFilterUi.value.copy(search = value)
    }

    fun moveDashboardMonth(offsetMonths: Int) {
        if (offsetMonths == 0) return
        val current = Instant.ofEpochMilli(selectedMonthAnchorEpochMillis.value).atZone(zoneId).toLocalDate().withDayOfMonth(1)
        val nowMonth = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate().withDayOfMonth(1)
        val shifted = current.plusMonths(offsetMonths.toLong())
        val bounded = if (shifted.isAfter(nowMonth)) nowMonth else shifted
        selectedMonthAnchorEpochMillis.value = monthAnchorEpochMillis(bounded.year, bounded.monthValue)
    }

    fun resetDashboardMonthToCurrent() {
        selectedMonthAnchorEpochMillis.value = defaultMonthAnchorEpochMillis()
    }

    fun setFilterCategory(categoryId: Long?) {
        expenseFilterUi.value = expenseFilterUi.value.copy(categoryId = categoryId)
    }

    fun setFilterPayment(paymentMethod: PaymentMethod?) {
        expenseFilterUi.value = expenseFilterUi.value.copy(paymentMethod = paymentMethod)
    }

    fun setFilterDateRange(fromEpochMillis: Long?, toEpochMillis: Long?) {
        expenseFilterUi.value = expenseFilterUi.value.copy(
            fromEpochMillis = fromEpochMillis,
            toEpochMillis = toEpochMillis,
        )
    }

    fun updateGraphEditorConfig(update: (GraphConfigModel) -> GraphConfigModel) {
        val updated = sanitizeGraphConfig(update(graphEditorUi.value.config))
        graphEditorUi.value = graphEditorUi.value.copy(config = updated)
    }

    fun startEditGraphWidget(widgetId: Long) {
        val widget = graphWidgetsConfigState.value.firstOrNull { it.id == widgetId } ?: return
        graphEditorUi.value = GraphEditorUiState(
            config = sanitizeGraphConfig(widget.config),
            editingWidgetId = widgetId,
        )
    }

    fun cancelGraphWidgetEditing() {
        graphEditorUi.value = GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle()))
    }

    fun addGraphWidgetFromEditor() {
        val config = sanitizeGraphConfig(graphEditorUi.value.config)
        val current = graphWidgetsConfigState.value.sortedBy { it.order }
        if (current.size >= maxGraphWidgets) {
            bannerMessage.value = tr(
                fr = "Limite atteinte ($maxGraphWidgets graphes). Supprime un graphe avant d'en ajouter.",
                en = "Limit reached ($maxGraphWidgets charts). Delete one before adding another.",
            )
            return
        }
        if (!isGraphConfigValidForSave(config)) return
        val nextId = ((current.maxOfOrNull { it.id } ?: 0L) + 1L).coerceAtLeast(System.currentTimeMillis())
        val updated = normalizeWidgetOrders(
            current + GraphWidgetConfigModel(
                id = nextId,
                order = current.size,
                config = config,
            ),
        )
        graphWidgetsConfigState.value = updated
        graphEditorUi.value = GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle()))
        viewModelScope.launch { persistGraphWidgets(updated) }
        bannerMessage.value = tr("Graphe ajoute", "Chart added")
    }

    fun saveGraphWidgetEdit() {
        val editingId = graphEditorUi.value.editingWidgetId ?: return
        val config = sanitizeGraphConfig(graphEditorUi.value.config)
        if (!isGraphConfigValidForSave(config)) return
        val current = graphWidgetsConfigState.value
        if (current.none { it.id == editingId }) return
        val updated = normalizeWidgetOrders(
            current.map { widget ->
                if (widget.id == editingId) widget.copy(config = config) else widget
            },
        )
        graphWidgetsConfigState.value = updated
        graphEditorUi.value = GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle()))
        viewModelScope.launch { persistGraphWidgets(updated) }
        bannerMessage.value = tr("Graphe modifie", "Chart updated")
    }

    fun deleteGraphWidget(widgetId: Long) {
        val updated = normalizeWidgetOrders(graphWidgetsConfigState.value.filterNot { it.id == widgetId })
        graphWidgetsConfigState.value = updated
        if (graphEditorUi.value.editingWidgetId == widgetId) {
            graphEditorUi.value = GraphEditorUiState(config = GraphConfigModel(title = defaultGraphTitle()))
        }
        viewModelScope.launch { persistGraphWidgets(updated) }
        bannerMessage.value = tr("Graphe supprime", "Chart deleted")
    }

    fun moveGraphWidget(widgetId: Long, direction: Int) {
        if (direction == 0) return
        val current = graphWidgetsConfigState.value.sortedBy { it.order }.toMutableList()
        val index = current.indexOfFirst { it.id == widgetId }
        if (index == -1) return
        val target = index + direction
        if (target !in current.indices) return
        val temp = current[index]
        current[index] = current[target]
        current[target] = temp
        // Keep the swapped visual order as canonical before normalization.
        val reordered = current.mapIndexed { idx, widget -> widget.copy(order = idx) }
        val updated = normalizeWidgetOrders(reordered)
        graphWidgetsConfigState.value = updated
        viewModelScope.launch { persistGraphWidgets(updated) }
    }

    fun updateAddExpense(update: (AddExpenseUiState) -> AddExpenseUiState) {
        addExpenseUi.value = update(addExpenseUi.value)
    }

    fun updateAddCheckpoint(update: (AddCheckpointUiState) -> AddCheckpointUiState) {
        addCheckpointUi.value = update(addCheckpointUi.value)
    }

    fun saveExpense() {
        val state = addExpenseUi.value
        val categoryId = state.categoryId
        val amountMinor = state.amountInput.amountExpressionToMinorOrNull()
        if (categoryId == null || amountMinor == null || amountMinor <= 0) {
            bannerMessage.value = tr("Verifier montant et categorie", "Check amount and category")
            return
        }
        viewModelScope.launch {
            val editingId = state.editingExpenseId
            if (editingId == null) {
                container.expenseRepository.addExpense(
                    amountMinor = amountMinor,
                    occurredAtEpochMillis = state.dateEpochMillis,
                    categoryId = categoryId,
                    paymentMethod = state.paymentMethod,
                    merchantOrLabel = state.merchantOrLabel,
                    note = state.note,
                    amountExpression = state.amountInput.takeIf { it.isAmountExpression() },
                )
            } else {
                container.expenseRepository.updateExpense(
                    id = editingId,
                    amountMinor = amountMinor,
                    occurredAtEpochMillis = state.dateEpochMillis,
                    categoryId = categoryId,
                    paymentMethod = state.paymentMethod,
                    merchantOrLabel = state.merchantOrLabel,
                    note = state.note,
                    amountExpression = state.amountInput.takeIf { it.isAmountExpression() },
                )
            }
            addExpenseUi.value = AddExpenseUiState(
                categoryId = categoryId,
                paymentMethod = state.paymentMethod,
            )
            bannerMessage.value = if (editingId == null) tr("Depense enregistree", "Expense saved") else tr("Depense modifiee", "Expense updated")
            triggerAutoExportIfConfigured()
            refreshWidget()
        }
    }

    fun quickAddExpense(
        amountInput: String,
        categoryId: Long?,
        paymentMethod: PaymentMethod,
        occurredAtEpochMillis: Long,
        note: String,
    ): Boolean {
        val amountMinor = amountInput.amountExpressionToMinorOrNull()
        if (categoryId == null || amountMinor == null || amountMinor <= 0L) {
            bannerMessage.value = tr("Verifier montant et categorie", "Check amount and category")
            return false
        }
        viewModelScope.launch {
            container.expenseRepository.addExpense(
                amountMinor = amountMinor,
                occurredAtEpochMillis = occurredAtEpochMillis,
                categoryId = categoryId,
                paymentMethod = paymentMethod,
                merchantOrLabel = null,
                note = note,
                amountExpression = amountInput.takeIf { it.isAmountExpression() },
            )
            addExpenseUi.value = AddExpenseUiState(
                categoryId = categoryId,
                paymentMethod = paymentMethod,
            )
            bannerMessage.value = tr("Depense enregistree", "Expense saved")
            triggerAutoExportIfConfigured()
            refreshWidget()
        }
        return true
    }

    fun startEditExpense(expense: ExpenseRecord) {
        addExpenseUi.value = AddExpenseUiState(
            amountInput = expense.amountExpression ?: expense.amountMinor.minorToInputString(),
            dateEpochMillis = expense.occurredAtEpochMillis,
            categoryId = expense.categoryId,
            paymentMethod = expense.paymentMethod,
            merchantOrLabel = expense.merchantOrLabel.orEmpty(),
            note = expense.note.orEmpty(),
            editingExpenseId = expense.id,
        )
    }

    fun cancelEditExpense() {
        addExpenseUi.value = AddExpenseUiState(
            categoryId = addExpenseUi.value.categoryId,
            paymentMethod = addExpenseUi.value.paymentMethod,
        )
    }

    fun applyTemplate(template: QuickTemplateModel) {
        addExpenseUi.value = AddExpenseUiState(
            amountInput = template.defaultAmountMinor?.minorToInputString().orEmpty(),
            categoryId = template.defaultCategoryId,
            paymentMethod = template.defaultPaymentMethod,
            note = template.defaultNote.orEmpty(),
        )
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            container.expenseRepository.deleteExpense(id)
            if (addExpenseUi.value.editingExpenseId == id) {
                addExpenseUi.value = AddExpenseUiState()
            }
            bannerMessage.value = tr("Depense supprimee", "Expense deleted")
            triggerAutoExportIfConfigured()
            refreshWidget()
        }
    }

    fun startEditCheckpoint(checkpoint: BalanceCheckpointModel) {
        addCheckpointUi.value = AddCheckpointUiState(
            dateEpochMillis = checkpoint.recordedAtEpochMillis,
            bankInput = checkpoint.bankBalanceMinor.minorToInputString(),
            cashInput = checkpoint.cashBalanceMinor.minorToInputString(),
            note = checkpoint.note.orEmpty(),
            editingCheckpointId = checkpoint.id,
        )
    }

    fun cancelEditCheckpoint() {
        addCheckpointUi.value = AddCheckpointUiState()
    }

    fun deleteCheckpoint(id: Long) {
        viewModelScope.launch {
            container.balanceCheckpointRepository.deleteCheckpoint(id)
            if (addCheckpointUi.value.editingCheckpointId == id) {
                addCheckpointUi.value = AddCheckpointUiState()
            }
            bannerMessage.value = tr("Checkpoint supprime", "Checkpoint deleted")
            triggerAutoExportIfConfigured()
        }
    }

    fun saveCheckpoint() {
        val state = addCheckpointUi.value
        val bankMinor = state.bankInput.moneyInputToMinorOrNull()
        val cashMinor = state.cashInput.moneyInputToMinorOrNull()
        if (bankMinor == null || cashMinor == null) {
            bannerMessage.value = tr("Verifier les montants banque/liquide", "Check bank/cash balances")
            return
        }
        viewModelScope.launch {
            val editingId = state.editingCheckpointId
            if (editingId == null) {
                container.balanceCheckpointRepository.addCheckpoint(
                    recordedAtEpochMillis = state.dateEpochMillis,
                    bankBalanceMinor = bankMinor,
                    cashBalanceMinor = cashMinor,
                    note = state.note,
                )
            } else {
                container.balanceCheckpointRepository.updateCheckpoint(
                    id = editingId,
                    recordedAtEpochMillis = state.dateEpochMillis,
                    bankBalanceMinor = bankMinor,
                    cashBalanceMinor = cashMinor,
                    note = state.note,
                )
            }
            addCheckpointUi.value = AddCheckpointUiState()
            bannerMessage.value = if (editingId == null) tr("Checkpoint enregistre", "Checkpoint saved") else tr("Checkpoint modifie", "Checkpoint updated")
            triggerAutoExportIfConfigured()
        }
    }

    fun updateGlobalBudget(input: String) {
        val amount = input.moneyInputToMinorOrNull()
        if (amount == null) {
            bannerMessage.value = tr("Budget global invalide", "Invalid global budget")
            return
        }
        viewModelScope.launch {
            container.budgetRepository.updateGlobalMonthlyBudget(amount)
            bannerMessage.value = tr("Budget global mis a jour", "Global budget updated")
            refreshWidget()
        }
    }

    fun updateCategoryBudget(categoryId: Long, input: String) {
        val amount = input.moneyInputToMinorOrNull()
        if (amount == null) {
            bannerMessage.value = tr("Budget categorie invalide", "Invalid category budget")
            return
        }
        viewModelScope.launch {
            container.categoryRepository.updateBudget(categoryId, amount)
            bannerMessage.value = tr("Budget categorie mis a jour", "Category budget updated")
        }
    }

    fun createCategory(name: String) {
        val normalized = name.trim()
        if (normalized.length < 2) {
            bannerMessage.value = tr("Nom categorie trop court", "Category name is too short")
            return
        }
        viewModelScope.launch {
            val exists = container.categoryRepository.getAll().any { category ->
                category.name.equals(normalized, ignoreCase = true)
            }
            if (exists) {
                bannerMessage.value = tr("Categorie deja existante", "Category already exists")
                return@launch
            }
            container.categoryRepository.createCategory(normalized)
            bannerMessage.value = tr("Categorie ajoutee", "Category added")
        }
    }

    fun renameCategory(categoryId: Long, name: String) {
        val normalized = name.trim()
        if (normalized.length < 2) {
            bannerMessage.value = tr("Nom categorie trop court", "Category name is too short")
            return
        }
        viewModelScope.launch {
            val conflict = container.categoryRepository.getAll().any { category ->
                category.id != categoryId && category.name.equals(normalized, ignoreCase = true)
            }
            if (conflict) {
                bannerMessage.value = tr("Categorie deja existante", "Category already exists")
                return@launch
            }
            container.categoryRepository.renameCategory(categoryId, normalized)
            bannerMessage.value = tr("Categorie renomee", "Category renamed")
        }
    }

    fun setCategoryActive(categoryId: Long, isActive: Boolean) {
        viewModelScope.launch {
            container.categoryRepository.setCategoryActive(categoryId, isActive)
            bannerMessage.value = if (isActive) tr("Categorie reactivee", "Category reactivated") else tr("Categorie desactivee", "Category disabled")
        }
    }

    fun updateCategoryColor(categoryId: Long, colorHex: String) {
        val normalized = colorHex.trim().uppercase(Locale.ROOT)
        if (!Regex("^#[0-9A-F]{6}$").matches(normalized)) {
            bannerMessage.value = tr("Couleur invalide (format #RRGGBB)", "Invalid color (format #RRGGBB)")
            return
        }
        viewModelScope.launch {
            container.categoryRepository.updateCategoryColor(categoryId, normalized)
            bannerMessage.value = tr("Couleur categorie mise a jour", "Category color updated")
        }
    }

    fun updateCategoryIcon(categoryId: Long, iconName: String) {
        val normalized = iconName.trim()
        if (normalized.isBlank()) {
            bannerMessage.value = tr("Icone categorie invalide", "Invalid category icon")
            return
        }
        viewModelScope.launch {
            container.categoryRepository.updateCategoryIcon(categoryId, normalized)
            bannerMessage.value = tr("Icone categorie mise a jour", "Category icon updated")
        }
    }

    fun moveCategoryUp(categoryId: Long) {
        viewModelScope.launch {
            val moved = container.categoryRepository.moveCategory(categoryId, direction = -1)
            if (moved) bannerMessage.value = tr("Categorie deplacee", "Category moved")
        }
    }

    fun moveCategoryDown(categoryId: Long) {
        viewModelScope.launch {
            val moved = container.categoryRepository.moveCategory(categoryId, direction = 1)
            if (moved) bannerMessage.value = tr("Categorie deplacee", "Category moved")
        }
    }

    fun deleteCategory(categoryId: Long, replacementCategoryId: Long?) {
        viewModelScope.launch {
            val result = container.categoryRepository.deleteCategory(categoryId, replacementCategoryId)
            if (result.success && quickWidgetDefaultCategoryId.value == categoryId) {
                container.userSettingsRepository.setQuickWidgetDefaultCategoryId(replacementCategoryId)
            }
            bannerMessage.value = result.message
        }
    }

    fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceInput: String,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.length < 2) {
            bannerMessage.value = tr("Nom compte trop court", "Account name is too short")
            return
        }
        val initialBalanceMinor = if (initialBalanceInput.isBlank()) {
            0L
        } else {
            initialBalanceInput.moneyInputToMinorOrNull() ?: run {
                bannerMessage.value = tr("Solde initial invalide", "Invalid initial balance")
                return
            }
        }
        viewModelScope.launch {
            val exists = accounts.value.any { account ->
                account.name.equals(normalizedName, ignoreCase = true)
            }
            if (exists) {
                bannerMessage.value = tr("Compte deja existant", "Account already exists")
                return@launch
            }
            container.accountRepository.createAccount(
                name = normalizedName,
                type = type,
                initialBalanceMinor = initialBalanceMinor,
            )
            bannerMessage.value = tr("Compte ajoute", "Account added")
        }
    }

    fun renameAccount(accountId: Long, name: String) {
        val normalizedName = name.trim()
        if (normalizedName.length < 2) {
            bannerMessage.value = tr("Nom compte trop court", "Account name is too short")
            return
        }
        viewModelScope.launch {
            val conflict = accounts.value.any { account ->
                account.id != accountId && account.name.equals(normalizedName, ignoreCase = true)
            }
            if (conflict) {
                bannerMessage.value = tr("Compte deja existant", "Account already exists")
                return@launch
            }
            container.accountRepository.renameAccount(accountId, normalizedName)
            bannerMessage.value = tr("Compte renomme", "Account renamed")
        }
    }

    fun setAccountActive(accountId: Long, isActive: Boolean) {
        viewModelScope.launch {
            container.accountRepository.setAccountActive(accountId, isActive)
            bannerMessage.value = if (isActive) tr("Compte reactive", "Account reactivated") else tr("Compte desactive", "Account disabled")
        }
    }

    fun updateAccountTypeAndBalance(
        accountId: Long,
        type: AccountType,
        balanceInput: String,
    ) {
        val balanceMinor = if (balanceInput.isBlank()) {
            0L
        } else {
            balanceInput.moneyInputToMinorOrNull() ?: run {
                bannerMessage.value = tr("Solde compte invalide", "Invalid account balance")
                return
            }
        }
        viewModelScope.launch {
            val updated = container.accountRepository.updateTypeAndBalance(
                accountId = accountId,
                type = type,
                balanceMinor = balanceMinor,
            )
            bannerMessage.value = if (updated) {
                tr("Compte mis a jour", "Account updated")
            } else {
                tr("Compte introuvable", "Account not found")
            }
        }
    }

    fun deleteAccount(
        accountId: Long,
        replacementAccountId: Long? = null,
    ) {
        viewModelScope.launch {
            val result = container.accountRepository.deleteAccount(
                accountId = accountId,
                replacementAccountId = replacementAccountId,
            )
            bannerMessage.value = result.message
        }
    }

    fun transferBetweenAccounts(
        fromAccountId: Long?,
        toAccountId: Long?,
        amountInput: String,
        note: String,
    ) {
        if (fromAccountId == null || toAccountId == null) {
            bannerMessage.value = tr("Selectionner compte source et destination", "Select source and destination accounts")
            return
        }
        val amountMinor = amountInput.moneyInputToMinorOrNull()
        if (amountMinor == null || amountMinor <= 0L) {
            bannerMessage.value = tr("Montant transfert invalide", "Invalid transfer amount")
            return
        }
        viewModelScope.launch {
            val result = container.accountRepository.transfer(
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amountMinor = amountMinor,
                occurredAtEpochMillis = System.currentTimeMillis(),
                note = note,
            )
            bannerMessage.value = result.message
        }
    }

    fun updateMonthStartDay(input: String) {
        val day = input.toIntOrNull()
        if (day == null || day !in 1..28) {
            bannerMessage.value = tr("Jour debut mois invalide (1..28)", "Invalid month start day (1..28)")
            return
        }
        viewModelScope.launch {
            container.budgetRepository.updateMonthStartDay(day)
            bannerMessage.value = tr("Debut de mois budget mis a jour", "Budget month start updated")
            refreshWidget()
        }
    }

    fun updateCurrencyCode(input: String) {
        val normalized = input.trim().uppercase(Locale.ROOT)
        if (!normalized.matches(Regex("^[A-Z]{3}$"))) {
            bannerMessage.value = tr("Devise invalide (ex: EUR, USD, TND)", "Invalid currency (e.g. EUR, USD, TND)")
            return
        }
        viewModelScope.launch {
            container.budgetRepository.updateCurrencyCode(normalized)
            bannerMessage.value = tr("Devise mise a jour", "Currency updated")
            refreshWidget()
        }
    }

    fun updateTemplateEditor(update: (TemplateEditorUiState) -> TemplateEditorUiState) {
        templateEditorUi.value = update(templateEditorUi.value)
    }

    fun editTemplate(template: QuickTemplateModel) {
        templateEditorUi.value = TemplateEditorUiState(
            id = template.id,
            name = template.name,
            amountInput = template.defaultAmountMinor?.minorToInputString().orEmpty(),
            categoryId = template.defaultCategoryId,
            paymentMethod = template.defaultPaymentMethod,
            note = template.defaultNote.orEmpty(),
            isPinned = template.isPinned,
        )
    }

    fun resetTemplateEditor() {
        templateEditorUi.value = TemplateEditorUiState()
    }

    fun saveTemplate() {
        val state = templateEditorUi.value
        val categoryId = state.categoryId
        if (state.name.isBlank() || categoryId == null) {
            bannerMessage.value = tr("Nom et categorie template obligatoires", "Template name and category are required")
            return
        }
        val amountMinor = state.amountInput.amountExpressionToMinorOrNull()
        if (state.amountInput.isNotBlank() && amountMinor == null) {
            bannerMessage.value = tr("Montant template invalide", "Invalid template amount")
            return
        }
        viewModelScope.launch {
            container.quickTemplateRepository.saveTemplate(
                id = state.id,
                name = state.name,
                defaultAmountMinor = amountMinor,
                defaultCategoryId = categoryId,
                defaultPaymentMethod = state.paymentMethod,
                defaultNote = state.note,
                isPinned = state.isPinned,
            )
            templateEditorUi.value = TemplateEditorUiState()
            bannerMessage.value = tr("Template enregistre", "Template saved")
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            container.quickTemplateRepository.deleteTemplate(id)
            if (templateEditorUi.value.id == id) {
                templateEditorUi.value = TemplateEditorUiState()
            }
            bannerMessage.value = tr("Template supprime", "Template deleted")
        }
    }

    fun updateRecurringEditor(update: (RecurringRuleEditorUiState) -> RecurringRuleEditorUiState) {
        recurringEditorUi.value = update(recurringEditorUi.value)
    }

    fun editRecurringRule(rule: RecurringRuleModel) {
        recurringEditorUi.value = RecurringRuleEditorUiState(
            id = rule.id,
            name = rule.name,
            amountInput = rule.amountMinor.minorToInputString(),
            categoryId = rule.categoryId,
            paymentMethod = rule.paymentMethod,
            note = rule.note.orEmpty(),
            frequency = rule.frequency,
            intervalValueInput = rule.intervalValue.toString(),
            nextRunEpochMillis = rule.nextRunEpochMillis,
            isActive = rule.isActive,
        )
    }

    fun resetRecurringEditor() {
        recurringEditorUi.value = RecurringRuleEditorUiState()
    }

    fun saveRecurringRule() {
        val state = recurringEditorUi.value
        val categoryId = state.categoryId
        val amountMinor = state.amountInput.amountExpressionToMinorOrNull()
        val interval = state.intervalValueInput.toIntOrNull()
        if (state.name.isBlank() || categoryId == null || amountMinor == null || amountMinor <= 0L || interval == null || interval <= 0) {
            bannerMessage.value = tr("Verifier nom, montant, categorie et intervalle", "Check name, amount, category and interval")
            return
        }
        viewModelScope.launch {
            container.recurringRuleRepository.saveRule(
                id = state.id,
                name = state.name,
                amountMinor = amountMinor,
                categoryId = categoryId,
                paymentMethod = state.paymentMethod,
                note = state.note,
                frequency = state.frequency,
                intervalValue = interval,
                nextRunEpochMillis = state.nextRunEpochMillis,
                isActive = state.isActive,
            )
            recurringEditorUi.value = RecurringRuleEditorUiState()
            bannerMessage.value = tr("Regle recurrente enregistree", "Recurring rule saved")
        }
    }

    fun deleteRecurringRule(id: Long) {
        viewModelScope.launch {
            container.recurringRuleRepository.deleteRule(id)
            if (recurringEditorUi.value.id == id) {
                recurringEditorUi.value = RecurringRuleEditorUiState()
            }
            bannerMessage.value = tr("Regle recurrente supprimee", "Recurring rule deleted")
        }
    }

    fun runRecurringNow() {
        viewModelScope.launch {
            val created = withContext(Dispatchers.IO) {
                container.recurringRuleRepository.generateDueExpenses()
            }
            bannerMessage.value = if (created > 0) {
                tr("$created depenses recurrentes ajoutees", "$created recurring expenses added")
            } else {
                tr("Aucune depense recurrente due", "No recurring expense due")
            }
            if (created > 0) {
                triggerAutoExportIfConfigured()
                refreshWidget()
            }
        }
    }

    fun setExportFolderUri(uriString: String) {
        viewModelScope.launch {
            container.userSettingsRepository.setExportFolderUri(uriString)
            if (container.userSettingsRepository.autoBackupEnabled.first()) {
                val hours = container.userSettingsRepository.backupIntervalHours.first()
                BackupScheduler.schedule(container.context, hours)
            }
            bannerMessage.value = tr("Dossier export configure", "Export folder configured")
        }
    }

    fun setBackupFileUri(uriString: String) {
        viewModelScope.launch {
            container.userSettingsRepository.setBackupFileUri(uriString)
            if (container.userSettingsRepository.autoBackupEnabled.first()) {
                val hours = container.userSettingsRepository.backupIntervalHours.first()
                BackupScheduler.schedule(container.context, hours)
            }
            bannerMessage.value = tr("Fichier backup configure", "Backup file configured")
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            container.userSettingsRepository.setAutoBackupEnabled(enabled)
            val hours = container.userSettingsRepository.backupIntervalHours.first()
            if (enabled) {
                BackupScheduler.schedule(container.context, hours)
            } else {
                BackupScheduler.cancel(container.context)
            }
        }
    }

    fun setBackupIntervalHours(hours: Int) {
        viewModelScope.launch {
            container.userSettingsRepository.setBackupIntervalHours(hours)
            if (container.userSettingsRepository.autoBackupEnabled.first()) {
                BackupScheduler.schedule(container.context, hours)
            }
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.userSettingsRepository.setRemindersEnabled(enabled)
            if (enabled) {
                ReminderScheduler.schedule(container.context)
            } else {
                ReminderScheduler.cancel(container.context)
            }
        }
    }

    fun setNoExpenseReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.userSettingsRepository.setNoExpenseReminderEnabled(enabled)
        }
    }

    fun setCheckpointReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.userSettingsRepository.setCheckpointReminderEnabled(enabled)
        }
    }

    fun setNoExpenseReminderDays(days: Int) {
        viewModelScope.launch {
            container.userSettingsRepository.setNoExpenseReminderDays(days)
        }
    }

    fun setCheckpointReminderDays(days: Int) {
        viewModelScope.launch {
            container.userSettingsRepository.setCheckpointReminderDays(days)
        }
    }

    fun setBudgetWarningPercent(percent: Int) {
        viewModelScope.launch {
            container.userSettingsRepository.setBudgetWarningPercent(percent)
        }
    }

    fun setAppLockMode(mode: AppLockMode) {
        viewModelScope.launch {
            container.userSettingsRepository.setAppLockMode(mode.storageValue)
            if (mode != AppLockMode.PIN) {
                container.userSettingsRepository.setAppLockPin(null)
            }
            bannerMessage.value = tr("Securite mise a jour", "Security updated")
        }
    }

    fun setAppLockPin(pin: String) {
        val normalized = pin.filter(Char::isDigit)
        if (normalized.length !in 4..6) {
            bannerMessage.value = tr("PIN invalide (4 a 6 chiffres)", "Invalid PIN (4 to 6 digits)")
            return
        }
        viewModelScope.launch {
            container.userSettingsRepository.setAppLockPin(normalized)
            bannerMessage.value = tr("PIN enregistre", "PIN saved")
        }
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            container.userSettingsRepository.setAppThemeMode(mode.storageValue)
            bannerMessage.value = tr("Theme applique", "Theme applied")
        }
    }

    fun setAppLanguagePreference(preference: AppLanguagePreference) {
        viewModelScope.launch {
            container.userSettingsRepository.setAppLanguagePreference(preference.storageValue)
            bannerMessage.value = tr("Langue mise a jour", "Language updated")
        }
    }

    fun setQuickWidgetDefaultCategoryId(categoryId: Long?) {
        viewModelScope.launch {
            container.userSettingsRepository.setQuickWidgetDefaultCategoryId(categoryId)
            refreshWidget()
            bannerMessage.value = tr("Categorie par defaut widget mise a jour", "Widget default category updated")
        }
    }

    fun setQuickWidgetDefaultPaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            container.userSettingsRepository.setQuickWidgetDefaultPaymentMethod(method.name)
            refreshWidget()
            bannerMessage.value = tr("Type par defaut widget mis a jour", "Widget default payment type updated")
        }
    }

    fun exportNow() {
        viewModelScope.launch {
            val folder = container.userSettingsRepository.exportFolderUri.first()
            val backupFile = container.userSettingsRepository.backupFileUri.first()
            val result = withContext(Dispatchers.IO) {
                when {
                    !folder.isNullOrBlank() -> container.importExportRepository.exportAllToFolder(folder)
                    !backupFile.isNullOrBlank() -> container.importExportRepository.exportBackupJsonToFile(backupFile)
                    else -> ExportResult(
                        false,
                        tr(
                            "Configurer un dossier export ou un fichier backup d'abord",
                            "Configure an export folder or backup file first",
                        ),
                    )
                }
            }
            bannerMessage.value = result.message
        }
    }

    fun restoreFromBackup(uri: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                container.importExportRepository.restoreFromBackup(uri)
            }
            bannerMessage.value = result.message
            if (result.success) triggerAutoExportIfConfigured()
        }
    }

    fun previewCsvImport(uri: Uri) {
        viewModelScope.launch {
            val preview = withContext(Dispatchers.IO) {
                container.importExportRepository.previewCsv(uri)
            }
            if (preview == null) {
                bannerMessage.value = tr("CSV invalide ou vide", "Invalid or empty CSV")
                return@launch
            }
            val guessed = guessMapping(preview.headers)
            csvImportUi.value = CsvImportUiState(
                sourceUriString = uri.toString(),
                preview = preview,
                dateColumn = guessed.dateColumn,
                amountColumn = guessed.amountColumn,
                categoryColumn = guessed.categoryColumn,
                paymentColumn = guessed.paymentColumn,
                merchantColumn = guessed.merchantColumn,
                noteColumn = guessed.noteColumn,
            )
        }
    }

    fun updateCsvMapping(update: (CsvImportUiState) -> CsvImportUiState) {
        csvImportUi.value = update(csvImportUi.value)
    }

    fun clearCsvImportState() {
        csvImportUi.value = CsvImportUiState()
    }

    fun importCsvTransactions() {
        val state = csvImportUi.value
        val uri = state.sourceUriString?.let(Uri::parse)
        val dateColumn = state.dateColumn
        val amountColumn = state.amountColumn
        val categoryColumn = state.categoryColumn
        if (uri == null || dateColumn.isNullOrBlank() || amountColumn.isNullOrBlank() || categoryColumn.isNullOrBlank()) {
            bannerMessage.value = tr(
                "Completer source CSV + mapping date/montant/categorie",
                "Complete CSV source + date/amount/category mapping",
            )
            return
        }
        if (setOf(dateColumn, amountColumn, categoryColumn).size < 3) {
            bannerMessage.value = tr(
                "Mapping CSV invalide: date/montant/categorie doivent etre 3 colonnes differentes",
                "Invalid CSV mapping: date/amount/category must use 3 different columns",
            )
            return
        }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                container.importExportRepository.importTransactionsCsv(
                    csvUri = uri,
                    mapping = CsvMapping(
                        dateColumn = dateColumn,
                        amountColumn = amountColumn,
                        categoryColumn = categoryColumn,
                        paymentColumn = state.paymentColumn,
                        merchantColumn = state.merchantColumn,
                        noteColumn = state.noteColumn,
                    ),
                )
            }
            csvImportUi.value = state.copy(lastResult = result)
            bannerMessage.value = tr(
                "Import CSV: +${result.addedCount}, ignores ${result.skippedCount}",
                "CSV import: +${result.addedCount}, skipped ${result.skippedCount}",
            )
            if (result.addedCount > 0) triggerAutoExportIfConfigured()
        }
    }

    fun consumeBanner() {
        bannerMessage.value = null
    }

    fun requestQuickAddOpen(focusAmount: Boolean = false) {
        quickAddOpenRequestCounter += 1
        quickAddOpenRequestState.value = QuickAddOpenRequest(
            requestId = quickAddOpenRequestCounter,
            focusAmount = focusAmount,
        )
    }

    fun consumeQuickAddOpenRequest(requestId: Long) {
        if (quickAddOpenRequestState.value?.requestId == requestId) {
            quickAddOpenRequestState.value = null
        }
    }

    private suspend fun triggerAutoExportIfConfigured() {
        val folder = container.userSettingsRepository.exportFolderUri.first()
        val backupFile = container.userSettingsRepository.backupFileUri.first()
        val result: ExportResult? = withContext(Dispatchers.IO) {
            when {
                !folder.isNullOrBlank() -> container.importExportRepository.exportAllToFolder(folder)
                !backupFile.isNullOrBlank() -> container.importExportRepository.exportBackupJsonToFile(backupFile)
                else -> null
            }
        }
        if (result != null && !result.success) {
            bannerMessage.value = result.message
        }
    }

    private suspend fun persistGraphWidgets(widgets: List<GraphWidgetConfigModel>) {
        val normalized = normalizeWidgetOrders(widgets)
        val encoded = graphConfigJson.encodeToString(
            ListSerializer(GraphWidgetConfigModel.serializer()),
            normalized,
        )
        container.userSettingsRepository.setGraphWidgetsJson(encoded)
    }

    private fun isGraphConfigValidForSave(config: GraphConfigModel): Boolean {
        if (config.period == com.hdk.soltra.domain.GraphPeriod.CUSTOM &&
            (config.customFromEpochMillis == null || config.customToEpochMillis == null)
        ) {
            bannerMessage.value = tr(
                "Choisis une date de debut et une date de fin pour la periode personnalisee.",
                "Choose a start date and end date for the custom period.",
            )
            return false
        }
        return true
    }

    private fun normalizeWidgetOrders(widgets: List<GraphWidgetConfigModel>): List<GraphWidgetConfigModel> {
        return GraphComposerLogic.normalizeWidgetOrders(widgets)
    }

    private fun buildGraphPreview(
        config: GraphConfigModel,
        monthStartDay: Int,
        allExpenses: List<ExpenseRecord>,
        categoriesList: List<CategoryModel>,
        locale: Locale,
    ): GraphPreviewUiState {
        return GraphComposerLogic.buildGraphPreview(
            config = config,
            monthStartDay = monthStartDay,
            allExpenses = allExpenses,
            categories = categoriesList,
            zoneId = zoneId,
            locale = locale,
        )
    }

    private fun sanitizeGraphConfig(config: GraphConfigModel): GraphConfigModel {
        return GraphComposerLogic.sanitizeGraphConfig(config)
    }

    private fun buildCheckpointAuditMap(
        checkpoints: List<BalanceCheckpointModel>,
        expenses: List<ExpenseRecord>,
    ): Map<Long, CheckpointAuditUiState> {
        if (checkpoints.isEmpty()) return emptyMap()

        val checkpointsAsc = checkpoints.sortedBy { it.recordedAtEpochMillis }
        val map = linkedMapOf<Long, CheckpointAuditUiState>()
        checkpointsAsc.forEachIndexed { index, checkpoint ->
            val actualTotal = checkpoint.bankBalanceMinor + checkpoint.cashBalanceMinor
            if (index == 0) {
                map[checkpoint.id] = CheckpointAuditUiState(
                    checkpointId = checkpoint.id,
                    currentCheckpointEpochMillis = checkpoint.recordedAtEpochMillis,
                    actualCurrentTotalMinor = actualTotal,
                )
                return@forEachIndexed
            }

            val previous = checkpointsAsc[index - 1]
            val previousTotal = previous.bankBalanceMinor + previous.cashBalanceMinor
            val spentBetween = expenses
                .asSequence()
                .filter { expense ->
                    expense.occurredAtEpochMillis > previous.recordedAtEpochMillis &&
                        expense.occurredAtEpochMillis <= checkpoint.recordedAtEpochMillis
                }
                .sumOf { expense -> expense.amountMinor }
            val expectedCurrentTotal = previousTotal - spentBetween
            map[checkpoint.id] = CheckpointAuditUiState(
                checkpointId = checkpoint.id,
                currentCheckpointEpochMillis = checkpoint.recordedAtEpochMillis,
                previousCheckpointEpochMillis = previous.recordedAtEpochMillis,
                previousTotalMinor = previousTotal,
                expensesBetweenMinor = spentBetween,
                expectedCurrentTotalMinor = expectedCurrentTotal,
                actualCurrentTotalMinor = actualTotal,
                uncontrolledMinor = expectedCurrentTotal - actualTotal,
            )
        }
        return map
    }

    private fun advanceRecurring(
        baseEpochMillis: Long,
        frequency: RecurrenceFrequency,
        interval: Int,
    ): Long {
        val zdt = Instant.ofEpochMilli(baseEpochMillis).atZone(zoneId)
        val shifted = when (frequency) {
            RecurrenceFrequency.DAILY -> zdt.plusDays(interval.toLong())
            RecurrenceFrequency.WEEKLY -> zdt.plusWeeks(interval.toLong())
            RecurrenceFrequency.MONTHLY -> zdt.plusMonths(interval.toLong())
        }
        return shifted.toInstant().toEpochMilli()
    }

    private fun guessMapping(headers: List<String>): CsvMapping {
        fun find(vararg keys: String): String? {
            return headers.firstOrNull { header ->
                val normalized = normalize(header)
                keys.any { key -> normalized.contains(key) }
            }
        }
        return CsvMapping(
            dateColumn = find("date", "jour") ?: headers.first(),
            amountColumn = find("montant", "amount", "prix", "value") ?: headers.first(),
            categoryColumn = find("categorie", "category", "cat") ?: headers.first(),
            paymentColumn = find("paiement", "payment", "method"),
            merchantColumn = find("merchant", "libelle", "label", "description"),
            noteColumn = find("note", "comment", "memo"),
        )
    }

    private fun normalize(text: String): String = text.trim().lowercase()

    private fun tr(fr: String, en: String): String {
        val locale = resolveLocale(appLanguagePreference.value, Locale.getDefault())
        return if (locale.language.equals("fr", ignoreCase = true)) fr else en
    }

    private fun defaultGraphTitle(): String = tr("Nouveau graphe", "New chart")

    private fun refreshWidget() {
        BudgetOverviewWidgetProvider.refresh(container.context)
        QuickAddWidgetProvider.refresh(container.context)
        ChartWidgetProvider.refresh(container.context)
    }

    private fun defaultMonthAnchorEpochMillis(): Long {
        val now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId).toLocalDate()
        return monthAnchorEpochMillis(now.year, now.monthValue)
    }

    private fun monthAnchorEpochMillis(year: Int, month: Int): Long {
        val anchorDate = LocalDate.of(year, month, 28)
        return anchorDate
            .atTime(12, 0)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(container) as T
        }
    }
}
