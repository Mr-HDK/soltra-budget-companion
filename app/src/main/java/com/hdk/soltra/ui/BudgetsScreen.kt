package com.hdk.soltra.ui

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import com.hdk.soltra.domain.BudgetConfigModel
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.i18n.AppTextKey
import com.hdk.soltra.i18n.localized
import com.hdk.soltra.ui.components.SoltraOutlineCard
import com.hdk.soltra.ui.components.SoltraSectionCard
import com.hdk.soltra.util.formatDateTime
import com.hdk.soltra.util.minorToInputString
import com.hdk.soltra.util.minorToMoneyString
import java.util.Locale
import kotlin.math.abs

private enum class BudgetSection(val labelKey: AppTextKey) {
    BUDGET(AppTextKey.BUDGET_SECTION_BUDGET),
    CATEGORIES(AppTextKey.BUDGET_SECTION_CATEGORIES),
    ACCOUNTS(AppTextKey.BUDGET_SECTION_ACCOUNTS),
    TRANSFERS(AppTextKey.BUDGET_SECTION_TRANSFERS),
}

private enum class CategoryPane(val labelKey: AppTextKey) {
    BUDGETS(AppTextKey.ROOT_TAB_BUDGETS),
    MANAGE(AppTextKey.BUDGETS_PANE_MANAGE),
}

private enum class CategoryManageFilter(val labelKey: AppTextKey) {
    ALL(AppTextKey.BUDGETS_FILTER_ALL),
    ACTIVE(AppTextKey.BUDGETS_FILTER_ACTIVE),
    INACTIVE(AppTextKey.BUDGETS_FILTER_INACTIVE),
}

private const val localCategoryInitialBatchSize = 12
private const val localCategoryBatchStep = 12

private data class LocalCategoryIconOption(
    val key: String,
    val labelKey: AppTextKey,
    val icon: ImageVector,
)

private val localCategoryColorOptions = listOf(
    "#0E6B68",
    "#132338",
    "#C9823A",
    "#2E7D32",
    "#006C9C",
    "#8A4F90",
    "#B33D3D",
    "#4F5F78",
    "#5D4037",
    "#607D8B",
    "#0A8A73",
)

private val localCategoryIconOptions = listOf(
    LocalCategoryIconOption("utensils", AppTextKey.CATEGORY_ICON_MEALS, Icons.Default.Restaurant),
    LocalCategoryIconOption("coffee", AppTextKey.CATEGORY_ICON_COFFEE, Icons.Default.LocalCafe),
    LocalCategoryIconOption("transport", AppTextKey.CATEGORY_ICON_TRANSPORT, Icons.Default.DirectionsBus),
    LocalCategoryIconOption("party", AppTextKey.CATEGORY_ICON_PARTY, Icons.Default.Celebration),
    LocalCategoryIconOption("shopping", AppTextKey.CATEGORY_ICON_SHOPPING, Icons.Default.ShoppingCart),
    LocalCategoryIconOption("health", AppTextKey.CATEGORY_ICON_HEALTH, Icons.Default.Favorite),
    LocalCategoryIconOption("home", AppTextKey.CATEGORY_ICON_HOME, Icons.Default.Home),
    LocalCategoryIconOption("subscription", AppTextKey.CATEGORY_ICON_SUBSCRIPTIONS, Icons.Default.Subscriptions),
    LocalCategoryIconOption("gift", AppTextKey.CATEGORY_ICON_GIFTS, Icons.Default.CardGiftcard),
    LocalCategoryIconOption("document", AppTextKey.CATEGORY_ICON_ADMIN, Icons.Default.Description),
    LocalCategoryIconOption("tag", AppTextKey.CATEGORY_ICON_MISC, Icons.AutoMirrored.Filled.Label),
)

private fun AccountType.labelKey(): AppTextKey {
    return when (this) {
        AccountType.CASH -> AppTextKey.ACCOUNT_TYPE_CASH
        AccountType.BANK -> AppTextKey.ACCOUNT_TYPE_BANK
        AccountType.CARD -> AppTextKey.ACCOUNT_TYPE_CARD
        AccountType.EWALLET -> AppTextKey.ACCOUNT_TYPE_EWALLET
        AccountType.OTHER -> AppTextKey.ACCOUNT_TYPE_OTHER
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BudgetsScreen(
    budgetConfig: BudgetConfigModel,
    dashboard: DashboardUiState,
    monthLabel: String,
    categories: List<CategoryModel>,
    accounts: List<AccountModel>,
    accountTrends: Map<Long, AccountTrendUiState>,
    recentTransfers: List<AccountTransferModel>,
    onUpdateGlobal: (String) -> Unit,
    onUpdateCategory: (Long, String) -> Unit,
    onUpdateMonthStartDay: (String) -> Unit,
    onUpdateCurrencyCode: (String) -> Unit,
    onCreateCategory: (String) -> Unit,
    onRenameCategory: (Long, String) -> Unit,
    onSetCategoryActive: (Long, Boolean) -> Unit,
    onUpdateCategoryColor: (Long, String) -> Unit,
    onUpdateCategoryIcon: (Long, String) -> Unit,
    onMoveCategoryUp: (Long) -> Unit,
    onMoveCategoryDown: (Long) -> Unit,
    onDeleteCategory: (Long, Long?) -> Unit,
    onCreateAccount: (String, AccountType, String) -> Unit,
    onRenameAccount: (Long, String) -> Unit,
    onUpdateAccountTypeAndBalance: (Long, AccountType, String) -> Unit,
    onSetAccountActive: (Long, Boolean) -> Unit,
    onDeleteAccount: (Long, Long?) -> Unit,
    onTransferBetweenAccounts: (Long?, Long?, String, String) -> Unit,
) {
    val activeCategories = remember(categories) { categories.filter { it.isActive } }
    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    val orderedCategories = remember(categories) { categories.sortedBy { it.sortOrder } }
    val manageActiveCount = remember(orderedCategories) { orderedCategories.count { it.isActive } }
    val manageInactiveCount = remember(orderedCategories) { orderedCategories.count { !it.isActive } }
    var selectedSection by rememberSaveable { mutableStateOf(BudgetSection.BUDGET) }
    var selectedCategoryPane by rememberSaveable { mutableStateOf(CategoryPane.BUDGETS) }
    var selectedManageFilter by rememberSaveable { mutableStateOf(CategoryManageFilter.ALL) }

    var globalInput by remember(budgetConfig.monthlyBudgetMinor) { mutableStateOf((budgetConfig.monthlyBudgetMinor / 100.0).toString()) }
    var monthStartInput by remember(budgetConfig.monthStartDay) { mutableStateOf(budgetConfig.monthStartDay.toString()) }
    var currencyInput by remember(budgetConfig.currencyCode) { mutableStateOf(budgetConfig.currencyCode) }
    var newCategoryInput by remember { mutableStateOf("") }
    var categorySearchInput by rememberSaveable { mutableStateOf("") }
    var expandedCategoryEditorId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingCategoryDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteCategoryReplacementId by rememberSaveable { mutableStateOf<Long?>(null) }
    var categoriesOpenStartNanos by remember { mutableStateOf<Long?>(null) }
    var categoriesOpenMeasurePending by remember { mutableStateOf(false) }
    var lastCategoriesOpenMs by rememberSaveable { mutableStateOf<Long?>(null) }
    var lastCategoriesOpenCount by rememberSaveable { mutableStateOf(0) }
    var lastCategoriesOpenPane by rememberSaveable { mutableStateOf(CategoryPane.BUDGETS) }
    var budgetVisibleCount by rememberSaveable { mutableStateOf(localCategoryInitialBatchSize) }
    var manageVisibleCount by rememberSaveable { mutableStateOf(localCategoryInitialBatchSize) }
    var editingBudgetCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingBudgetInput by rememberSaveable { mutableStateOf("") }
    var newAccountName by remember { mutableStateOf("") }
    var newAccountBalanceInput by remember { mutableStateOf("") }
    var newAccountType by remember { mutableStateOf(AccountType.BANK) }
    var pendingAccountDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteAccountReplacementId by rememberSaveable { mutableStateOf<Long?>(null) }
    var transferFromAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var transferToAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var transferAmountInput by rememberSaveable { mutableStateOf("") }
    var transferNoteInput by rememberSaveable { mutableStateOf("") }

    val filteredBudgetCategories = remember(activeCategories, categorySearchInput) {
        val query = categorySearchInput.trim()
        if (query.isBlank()) activeCategories else activeCategories.filter { it.name.contains(query, ignoreCase = true) }
    }
    val filteredManageCategories = remember(orderedCategories, categorySearchInput, selectedManageFilter) {
        val query = categorySearchInput.trim()
        orderedCategories
            .asSequence()
            .filter { category ->
                when (selectedManageFilter) {
                    CategoryManageFilter.ALL -> true
                    CategoryManageFilter.ACTIVE -> category.isActive
                    CategoryManageFilter.INACTIVE -> !category.isActive
                }
            }
            .filter { category ->
                query.isBlank() || category.name.contains(query, ignoreCase = true)
            }
            .toList()
    }
    val visibleBudgetCategories = remember(filteredBudgetCategories, budgetVisibleCount) {
        filteredBudgetCategories.take(budgetVisibleCount)
    }
    val visibleManageCategories = remember(filteredManageCategories, manageVisibleCount) {
        filteredManageCategories.take(manageVisibleCount)
    }
    val hasMoreBudgetCategories = filteredBudgetCategories.size > visibleBudgetCategories.size
    val hasMoreManageCategories = filteredManageCategories.size > visibleManageCategories.size

    LaunchedEffect(activeAccounts) {
        if (activeAccounts.isEmpty()) {
            transferFromAccountId = null
            transferToAccountId = null
            return@LaunchedEffect
        }
        if (transferFromAccountId == null || activeAccounts.none { it.id == transferFromAccountId }) {
            transferFromAccountId = activeAccounts.first().id
        }
        if (transferToAccountId == null || activeAccounts.none { it.id == transferToAccountId } || transferToAccountId == transferFromAccountId) {
            transferToAccountId = activeAccounts.firstOrNull { it.id != transferFromAccountId }?.id
        }
    }

    LaunchedEffect(pendingCategoryDeleteId) {
        deleteCategoryReplacementId = null
    }

    LaunchedEffect(pendingAccountDeleteId) {
        deleteAccountReplacementId = null
    }

    LaunchedEffect(selectedCategoryPane, categorySearchInput, selectedManageFilter) {
        if (selectedCategoryPane == CategoryPane.BUDGETS) {
            budgetVisibleCount = localCategoryInitialBatchSize
            editingBudgetCategoryId = null
        } else {
            manageVisibleCount = localCategoryInitialBatchSize
        }
    }

    LaunchedEffect(filteredBudgetCategories, editingBudgetCategoryId) {
        val editingId = editingBudgetCategoryId ?: return@LaunchedEffect
        if (filteredBudgetCategories.none { it.id == editingId }) {
            editingBudgetCategoryId = null
        }
    }

    LaunchedEffect(selectedSection) {
        if (selectedSection == BudgetSection.CATEGORIES) {
            categoriesOpenStartNanos = null
            categoriesOpenMeasurePending = true
            withFrameNanos { frameNanos ->
                categoriesOpenStartNanos = frameNanos
            }
        }
    }

    LaunchedEffect(selectedSection, selectedCategoryPane, filteredBudgetCategories.size, filteredManageCategories.size) {
        if (selectedSection != BudgetSection.CATEGORIES || !categoriesOpenMeasurePending) return@LaunchedEffect
        val startNanos = categoriesOpenStartNanos ?: return@LaunchedEffect
        withFrameNanos { frameNanos ->
            lastCategoriesOpenMs = ((frameNanos - startNanos) / 1_000_000L).coerceAtLeast(0L)
            lastCategoriesOpenPane = selectedCategoryPane
            lastCategoriesOpenCount = when (selectedCategoryPane) {
                CategoryPane.BUDGETS -> filteredBudgetCategories.size
                CategoryPane.MANAGE -> filteredManageCategories.size
            }
            categoriesOpenMeasurePending = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SoltraSectionCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(localized(AppTextKey.BUDGETS_SECTIONS_TITLE), fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BudgetSection.entries.forEach { section ->
                            FilterChip(
                                selected = selectedSection == section,
                                onClick = { selectedSection = section },
                                label = { Text(localized(section.labelKey)) },
                            )
                        }
                    }
                }
            }
        }

        when (selectedSection) {
            BudgetSection.BUDGET -> budgetOverviewSection(
                budgetConfig = budgetConfig,
                dashboard = dashboard,
                monthLabel = monthLabel,
                activeCategories = activeCategories,
                activeAccounts = activeAccounts,
                globalInput = globalInput,
                onGlobalInputChange = { globalInput = it },
                currencyInput = currencyInput,
                onCurrencyInputChange = { currencyInput = it.uppercase(Locale.ROOT).filter(Char::isLetter).take(3) },
                monthStartInput = monthStartInput,
                onMonthStartInputChange = { monthStartInput = it.filter(Char::isDigit).take(2) },
                onUpdateGlobal = { onUpdateGlobal(globalInput) },
                onUpdateCurrencyCode = { onUpdateCurrencyCode(currencyInput) },
                onUpdateMonthStartDay = { onUpdateMonthStartDay(monthStartInput) },
            )

            BudgetSection.CATEGORIES -> categoriesSection(
                budgetConfig = budgetConfig,
                orderedCategories = orderedCategories,
                filteredBudgetCategories = filteredBudgetCategories,
                filteredManageCategories = filteredManageCategories,
                selectedCategoryPane = selectedCategoryPane,
                onSelectCategoryPane = { selectedCategoryPane = it },
                selectedManageFilter = selectedManageFilter,
                onSelectManageFilter = { selectedManageFilter = it },
                manageActiveCount = manageActiveCount,
                manageInactiveCount = manageInactiveCount,
                lastCategoriesOpenMs = lastCategoriesOpenMs,
                lastCategoriesOpenCount = lastCategoriesOpenCount,
                lastCategoriesOpenPane = lastCategoriesOpenPane,
                budgetCategoriesToDisplay = visibleBudgetCategories,
                manageCategoriesToDisplay = visibleManageCategories,
                hasMoreBudgetCategories = hasMoreBudgetCategories,
                hasMoreManageCategories = hasMoreManageCategories,
                onLoadMoreBudgetCategories = {
                    budgetVisibleCount += localCategoryBatchStep
                },
                onLoadMoreManageCategories = {
                    manageVisibleCount += localCategoryBatchStep
                },
                editingBudgetCategoryId = editingBudgetCategoryId,
                editingBudgetInput = editingBudgetInput,
                onEditingBudgetInputChange = { editingBudgetInput = it },
                onStartBudgetEdit = { category ->
                    editingBudgetCategoryId = category.id
                    editingBudgetInput = category.monthlyBudgetMinor.minorToInputString()
                },
                onCancelBudgetEdit = {
                    editingBudgetCategoryId = null
                },
                onApplyBudgetEdit = { categoryId ->
                    onUpdateCategory(categoryId, editingBudgetInput)
                    editingBudgetCategoryId = null
                },
                categorySearchInput = categorySearchInput,
                onCategorySearchInputChange = { categorySearchInput = it.take(30) },
                newCategoryInput = newCategoryInput,
                onNewCategoryInputChange = { newCategoryInput = it.take(30) },
                onCreateCategory = {
                    onCreateCategory(newCategoryInput)
                    newCategoryInput = ""
                },
                expandedCategoryEditorId = expandedCategoryEditorId,
                onExpandedCategoryEditorChange = { expandedCategoryEditorId = it },
                onUpdateCategory = onUpdateCategory,
                onRenameCategory = onRenameCategory,
                onSetCategoryActive = onSetCategoryActive,
                onUpdateCategoryColor = onUpdateCategoryColor,
                onUpdateCategoryIcon = onUpdateCategoryIcon,
                onMoveCategoryUp = onMoveCategoryUp,
                onMoveCategoryDown = onMoveCategoryDown,
                onRequestDeleteCategory = { pendingCategoryDeleteId = it },
            )

            BudgetSection.ACCOUNTS -> accountsSection(
                budgetConfig = budgetConfig,
                accounts = accounts,
                accountTrends = accountTrends,
                newAccountName = newAccountName,
                onNewAccountNameChange = { newAccountName = it.take(30) },
                newAccountBalanceInput = newAccountBalanceInput,
                onNewAccountBalanceInputChange = { newAccountBalanceInput = it },
                newAccountType = newAccountType,
                onNewAccountTypeChange = { newAccountType = it },
                onCreateAccount = {
                    onCreateAccount(newAccountName, newAccountType, newAccountBalanceInput)
                    newAccountName = ""
                    newAccountBalanceInput = ""
                },
                onRenameAccount = onRenameAccount,
                onUpdateAccountTypeAndBalance = onUpdateAccountTypeAndBalance,
                onSetAccountActive = onSetAccountActive,
                onRequestDeleteAccount = { pendingAccountDeleteId = it },
            )

            BudgetSection.TRANSFERS -> transfersSection(
                budgetConfig = budgetConfig,
                activeAccounts = activeAccounts,
                recentTransfers = recentTransfers,
                transferFromAccountId = transferFromAccountId,
                onTransferFromAccountChange = { selectedId ->
                    transferFromAccountId = selectedId
                    if (transferToAccountId == selectedId) {
                        transferToAccountId = activeAccounts.firstOrNull { it.id != selectedId }?.id
                    }
                },
                transferToAccountId = transferToAccountId,
                onTransferToAccountChange = { transferToAccountId = it },
                transferAmountInput = transferAmountInput,
                onTransferAmountInputChange = { transferAmountInput = it },
                transferNoteInput = transferNoteInput,
                onTransferNoteInputChange = { transferNoteInput = it.take(60) },
                onTransferBetweenAccounts = {
                    onTransferBetweenAccounts(
                        transferFromAccountId,
                        transferToAccountId,
                        transferAmountInput,
                        transferNoteInput,
                    )
                    transferAmountInput = ""
                    transferNoteInput = ""
                },
            )
        }
    }

    orderedCategories.firstOrNull { it.id == pendingCategoryDeleteId }?.let { category ->
        DeleteCategoryDialog(
            category = category,
            replacementCandidates = orderedCategories.filter { it.id != category.id },
            selectedReplacementId = deleteCategoryReplacementId,
            onReplacementSelected = { replacementId -> deleteCategoryReplacementId = replacementId },
            onConfirm = {
                onDeleteCategory(category.id, deleteCategoryReplacementId)
                pendingCategoryDeleteId = null
            },
            onDismiss = { pendingCategoryDeleteId = null },
        )
    }

    accounts.firstOrNull { it.id == pendingAccountDeleteId }?.let { account ->
        DeleteAccountDialog(
            account = account,
            currencyCode = budgetConfig.currencyCode,
            replacementCandidates = accounts.filter { it.id != account.id },
            selectedReplacementId = deleteAccountReplacementId,
            onReplacementSelected = { replacementId -> deleteAccountReplacementId = replacementId },
            onConfirm = {
                onDeleteAccount(account.id, deleteAccountReplacementId)
                pendingAccountDeleteId = null
            },
            onDismiss = { pendingAccountDeleteId = null },
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.budgetOverviewSection(
    budgetConfig: BudgetConfigModel,
    dashboard: DashboardUiState,
    monthLabel: String,
    activeCategories: List<CategoryModel>,
    activeAccounts: List<AccountModel>,
    globalInput: String,
    onGlobalInputChange: (String) -> Unit,
    currencyInput: String,
    onCurrencyInputChange: (String) -> Unit,
    monthStartInput: String,
    onMonthStartInputChange: (String) -> Unit,
    onUpdateGlobal: () -> Unit,
    onUpdateCurrencyCode: () -> Unit,
    onUpdateMonthStartDay: () -> Unit,
) {
    item {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGETS_GLOBAL_TITLE), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = globalInput,
                    onValueChange = onGlobalInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("${localized(AppTextKey.BUDGETS_GLOBAL_MONTHLY_LABEL)} (${budgetConfig.currencyCode})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = currencyInput,
                        onValueChange = onCurrencyInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text(localized(AppTextKey.BUDGETS_CURRENCY_LABEL)) },
                        singleLine = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onUpdateCurrencyCode) {
                        Text(localized(AppTextKey.BUDGETS_CURRENCY_APPLY))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = monthStartInput,
                        onValueChange = onMonthStartInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text(localized(AppTextKey.BUDGETS_MONTH_START_LABEL)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onUpdateMonthStartDay) {
                        Text(localized(AppTextKey.COMMON_APPLY))
                    }
                }
                Button(onClick = onUpdateGlobal) { Text(localized(AppTextKey.BUDGETS_UPDATE_GLOBAL)) }
            }
        }
    }

    item {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGETS_VISUALIZATION_TITLE), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val monthBudgetMinor = dashboard.monthBudgetMinor
                val monthSpentMinor = dashboard.monthTotalMinor
                val monthProgress = if (monthBudgetMinor > 0L) {
                    (monthSpentMinor.toFloat() / monthBudgetMinor.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                LocalMetricBarRow(
                    label = "${localized(AppTextKey.BUDGETS_EXPENSES)} $monthLabel",
                    valueLabel = if (monthBudgetMinor > 0L) {
                        "${monthSpentMinor.minorToMoneyString(budgetConfig.currencyCode)} / ${monthBudgetMinor.minorToMoneyString(budgetConfig.currencyCode)}"
                    } else {
                        "${monthSpentMinor.minorToMoneyString(budgetConfig.currencyCode)} (${localized(AppTextKey.BUDGETS_BUDGET_UNSET)})"
                    },
                    progress = monthProgress,
                    highlightError = dashboard.budgetUsagePercent >= 100,
                )

                if (dashboard.byCategory.isNotEmpty()) {
                    Text(localized(AppTextKey.BUDGETS_TOP_CATEGORIES), fontWeight = FontWeight.SemiBold)
                    val maxCategorySpend = dashboard.byCategory.maxOf { it.totalMinor }.coerceAtLeast(1L)
                    dashboard.byCategory.forEach { categorySpend ->
                        val categoryBudget = activeCategories
                            .firstOrNull { category -> category.name == categorySpend.categoryName }
                            ?.monthlyBudgetMinor ?: 0L
                        val valueLabel = if (categoryBudget > 0L) {
                            "${categorySpend.totalMinor.minorToMoneyString(budgetConfig.currencyCode)} / ${categoryBudget.minorToMoneyString(budgetConfig.currencyCode)}"
                        } else {
                            categorySpend.totalMinor.minorToMoneyString(budgetConfig.currencyCode)
                        }
                        LocalMetricBarRow(
                            label = categorySpend.categoryName,
                            valueLabel = valueLabel,
                            progress = (categorySpend.totalMinor.toFloat() / maxCategorySpend.toFloat()).coerceIn(0f, 1f),
                            highlightError = categoryBudget > 0L && categorySpend.totalMinor > categoryBudget,
                        )
                    }
                }

                if (activeAccounts.isNotEmpty()) {
                    Text(localized(AppTextKey.BUDGETS_ACCOUNT_BALANCES), fontWeight = FontWeight.SemiBold)
                    val maxBalanceAbs = activeAccounts.maxOf { abs(it.balanceMinor) }.coerceAtLeast(1L)
                    activeAccounts.forEach { account ->
                        LocalMetricBarRow(
                            label = account.name,
                            valueLabel = account.balanceMinor.minorToMoneyString(budgetConfig.currencyCode),
                            progress = (abs(account.balanceMinor).toFloat() / maxBalanceAbs.toFloat()).coerceIn(0f, 1f),
                            highlightError = account.balanceMinor < 0L,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.categoriesSection(
    budgetConfig: BudgetConfigModel,
    orderedCategories: List<CategoryModel>,
    filteredBudgetCategories: List<CategoryModel>,
    filteredManageCategories: List<CategoryModel>,
    selectedCategoryPane: CategoryPane,
    onSelectCategoryPane: (CategoryPane) -> Unit,
    selectedManageFilter: CategoryManageFilter,
    onSelectManageFilter: (CategoryManageFilter) -> Unit,
    manageActiveCount: Int,
    manageInactiveCount: Int,
    lastCategoriesOpenMs: Long?,
    lastCategoriesOpenCount: Int,
    lastCategoriesOpenPane: CategoryPane,
    budgetCategoriesToDisplay: List<CategoryModel>,
    manageCategoriesToDisplay: List<CategoryModel>,
    hasMoreBudgetCategories: Boolean,
    hasMoreManageCategories: Boolean,
    onLoadMoreBudgetCategories: () -> Unit,
    onLoadMoreManageCategories: () -> Unit,
    editingBudgetCategoryId: Long?,
    editingBudgetInput: String,
    onEditingBudgetInputChange: (String) -> Unit,
    onStartBudgetEdit: (CategoryModel) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onApplyBudgetEdit: (Long) -> Unit,
    categorySearchInput: String,
    onCategorySearchInputChange: (String) -> Unit,
    newCategoryInput: String,
    onNewCategoryInputChange: (String) -> Unit,
    onCreateCategory: () -> Unit,
    expandedCategoryEditorId: Long?,
    onExpandedCategoryEditorChange: (Long?) -> Unit,
    onUpdateCategory: (Long, String) -> Unit,
    onRenameCategory: (Long, String) -> Unit,
    onSetCategoryActive: (Long, Boolean) -> Unit,
    onUpdateCategoryColor: (Long, String) -> Unit,
    onUpdateCategoryIcon: (Long, String) -> Unit,
    onMoveCategoryUp: (Long) -> Unit,
    onMoveCategoryDown: (Long) -> Unit,
    onRequestDeleteCategory: (Long) -> Unit,
) {
    item {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGET_SECTION_CATEGORIES), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    localized(AppTextKey.BUDGETS_CATEGORIES_OPTIMIZED_HELP),
                    style = MaterialTheme.typography.bodySmall,
                )
                lastCategoriesOpenMs?.let { elapsed ->
                    Text(
                        "${localized(AppTextKey.BUDGETS_OPENING_MEASURE)}: ${elapsed} ms (${lastCategoriesOpenCount} ${localized(AppTextKey.BUDGETS_CATEGORIES_COUNT)}, ${localized(AppTextKey.BUDGETS_VIEW)} ${localized(lastCategoriesOpenPane.labelKey).lowercase(Locale.ROOT)}).",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryPane.entries.forEach { pane ->
                        FilterChip(
                            selected = selectedCategoryPane == pane,
                            onClick = { onSelectCategoryPane(pane) },
                            label = { Text(localized(pane.labelKey)) },
                        )
                    }
                }
                OutlinedTextField(
                    value = categorySearchInput,
                    onValueChange = onCategorySearchInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.BUDGETS_SEARCH_CATEGORY_LABEL)) },
                    singleLine = true,
                )
                if (selectedCategoryPane == CategoryPane.MANAGE) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CategoryManageFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                CategoryManageFilter.ALL -> orderedCategories.size
                                CategoryManageFilter.ACTIVE -> manageActiveCount
                                CategoryManageFilter.INACTIVE -> manageInactiveCount
                            }
                            FilterChip(
                                selected = selectedManageFilter == filter,
                                onClick = { onSelectManageFilter(filter) },
                                label = { Text("${localized(filter.labelKey)} ($count)") },
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCategoryInput,
                            onValueChange = onNewCategoryInputChange,
                            modifier = Modifier.weight(1f),
                            label = { Text(localized(AppTextKey.BUDGETS_NEW_CATEGORY_LABEL)) },
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = onCreateCategory) {
                            Text(localized(AppTextKey.BUDGETS_ADD_CATEGORY))
                        }
                    }
                }
            }
        }
    }

    if (selectedCategoryPane == CategoryPane.BUDGETS) {
        if (filteredBudgetCategories.isEmpty()) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(localized(AppTextKey.BUDGETS_NO_ACTIVE_CATEGORY_TITLE), fontWeight = FontWeight.SemiBold)
                        Text(localized(AppTextKey.BUDGETS_NO_ACTIVE_CATEGORY_HELP))
                    }
                }
            }
        }
        items(
            items = budgetCategoriesToDisplay,
            key = { it.id },
            contentType = { "budgetCategory" },
        ) { category ->
            val isBudgetEditing = editingBudgetCategoryId == category.id
            SoltraSectionCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LocalCategoryIconToken(iconName = category.iconName, colorHex = category.colorHex)
                            Column {
                                Text(category.name, fontWeight = FontWeight.Medium)
                                Text(
                                    if (category.isActive) localized(AppTextKey.BUDGETS_STATUS_ACTIVE) else localized(AppTextKey.BUDGETS_STATUS_INACTIVE),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Text(
                            category.monthlyBudgetMinor.minorToMoneyString(budgetConfig.currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (isBudgetEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editingBudgetInput,
                                onValueChange = onEditingBudgetInputChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("${localized(AppTextKey.BUDGETS_BUDGET_LABEL)} (${budgetConfig.currencyCode})") },
                                singleLine = true,
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onApplyBudgetEdit(category.id) }) {
                                Text(localized(AppTextKey.COMMON_APPLY))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onCancelBudgetEdit) {
                                Text(localized(AppTextKey.COMMON_CANCEL))
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = { onStartBudgetEdit(category) }) {
                                Text(localized(AppTextKey.BUDGETS_EDIT_BUDGET))
                            }
                        }
                    }
                }
            }
        }
        if (hasMoreBudgetCategories) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OutlinedButton(onClick = onLoadMoreBudgetCategories) {
                        Text("${localized(AppTextKey.BUDGETS_SHOW_MORE)} (${filteredBudgetCategories.size - budgetCategoriesToDisplay.size} ${localized(AppTextKey.BUDGETS_REMAINING)})")
                    }
                }
            }
        }
    }

    if (selectedCategoryPane == CategoryPane.MANAGE) {
        if (filteredManageCategories.isEmpty()) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(localized(AppTextKey.BUDGETS_NO_MATCHING_CATEGORY_TITLE), fontWeight = FontWeight.SemiBold)
                        Text(localized(AppTextKey.BUDGETS_NO_MATCHING_CATEGORY_HELP))
                    }
                }
            }
        }
        items(
            items = manageCategoriesToDisplay,
            key = { it.id },
            contentType = { "manageCategory" },
        ) { category ->
            var renameInput by rememberSaveable(category.id, category.name) { mutableStateOf(category.name) }
            var colorInput by rememberSaveable(category.id, category.colorHex) { mutableStateOf(category.colorHex) }
            var iconInput by rememberSaveable(category.id, category.iconName) { mutableStateOf(category.iconName) }
            val expanded = expandedCategoryEditorId == category.id
            SoltraOutlineCard(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LocalCategoryIconToken(iconName = iconInput, colorHex = colorInput)
                            Column {
                                Text(category.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${if (category.isActive) localized(AppTextKey.BUDGETS_STATUS_ACTIVE) else localized(AppTextKey.BUDGETS_STATUS_INACTIVE)} - ${localized(AppTextKey.BUDGET_SECTION_BUDGET)} ${category.monthlyBudgetMinor.minorToMoneyString(budgetConfig.currencyCode)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (category.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Switch(
                            checked = category.isActive,
                            onCheckedChange = { enabled -> onSetCategoryActive(category.id, enabled) },
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onMoveCategoryUp(category.id) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = localized(AppTextKey.BUDGETS_MOVE_UP))
                        }
                        IconButton(onClick = { onMoveCategoryDown(category.id) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = localized(AppTextKey.BUDGETS_MOVE_DOWN))
                        }
                        OutlinedButton(onClick = { onExpandedCategoryEditorChange(if (expanded) null else category.id) }) {
                            Text(if (expanded) localized(AppTextKey.BUDGETS_HIDE) else localized(AppTextKey.BUDGETS_EDIT))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onRequestDeleteCategory(category.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = localized(AppTextKey.BUDGETS_DELETE_CATEGORY_CONTENT_DESC))
                        }
                    }
                    AnimatedVisibility(expanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = renameInput,
                                onValueChange = { renameInput = it.take(30) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(localized(AppTextKey.BUDGETS_CATEGORY_NAME_LABEL)) },
                                singleLine = true,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { onRenameCategory(category.id, renameInput) }) {
                                    Text(localized(AppTextKey.BUDGETS_RENAME))
                                }
                            }
                            Text(localized(AppTextKey.BUDGETS_COLOR), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                localCategoryColorOptions.forEach { color ->
                                    FilterChip(
                                        selected = colorInput == color,
                                        onClick = {
                                            colorInput = color
                                            onUpdateCategoryColor(category.id, color)
                                        },
                                        label = { Text(color) },
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = colorInput,
                                onValueChange = { colorInput = it.uppercase(Locale.ROOT).take(7) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(localized(AppTextKey.BUDGETS_CUSTOM_COLOR_LABEL)) },
                                singleLine = true,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { onUpdateCategoryColor(category.id, colorInput) }) {
                                    Text(localized(AppTextKey.BUDGETS_APPLY_COLOR))
                                }
                            }
                            Text(localized(AppTextKey.BUDGETS_ICON), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                localCategoryIconOptions.forEach { option ->
                                    FilterChip(
                                        selected = iconInput == option.key,
                                        onClick = {
                                            iconInput = option.key
                                            onUpdateCategoryIcon(category.id, option.key)
                                        },
                                        label = { Text(localized(option.labelKey)) },
                                        leadingIcon = {
                                            Icon(option.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (hasMoreManageCategories) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OutlinedButton(onClick = onLoadMoreManageCategories) {
                        Text("${localized(AppTextKey.BUDGETS_SHOW_MORE)} (${filteredManageCategories.size - manageCategoriesToDisplay.size} ${localized(AppTextKey.BUDGETS_REMAINING)})")
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.accountsSection(
    budgetConfig: BudgetConfigModel,
    accounts: List<AccountModel>,
    accountTrends: Map<Long, AccountTrendUiState>,
    newAccountName: String,
    onNewAccountNameChange: (String) -> Unit,
    newAccountBalanceInput: String,
    onNewAccountBalanceInputChange: (String) -> Unit,
    newAccountType: AccountType,
    onNewAccountTypeChange: (AccountType) -> Unit,
    onCreateAccount: () -> Unit,
    onRenameAccount: (Long, String) -> Unit,
    onUpdateAccountTypeAndBalance: (Long, AccountType, String) -> Unit,
    onSetAccountActive: (Long, Boolean) -> Unit,
    onRequestDeleteAccount: (Long) -> Unit,
) {
    item {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGET_SECTION_ACCOUNTS), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${localized(AppTextKey.BUDGETS_TRACKING_HELP_PREFIX)} ${accountTrends.values.firstOrNull()?.windowDays ?: 30} ${localized(AppTextKey.BUDGETS_DAYS_SUFFIX)}.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    localized(AppTextKey.BUDGETS_TYPES_HELP),
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = newAccountName,
                    onValueChange = onNewAccountNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.BUDGETS_ACCOUNT_NAME_LABEL)) },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newAccountBalanceInput,
                        onValueChange = onNewAccountBalanceInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("${localized(AppTextKey.BUDGETS_INITIAL_BALANCE_LABEL)} (${budgetConfig.currencyCode})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Spacer(Modifier.width(8.dp))
                    LocalAccountTypeDropdown(selected = newAccountType, onSelect = onNewAccountTypeChange)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onCreateAccount) {
                        Text(localized(AppTextKey.BUDGETS_ADD_ACCOUNT))
                    }
                }
            }
        }
    }

    if (accounts.isEmpty()) {
        item {
            SoltraOutlineCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(localized(AppTextKey.BUDGETS_NO_ACCOUNT_TITLE), fontWeight = FontWeight.SemiBold)
                    Text(localized(AppTextKey.BUDGETS_NO_ACCOUNT_HELP))
                }
            }
        }
    }

    items(accounts, key = { it.id }) { account ->
        var renameInput by rememberSaveable(account.id, account.name) { mutableStateOf(account.name) }
        var balanceInput by rememberSaveable(account.id, account.balanceMinor) {
            mutableStateOf(account.balanceMinor.minorToInputString())
        }
        var typeInput by remember(account.id, account.type) { mutableStateOf(account.type) }
        val trend = accountTrends[account.id]

        SoltraOutlineCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(account.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${localized(account.type.labelKey())} - ${account.balanceMinor.minorToMoneyString(budgetConfig.currencyCode)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = account.isActive,
                        onCheckedChange = { enabled -> onSetAccountActive(account.id, enabled) },
                    )
                }

                if (trend?.hasTransferHistory == true) {
                    Text("${localized(AppTextKey.BUDGETS_TRACKING_TITLE)} ${trend.windowDays} ${localized(AppTextKey.BUDGETS_DAYS_SUFFIX)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(localized(AppTextKey.BUDGETS_ESTIMATED_START_BALANCE))
                        Text(
                            trend.estimatedStartBalanceMinor.minorToMoneyString(budgetConfig.currencyCode),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(localized(AppTextKey.BUDGETS_CURRENT_PERIOD_VARIATION))
                        Text(
                            signedMoneyString(trend.currentPeriodNetMinor, budgetConfig.currencyCode),
                            color = signedAmountColor(trend.currentPeriodNetMinor),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(localized(AppTextKey.BUDGETS_VS_PREVIOUS_PERIOD))
                        Text(
                            signedMoneyString(trend.deltaVsPreviousPeriodMinor, budgetConfig.currencyCode),
                            color = signedAmountColor(trend.deltaVsPreviousPeriodMinor),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Text(
                        localized(AppTextKey.BUDGETS_TRACKING_UNAVAILABLE),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it.take(30) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.BUDGETS_ACCOUNT_NAME_LABEL)) },
                    singleLine = true,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { onRenameAccount(account.id, renameInput) }) {
                        Text(localized(AppTextKey.BUDGETS_RENAME))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("${localized(AppTextKey.BUDGETS_CURRENT_BALANCE_LABEL)} (${budgetConfig.currencyCode})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Spacer(Modifier.width(8.dp))
                    LocalAccountTypeDropdown(selected = typeInput, onSelect = { typeInput = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { onUpdateAccountTypeAndBalance(account.id, typeInput, balanceInput) }) {
                        Text(localized(AppTextKey.BUDGETS_APPLY_TYPE_AND_BALANCE))
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { onRequestDeleteAccount(account.id) }) {
                        Text(localized(AppTextKey.COMMON_DELETE))
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.transfersSection(
    budgetConfig: BudgetConfigModel,
    activeAccounts: List<AccountModel>,
    recentTransfers: List<AccountTransferModel>,
    transferFromAccountId: Long?,
    onTransferFromAccountChange: (Long?) -> Unit,
    transferToAccountId: Long?,
    onTransferToAccountChange: (Long?) -> Unit,
    transferAmountInput: String,
    onTransferAmountInputChange: (String) -> Unit,
    transferNoteInput: String,
    onTransferNoteInputChange: (String) -> Unit,
    onTransferBetweenAccounts: () -> Unit,
) {
    item {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGETS_TRANSFERS_BETWEEN_ACCOUNTS), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (activeAccounts.size < 2) {
                    Text(localized(AppTextKey.BUDGETS_CREATE_TWO_ACCOUNTS_HELP))
                } else {
                    LocalAccountDropdown(
                        label = localized(AppTextKey.BUDGETS_FROM),
                        accounts = activeAccounts,
                        selectedAccountId = transferFromAccountId,
                        onAccountSelect = onTransferFromAccountChange,
                    )
                    LocalAccountDropdown(
                        label = localized(AppTextKey.BUDGETS_TO),
                        accounts = activeAccounts,
                        selectedAccountId = transferToAccountId,
                        onAccountSelect = onTransferToAccountChange,
                    )
                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = onTransferAmountInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("${localized(AppTextKey.SETTINGS_CSV_AMOUNT)} (${budgetConfig.currencyCode})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = transferNoteInput,
                        onValueChange = onTransferNoteInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                        singleLine = true,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = onTransferBetweenAccounts,
                            enabled = transferFromAccountId != null &&
                                transferToAccountId != null &&
                                transferFromAccountId != transferToAccountId &&
                                transferAmountInput.isNotBlank(),
                        ) {
                            Text(localized(AppTextKey.BUDGETS_TRANSFER))
                        }
                    }
                }
                if (recentTransfers.isNotEmpty()) {
                    HorizontalDivider()
                    Text(localized(AppTextKey.BUDGETS_RECENT_TRANSFERS), fontWeight = FontWeight.SemiBold)
                    recentTransfers.take(10).forEach { transfer ->
                        SoltraOutlineCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${transfer.fromAccountName} -> ${transfer.toAccountName}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${transfer.amountMinor.minorToMoneyString(budgetConfig.currencyCode)} - ${transfer.occurredAtEpochMillis.formatDateTime()}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (!transfer.note.isNullOrBlank()) {
                                    Text("${localized(AppTextKey.BUDGETS_NOTE_PREFIX)}: ${transfer.note}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalCategoryIconToken(
    iconName: String,
    colorHex: String,
) {
    val color = rememberLocalCategoryColor(colorHex)
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color.copy(alpha = 0.16f), shape = MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = localCategoryIconVector(iconName),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun LocalMetricBarRow(
    label: String,
    valueLabel: String,
    progress: Float,
    highlightError: Boolean = false,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val barColor = if (highlightError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(valueLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun LocalConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized(AppTextKey.COMMON_CANCEL))
            }
        },
    )
}

@Composable
private fun LocalAccountDropdown(
    label: String,
    accounts: List<AccountModel>,
    selectedAccountId: Long?,
    onAccountSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: localized(AppTextKey.DROPDOWN_CHOOSE_ACCOUNT)
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedLabel")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelect(account.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LocalAccountTypeDropdown(
    selected: AccountType,
    onSelect: (AccountType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(localized(selected.labelKey()))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AccountType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(localized(type.labelKey())) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DeleteCategoryDialog(
    category: CategoryModel,
    replacementCandidates: List<CategoryModel>,
    selectedReplacementId: Long?,
    onReplacementSelected: (Long?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = replacementCandidates.firstOrNull { it.id == selectedReplacementId }?.name ?: localized(AppTextKey.DROPDOWN_NONE)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${localized(AppTextKey.COMMON_DELETE)} ${category.name} ?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized(AppTextKey.BUDGETS_DELETE_CATEGORY_HELP))
                if (replacementCandidates.isEmpty()) {
                    Text(localized(AppTextKey.BUDGETS_NO_REPLACEMENT_CATEGORY), style = MaterialTheme.typography.bodySmall)
                } else {
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("${localized(AppTextKey.BUDGETS_REPLACEMENT)}: $selectedLabel")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(localized(AppTextKey.DROPDOWN_NONE)) },
                                onClick = {
                                    onReplacementSelected(null)
                                    expanded = false
                                },
                            )
                            replacementCandidates.forEach { candidate ->
                                DropdownMenuItem(
                                    text = { Text(candidate.name) },
                                    onClick = {
                                        onReplacementSelected(candidate.id)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localized(AppTextKey.COMMON_DELETE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized(AppTextKey.COMMON_CANCEL))
            }
        },
    )
}

@Composable
private fun DeleteAccountDialog(
    account: AccountModel,
    currencyCode: String,
    replacementCandidates: List<AccountModel>,
    selectedReplacementId: Long?,
    onReplacementSelected: (Long?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = replacementCandidates.firstOrNull { it.id == selectedReplacementId }?.name ?: localized(AppTextKey.DROPDOWN_NONE)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localized(AppTextKey.BUDGETS_DELETE_ACCOUNT_TITLE)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${account.name} - ${account.balanceMinor.minorToMoneyString(currencyCode)}")
                Text(localized(AppTextKey.BUDGETS_DELETE_ACCOUNT_HELP))
                if (replacementCandidates.isEmpty()) {
                    Text(localized(AppTextKey.BUDGETS_NO_REPLACEMENT_ACCOUNT), style = MaterialTheme.typography.bodySmall)
                } else {
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("${localized(AppTextKey.BUDGETS_REPLACEMENT)}: $selectedLabel")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(localized(AppTextKey.DROPDOWN_NONE)) },
                                onClick = {
                                    onReplacementSelected(null)
                                    expanded = false
                                },
                            )
                            replacementCandidates.forEach { candidate ->
                                DropdownMenuItem(
                                    text = { Text(candidate.name) },
                                    onClick = {
                                        onReplacementSelected(candidate.id)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localized(AppTextKey.COMMON_DELETE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized(AppTextKey.COMMON_CANCEL))
            }
        },
    )
}

private fun localCategoryIconVector(iconName: String): ImageVector {
    return localCategoryIconOptions.firstOrNull { it.key == iconName }?.icon ?: Icons.AutoMirrored.Filled.Label
}

@Composable
private fun rememberLocalCategoryColor(colorHex: String): Color {
    val fallback = MaterialTheme.colorScheme.primary
    return remember(colorHex) {
        runCatching { Color(AndroidColor.parseColor(colorHex)) }
            .getOrElse { fallback }
    }
}

private fun signedMoneyString(amountMinor: Long, currency: String): String {
    val absoluteValue = abs(amountMinor).minorToMoneyString(currency)
    return when {
        amountMinor > 0L -> "+$absoluteValue"
        amountMinor < 0L -> "-$absoluteValue"
        else -> 0L.minorToMoneyString(currency)
    }
}

@Composable
private fun signedAmountColor(amountMinor: Long): Color {
    return when {
        amountMinor > 0L -> MaterialTheme.colorScheme.primary
        amountMinor < 0L -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}



