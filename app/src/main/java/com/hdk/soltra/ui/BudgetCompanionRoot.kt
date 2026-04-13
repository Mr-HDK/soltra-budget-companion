package com.hdk.soltra.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hdk.soltra.R
import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import com.hdk.soltra.domain.AppLockMode
import com.hdk.soltra.domain.AppThemeMode
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphGrouping
import com.hdk.soltra.domain.GraphPeriod
import com.hdk.soltra.domain.GraphType
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.QuickTemplateModel
import com.hdk.soltra.domain.RecurrenceFrequency
import com.hdk.soltra.domain.RecurringRuleModel
import com.hdk.soltra.i18n.AppLanguagePreference
import com.hdk.soltra.i18n.AppTextKey
import com.hdk.soltra.i18n.localized
import com.hdk.soltra.ui.components.SoltraMetricBlock
import com.hdk.soltra.ui.components.SoltraOutlineCard
import com.hdk.soltra.ui.components.SoltraScreenBackground
import com.hdk.soltra.ui.components.SoltraSectionCard
import com.hdk.soltra.ui.components.SoltraSectionHeader
import com.hdk.soltra.ui.components.SoltraTintedCard
import com.hdk.soltra.ui.theme.soltra
import com.hdk.soltra.util.formatDate
import com.hdk.soltra.util.formatDateTime
import com.hdk.soltra.util.minorToMoneyString
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

private enum class RootTab(
    val labelKey: AppTextKey,
    val icon: @Composable () -> Unit,
) {
    DASHBOARD(AppTextKey.ROOT_TAB_DASHBOARD, { Icon(Icons.Default.Home, contentDescription = null) }),
    GRAPHS(AppTextKey.ROOT_TAB_GRAPHS, { Icon(Icons.Default.PieChart, contentDescription = null) }),
    EXPENSES(AppTextKey.ROOT_TAB_EXPENSES, { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) }),
    CHECKPOINTS(AppTextKey.ROOT_TAB_CHECKPOINTS, { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) }),
    BUDGETS(AppTextKey.ROOT_TAB_BUDGETS, { Icon(Icons.Default.Savings, contentDescription = null) }),
    TEMPLATES(AppTextKey.ROOT_TAB_TEMPLATES, { Icon(Icons.Default.AutoAwesome, contentDescription = null) }),
    SETTINGS(AppTextKey.ROOT_TAB_SETTINGS, { Icon(Icons.Default.Settings, contentDescription = null) }),
}

private enum class BudgetsWorkspace(val labelKey: AppTextKey) {
    BUDGETS(AppTextKey.ROOT_TAB_BUDGETS),
    TEMPLATES(AppTextKey.ROOT_TAB_TEMPLATES),
}

private enum class QuickAddDatePreset {
    TODAY,
    YESTERDAY,
}

private enum class BudgetsSection(val labelKey: AppTextKey) {
    BUDGET(AppTextKey.BUDGET_SECTION_BUDGET),
    CATEGORIES(AppTextKey.BUDGET_SECTION_CATEGORIES),
    ACCOUNTS(AppTextKey.BUDGET_SECTION_ACCOUNTS),
    TRANSFERS(AppTextKey.BUDGET_SECTION_TRANSFERS),
}

private data class CategoryIconOption(
    val key: String,
    val labelKey: AppTextKey,
    val icon: ImageVector,
)

private val categoryColorOptions = listOf(
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

private val categoryIconOptions = listOf(
    CategoryIconOption("utensils", AppTextKey.CATEGORY_ICON_MEALS, Icons.Default.Restaurant),
    CategoryIconOption("coffee", AppTextKey.CATEGORY_ICON_COFFEE, Icons.Default.LocalCafe),
    CategoryIconOption("transport", AppTextKey.CATEGORY_ICON_TRANSPORT, Icons.Default.DirectionsBus),
    CategoryIconOption("party", AppTextKey.CATEGORY_ICON_PARTY, Icons.Default.Celebration),
    CategoryIconOption("shopping", AppTextKey.CATEGORY_ICON_SHOPPING, Icons.Default.ShoppingCart),
    CategoryIconOption("health", AppTextKey.CATEGORY_ICON_HEALTH, Icons.Default.Favorite),
    CategoryIconOption("home", AppTextKey.CATEGORY_ICON_HOME, Icons.Default.Home),
    CategoryIconOption("subscription", AppTextKey.CATEGORY_ICON_SUBSCRIPTIONS, Icons.Default.Subscriptions),
    CategoryIconOption("gift", AppTextKey.CATEGORY_ICON_GIFTS, Icons.Default.CardGiftcard),
    CategoryIconOption("document", AppTextKey.CATEGORY_ICON_ADMIN, Icons.Default.Description),
    CategoryIconOption("tag", AppTextKey.CATEGORY_ICON_MISC, Icons.AutoMirrored.Filled.Label),
)

private fun categoryIconVector(iconName: String): ImageVector {
    return categoryIconOptions.firstOrNull { it.key == iconName }?.icon ?: Icons.AutoMirrored.Filled.Label
}

private fun parseColorOrFallback(colorHex: String, fallback: Color): Color {
    return runCatching { Color(AndroidColor.parseColor(colorHex)) }.getOrDefault(fallback)
}

private fun PaymentMethod.labelKey(): AppTextKey {
    return when (this) {
        PaymentMethod.LIQUIDE -> AppTextKey.PAYMENT_METHOD_CASH
        PaymentMethod.CARTE_TPE -> AppTextKey.PAYMENT_METHOD_CARD_TPE
        PaymentMethod.VIREMENT -> AppTextKey.PAYMENT_METHOD_BANK_TRANSFER
    }
}

private fun AccountType.labelKey(): AppTextKey {
    return when (this) {
        AccountType.CASH -> AppTextKey.ACCOUNT_TYPE_CASH
        AccountType.BANK -> AppTextKey.ACCOUNT_TYPE_BANK
        AccountType.CARD -> AppTextKey.ACCOUNT_TYPE_CARD
        AccountType.EWALLET -> AppTextKey.ACCOUNT_TYPE_EWALLET
        AccountType.OTHER -> AppTextKey.ACCOUNT_TYPE_OTHER
    }
}

private fun RecurrenceFrequency.labelKey(): AppTextKey {
    return when (this) {
        RecurrenceFrequency.DAILY -> AppTextKey.RECURRENCE_DAILY
        RecurrenceFrequency.WEEKLY -> AppTextKey.RECURRENCE_WEEKLY
        RecurrenceFrequency.MONTHLY -> AppTextKey.RECURRENCE_MONTHLY
    }
}

private fun AppThemeMode.labelKey(): AppTextKey {
    return when (this) {
        AppThemeMode.SYSTEM -> AppTextKey.APP_THEME_SYSTEM
        AppThemeMode.LIGHT -> AppTextKey.APP_THEME_LIGHT
        AppThemeMode.COLORFUL -> AppTextKey.APP_THEME_COLORFUL
    }
}

private fun AppLockMode.labelKey(): AppTextKey {
    return when (this) {
        AppLockMode.NONE -> AppTextKey.APP_LOCK_NONE
        AppLockMode.PIN -> AppTextKey.APP_LOCK_PIN
    }
}

private fun GraphType.labelKey(): AppTextKey {
    return when (this) {
        GraphType.PIE -> AppTextKey.GRAPH_TYPE_PIE
        GraphType.DONUT -> AppTextKey.GRAPH_TYPE_DONUT
        GraphType.BAR -> AppTextKey.GRAPH_TYPE_BAR
    }
}

private fun GraphPeriod.labelKey(): AppTextKey {
    return when (this) {
        GraphPeriod.CURRENT_MONTH -> AppTextKey.GRAPH_PERIOD_CURRENT_MONTH
        GraphPeriod.PREVIOUS_MONTH -> AppTextKey.GRAPH_PERIOD_PREVIOUS_MONTH
        GraphPeriod.CURRENT_YEAR -> AppTextKey.GRAPH_PERIOD_CURRENT_YEAR
        GraphPeriod.PREVIOUS_YEAR -> AppTextKey.GRAPH_PERIOD_PREVIOUS_YEAR
        GraphPeriod.CUSTOM -> AppTextKey.GRAPH_PERIOD_CUSTOM
    }
}

private fun GraphGrouping.labelKey(): AppTextKey {
    return when (this) {
        GraphGrouping.CATEGORY -> AppTextKey.GRAPH_GROUPING_CATEGORY
        GraphGrouping.PAYMENT_METHOD -> AppTextKey.GRAPH_GROUPING_PAYMENT
        GraphGrouping.MONTH -> AppTextKey.GRAPH_GROUPING_MONTH
    }
}

private fun AppLanguagePreference.labelKey(): AppTextKey {
    return when (this) {
        AppLanguagePreference.SYSTEM -> AppTextKey.LANGUAGE_OPTION_SYSTEM
        AppLanguagePreference.FR -> AppTextKey.LANGUAGE_OPTION_FR
        AppLanguagePreference.EN -> AppTextKey.LANGUAGE_OPTION_EN
    }
}

@Composable
private fun <T> collectStateWithMemoryCache(
    source: StateFlow<T>,
    enabled: Boolean,
): T {
    var cached by remember(source) { mutableStateOf(source.value) }
    return if (enabled) {
        val live by source.collectAsStateWithLifecycle()
        LaunchedEffect(live) {
            cached = live
        }
        live
    } else {
        cached
    }
}

@Composable
private fun graphTypeAccentColor(graphType: GraphType): Color {
    return when (graphType) {
        GraphType.PIE -> Color(0xFF0E6B68)
        GraphType.DONUT -> Color(0xFF132338)
        GraphType.BAR -> Color(0xFFC9823A)
    }
}

@Composable
private fun GraphTypeThumbnail(
    graphType: GraphType,
    modifier: Modifier = Modifier,
) {
    val accent = graphTypeAccentColor(graphType)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    val tertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(7.dp),
            )
            .padding(4.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (graphType) {
                GraphType.PIE -> {
                    val sweeps = listOf(170f, 115f, 75f)
                    val colors = listOf(accent, secondary, tertiary)
                    var start = -90f
                    sweeps.forEachIndexed { i, sweep ->
                        drawArc(
                            color = colors[i],
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = true,
                        )
                        start += sweep
                    }
                }

                GraphType.DONUT -> {
                    val sweeps = listOf(170f, 115f, 75f)
                    val colors = listOf(accent, secondary, tertiary)
                    var start = -90f
                    val ring = size.minDimension * 0.22f
                    sweeps.forEachIndexed { i, sweep ->
                        drawArc(
                            color = colors[i],
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = ring, cap = StrokeCap.Butt),
                        )
                        start += sweep
                    }
                }

                GraphType.BAR -> {
                    val barWidth = size.width / 6.5f
                    val baseY = size.height
                    val heights = listOf(0.58f, 0.88f, 0.7f)
                    heights.forEachIndexed { i, ratio ->
                        val left = barWidth * (i * 1.8f)
                        val top = size.height * (1f - ratio)
                        drawRect(
                            color = if (i % 2 == 0) accent else secondary,
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(barWidth, baseY - top),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphTypePickerCard(
    graphType: GraphType,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val container = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    OutlinedCard(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.outlinedCardColors(containerColor = container),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GraphTypeThumbnail(
                graphType = graphType,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun rememberCategoryColor(colorHex: String): Color {
    val fallback = MaterialTheme.colorScheme.primary
    return remember(colorHex, fallback) {
        parseColorOrFallback(colorHex, fallback)
    }
}

@Composable
private fun CategoryIconToken(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
) {
    val categoryColor = rememberCategoryColor(colorHex)
    Box(
        modifier = modifier
            .size(28.dp)
            .background(categoryColor.copy(alpha = 0.16f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIconVector(iconName),
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun CategoryLabelChip(
    name: String,
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
) {
    val categoryColor = rememberCategoryColor(colorHex)
    val containerColor by animateColorAsState(
        targetValue = categoryColor.copy(alpha = 0.14f),
        label = "categoryChipContainer",
    )
    Row(
        modifier = modifier
            .background(containerColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CategoryIconToken(iconName = iconName, colorHex = colorHex)
        Text(
            text = name,
            fontWeight = FontWeight.SemiBold,
            color = categoryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetsWorkspaceScreen(
    selectedWorkspace: BudgetsWorkspace,
    onWorkspaceChange: (BudgetsWorkspace) -> Unit,
    budgetsContent: @Composable () -> Unit,
    templatesContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    localized(AppTextKey.BUDGETS_SECTIONS_TITLE),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BudgetsWorkspace.entries.forEach { workspace ->
                        FilterChip(
                            selected = selectedWorkspace == workspace,
                            onClick = { onWorkspaceChange(workspace) },
                            label = { Text(localized(workspace.labelKey)) },
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Crossfade(targetState = selectedWorkspace, label = "budgetsWorkspaceContent") { workspace ->
                when (workspace) {
                    BudgetsWorkspace.BUDGETS -> budgetsContent()
                    BudgetsWorkspace.TEMPLATES -> templatesContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCompanionRoot(
    viewModel: MainViewModel,
) {
    var selectedTab by rememberSaveable { mutableStateOf(RootTab.DASHBOARD) }
    var selectedBudgetsWorkspace by rememberSaveable { mutableStateOf(BudgetsWorkspace.BUDGETS) }
    val isBudgetsArea = selectedTab == RootTab.BUDGETS || selectedTab == RootTab.TEMPLATES
    val needsBudgetData = selectedTab == RootTab.DASHBOARD || isBudgetsArea
    val needsBudgetDetails = isBudgetsArea
    val needsGraphData = selectedTab == RootTab.GRAPHS
    val needsExpensesData = selectedTab == RootTab.EXPENSES
    val needsCheckpointsData = selectedTab == RootTab.CHECKPOINTS
    val needsTemplatesData = isBudgetsArea || selectedTab == RootTab.EXPENSES
    val needsSettingsCategories = selectedTab == RootTab.SETTINGS
    val topBarLabelKey = when {
        selectedTab == RootTab.BUDGETS && selectedBudgetsWorkspace == BudgetsWorkspace.TEMPLATES -> AppTextKey.ROOT_TAB_TEMPLATES
        else -> selectedTab.labelKey
    }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allCategories = collectStateWithMemoryCache(
        source = viewModel.allCategories,
        enabled = needsBudgetDetails || needsSettingsCategories,
    )
    val accounts = collectStateWithMemoryCache(
        source = viewModel.accounts,
        enabled = needsBudgetDetails,
    )
    val recentTransfers = collectStateWithMemoryCache(
        source = viewModel.recentTransfers,
        enabled = needsBudgetDetails,
    )
    val accountTrends = collectStateWithMemoryCache(
        source = viewModel.accountTrends,
        enabled = needsBudgetDetails,
    )
    val expenses = collectStateWithMemoryCache(
        source = viewModel.expenses,
        enabled = needsExpensesData,
    )
    val checkpointHistory = collectStateWithMemoryCache(
        source = viewModel.checkpointHistory,
        enabled = needsCheckpointsData,
    )
    val budgetConfig by viewModel.budgetConfig.collectAsStateWithLifecycle()
    val dashboard = collectStateWithMemoryCache(
        source = viewModel.dashboardUi,
        enabled = needsBudgetData,
    )
    val monthPicker = collectStateWithMemoryCache(
        source = viewModel.monthPickerUi,
        enabled = needsBudgetData,
    )
    val graphEditor = collectStateWithMemoryCache(
        source = viewModel.graphEditor,
        enabled = needsGraphData,
    )
    val graphWidgets = collectStateWithMemoryCache(
        source = viewModel.graphWidgets,
        enabled = needsGraphData,
    )
    val addExpense by viewModel.addExpenseUi.collectAsStateWithLifecycle()
    val addCheckpoint by viewModel.addCheckpointUi.collectAsStateWithLifecycle()
    val templates = collectStateWithMemoryCache(
        source = viewModel.templates,
        enabled = needsTemplatesData,
    )
    val recurringRules = collectStateWithMemoryCache(
        source = viewModel.recurringRules,
        enabled = needsTemplatesData,
    )
    val templateEditor = collectStateWithMemoryCache(
        source = viewModel.templateEditorUi,
        enabled = needsTemplatesData,
    )
    val recurringEditor = collectStateWithMemoryCache(
        source = viewModel.recurringEditorUi,
        enabled = needsTemplatesData,
    )
    val csvImportUi = collectStateWithMemoryCache(
        source = viewModel.csvImportUi,
        enabled = selectedTab == RootTab.SETTINGS,
    )
    val expenseFilter by viewModel.expenseFilter.collectAsStateWithLifecycle()
    val exportFolderUri by viewModel.exportFolderUri.collectAsStateWithLifecycle()
    val backupFileUri by viewModel.backupFileUri.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val backupIntervalHours by viewModel.backupIntervalHours.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val noExpenseReminderEnabled by viewModel.noExpenseReminderEnabled.collectAsStateWithLifecycle()
    val checkpointReminderEnabled by viewModel.checkpointReminderEnabled.collectAsStateWithLifecycle()
    val noExpenseReminderDays by viewModel.noExpenseReminderDays.collectAsStateWithLifecycle()
    val appLockMode by viewModel.appLockMode.collectAsStateWithLifecycle()
    val appLockPin by viewModel.appLockPin.collectAsStateWithLifecycle()
    val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
    val appLanguagePreference by viewModel.appLanguagePreference.collectAsStateWithLifecycle()
    val quickWidgetDefaultCategoryId by viewModel.quickWidgetDefaultCategoryId.collectAsStateWithLifecycle()
    val quickWidgetDefaultPaymentMethod by viewModel.quickWidgetDefaultPaymentMethod.collectAsStateWithLifecycle()
    val quickAddOpenRequest by viewModel.quickAddOpenRequest.collectAsStateWithLifecycle()
    val banner by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var quickAmountInput by rememberSaveable { mutableStateOf("") }
    var quickNoteInput by rememberSaveable { mutableStateOf("") }
    var quickCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var quickPaymentMethod by rememberSaveable { mutableStateOf(PaymentMethod.CARTE_TPE) }
    var quickDatePreset by rememberSaveable { mutableStateOf(QuickAddDatePreset.TODAY) }
    var quickAddFocusRequestId by rememberSaveable { mutableStateOf(0) }
    var appUnlocked by rememberSaveable(appLockMode) { mutableStateOf(appLockMode == AppLockMode.NONE) }
    var appLockError by remember { mutableStateOf<String?>(null) }
    val enforcedLockMode = if (appLockMode == AppLockMode.PIN && appLockPin.isNullOrBlank()) {
        AppLockMode.NONE
    } else {
        appLockMode
    }

    val treePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Ignore; providers may grant without persistable flag in callback.
            }
            viewModel.setExportFolderUri(uri.toString())
        }
    }

    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            pendingRestoreUri = uri
        }
    }

    val backupTargetPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Ignore; providers may grant without persistable flag in callback.
            }
            viewModel.setBackupFileUri(uri.toString())
        }
    }

    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            viewModel.previewCsvImport(uri)
        }
    }

    LaunchedEffect(banner) {
        if (!banner.isNullOrBlank()) {
            snackbarHostState.showSnackbar(banner ?: "")
            viewModel.consumeBanner()
        }
    }

    LaunchedEffect(enforcedLockMode) {
        appUnlocked = enforcedLockMode == AppLockMode.NONE
        appLockError = null
    }

    LaunchedEffect(quickAddOpenRequest?.requestId) {
        val request = quickAddOpenRequest ?: return@LaunchedEffect
        run {
            quickCategoryId = addExpense.categoryId ?: categories.firstOrNull()?.id
            quickPaymentMethod = addExpense.paymentMethod
            showQuickAdd = true
            if (request.focusAmount) {
                quickAddFocusRequestId += 1
            }
            viewModel.consumeQuickAddOpenRequest(request.requestId)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == RootTab.TEMPLATES) {
            selectedBudgetsWorkspace = BudgetsWorkspace.TEMPLATES
            selectedTab = RootTab.BUDGETS
        }
    }

    DisposableEffect(lifecycleOwner, enforcedLockMode) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && enforcedLockMode != AppLockMode.NONE) {
                appUnlocked = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!appUnlocked && enforcedLockMode != AppLockMode.NONE) {
        AppLockGate(
            mode = enforcedLockMode,
            expectedPin = appLockPin,
            errorMessage = appLockError,
            onPinUnlock = { input ->
                if (input == appLockPin) {
                    appUnlocked = true
                    appLockError = null
                } else {
                    appLockError = "Code incorrect"
                }
            },
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
                            tonalElevation = 0.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.ic_launcher_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = context.getString(R.string.app_name),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Crossfade(targetState = topBarLabelKey, label = "topBarTabLabel") { labelKey ->
                                Text(
                                    text = localized(labelKey),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != RootTab.SETTINGS) {
                FloatingActionButton(
                    onClick = {
                        quickCategoryId = quickCategoryId ?: addExpense.categoryId ?: categories.firstOrNull()?.id
                        quickPaymentMethod = addExpense.paymentMethod
                        showQuickAdd = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = localized(AppTextKey.UI_QUICK_ADD))
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.offset(y = 3.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            ) {
                RootTab.entries
                    .filterNot { it == RootTab.TEMPLATES }
                    .forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = tab.icon,
                        label = { Text(localized(tab.labelKey)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        SoltraScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Crossfade(targetState = selectedTab, label = "rootTabContent") { tab ->
                when (tab) {
                    RootTab.DASHBOARD -> DashboardScreen(
                        dashboard = dashboard,
                        monthPicker = monthPicker,
                        currency = budgetConfig.currencyCode,
                        onGoToAdd = { selectedTab = RootTab.EXPENSES },
                        onGoToCheckpoints = { selectedTab = RootTab.CHECKPOINTS },
                        onGoToBudgets = { selectedTab = RootTab.BUDGETS },
                        onPreviousMonth = { viewModel.moveDashboardMonth(-1) },
                        onNextMonth = { viewModel.moveDashboardMonth(1) },
                        onResetMonth = viewModel::resetDashboardMonthToCurrent,
                    )

                    RootTab.GRAPHS -> GraphsScreen(
                        graphEditor = graphEditor,
                        graphWidgets = graphWidgets,
                        currency = budgetConfig.currencyCode,
                        onUpdateGraphEditorConfig = viewModel::updateGraphEditorConfig,
                        onStartEditGraphWidget = viewModel::startEditGraphWidget,
                        onCancelGraphWidgetEditing = viewModel::cancelGraphWidgetEditing,
                        onAddGraphWidget = viewModel::addGraphWidgetFromEditor,
                        onSaveGraphWidget = viewModel::saveGraphWidgetEdit,
                        onDeleteGraphWidget = viewModel::deleteGraphWidget,
                        onMoveGraphWidget = viewModel::moveGraphWidget,
                    )

                    RootTab.EXPENSES -> ExpensesScreen(
                        addState = addExpense,
                        categories = categories,
                        templates = templates,
                        filterState = expenseFilter,
                        expenses = expenses,
                        currency = budgetConfig.currencyCode,
                        onUpdateAdd = viewModel::updateAddExpense,
                        onSaveExpense = viewModel::saveExpense,
                        onApplyTemplate = viewModel::applyTemplate,
                        onStartEditExpense = viewModel::startEditExpense,
                        onCancelEditExpense = viewModel::cancelEditExpense,
                        onDeleteExpense = viewModel::deleteExpense,
                        onSearchChange = viewModel::setSearch,
                        onFilterCategory = viewModel::setFilterCategory,
                        onFilterPayment = viewModel::setFilterPayment,
                    )

                    RootTab.CHECKPOINTS -> CheckpointsScreen(
                        state = addCheckpoint,
                        checkpointItems = checkpointHistory,
                        currency = budgetConfig.currencyCode,
                        onUpdate = viewModel::updateAddCheckpoint,
                        onSave = viewModel::saveCheckpoint,
                        onStartEdit = viewModel::startEditCheckpoint,
                        onCancelEdit = viewModel::cancelEditCheckpoint,
                        onDelete = viewModel::deleteCheckpoint,
                    )

                    RootTab.BUDGETS, RootTab.TEMPLATES -> BudgetsWorkspaceScreen(
                        selectedWorkspace = if (tab == RootTab.TEMPLATES) BudgetsWorkspace.TEMPLATES else selectedBudgetsWorkspace,
                        onWorkspaceChange = { selectedBudgetsWorkspace = it },
                        budgetsContent = {
                            BudgetsScreen(
                                budgetConfig = budgetConfig,
                                dashboard = dashboard,
                                monthLabel = monthPicker.label,
                                categories = allCategories,
                                accounts = accounts,
                                accountTrends = accountTrends,
                                recentTransfers = recentTransfers,
                                onUpdateGlobal = viewModel::updateGlobalBudget,
                                onUpdateCategory = viewModel::updateCategoryBudget,
                                onUpdateMonthStartDay = viewModel::updateMonthStartDay,
                                onUpdateCurrencyCode = viewModel::updateCurrencyCode,
                                onCreateCategory = viewModel::createCategory,
                                onRenameCategory = viewModel::renameCategory,
                                onSetCategoryActive = viewModel::setCategoryActive,
                                onUpdateCategoryColor = viewModel::updateCategoryColor,
                                onUpdateCategoryIcon = viewModel::updateCategoryIcon,
                                onMoveCategoryUp = viewModel::moveCategoryUp,
                                onMoveCategoryDown = viewModel::moveCategoryDown,
                                onDeleteCategory = viewModel::deleteCategory,
                                onCreateAccount = viewModel::createAccount,
                                onRenameAccount = viewModel::renameAccount,
                                onUpdateAccountTypeAndBalance = viewModel::updateAccountTypeAndBalance,
                                onSetAccountActive = viewModel::setAccountActive,
                                onDeleteAccount = viewModel::deleteAccount,
                                onTransferBetweenAccounts = viewModel::transferBetweenAccounts,
                            )
                        },
                        templatesContent = {
                            TemplatesScreen(
                                categories = categories,
                                templates = templates,
                                templateEditor = templateEditor,
                                recurringRules = recurringRules,
                                recurringEditor = recurringEditor,
                                onUpdateTemplateEditor = viewModel::updateTemplateEditor,
                                onSaveTemplate = viewModel::saveTemplate,
                                onEditTemplate = viewModel::editTemplate,
                                onDeleteTemplate = viewModel::deleteTemplate,
                                onResetTemplate = viewModel::resetTemplateEditor,
                                onUpdateRecurringEditor = viewModel::updateRecurringEditor,
                                onSaveRecurring = viewModel::saveRecurringRule,
                                onEditRecurring = viewModel::editRecurringRule,
                                onDeleteRecurring = viewModel::deleteRecurringRule,
                                onResetRecurring = viewModel::resetRecurringEditor,
                                onRunRecurringNow = viewModel::runRecurringNow,
                            )
                        },
                    )

                    RootTab.SETTINGS -> SettingsScreen(
                        exportFolderUri = exportFolderUri,
                        backupFileUri = backupFileUri,
                        autoBackupEnabled = autoBackupEnabled,
                        backupIntervalHours = backupIntervalHours,
                        onPickFolder = { treePicker.launch(null) },
                        onPickBackupFile = { backupTargetPicker.launch("budget-companion-backup.json") },
                        onExportNow = viewModel::exportNow,
                        onPickRestoreFile = { backupPicker.launch(arrayOf("application/json", "text/plain")) },
                        onPickCsvFile = { csvPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                        csvImportUi = csvImportUi,
                        onUpdateCsvMapping = viewModel::updateCsvMapping,
                        onImportCsv = viewModel::importCsvTransactions,
                        onClearCsvState = viewModel::clearCsvImportState,
                        onToggleAutoBackup = viewModel::setAutoBackup,
                        onChangeBackupInterval = viewModel::setBackupIntervalHours,
                        remindersEnabled = remindersEnabled,
                        noExpenseReminderEnabled = noExpenseReminderEnabled,
                        checkpointReminderEnabled = checkpointReminderEnabled,
                        noExpenseReminderDays = noExpenseReminderDays,
                        onToggleReminders = viewModel::setRemindersEnabled,
                        onToggleNoExpenseReminder = viewModel::setNoExpenseReminderEnabled,
                        onToggleCheckpointReminder = viewModel::setCheckpointReminderEnabled,
                        onChangeNoExpenseReminderDays = viewModel::setNoExpenseReminderDays,
                        appLockMode = appLockMode,
                        onSetAppLockMode = viewModel::setAppLockMode,
                        onSetAppLockPin = viewModel::setAppLockPin,
                        appThemeMode = appThemeMode,
                        onSetAppThemeMode = viewModel::setAppThemeMode,
                        appLanguagePreference = appLanguagePreference,
                        onSetAppLanguagePreference = viewModel::setAppLanguagePreference,
                        widgetCategories = allCategories.filter { it.isActive },
                        widgetDefaultCategoryId = quickWidgetDefaultCategoryId,
                        onSetWidgetDefaultCategory = viewModel::setQuickWidgetDefaultCategoryId,
                        widgetDefaultPaymentMethod = quickWidgetDefaultPaymentMethod,
                        onSetWidgetDefaultPaymentMethod = viewModel::setQuickWidgetDefaultPaymentMethod,
                    )
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddExpenseSheet(
            currency = budgetConfig.currencyCode,
            categories = categories,
            amountInput = quickAmountInput,
            selectedCategoryId = quickCategoryId,
            selectedPaymentMethod = quickPaymentMethod,
            datePreset = quickDatePreset,
            noteInput = quickNoteInput,
            onAmountInputChange = { quickAmountInput = it },
            onCategorySelect = { categoryId -> quickCategoryId = categoryId },
            onPaymentSelect = { payment -> quickPaymentMethod = payment },
            onDatePresetSelect = { preset -> quickDatePreset = preset },
            onNoteInputChange = { quickNoteInput = it },
            focusRequestId = quickAddFocusRequestId,
            onSave = {
                val shouldClose = viewModel.quickAddExpense(
                    amountInput = quickAmountInput,
                    categoryId = quickCategoryId,
                    paymentMethod = quickPaymentMethod,
                    occurredAtEpochMillis = quickDatePreset.toEpochMillis(),
                    note = quickNoteInput,
                )
                if (shouldClose) {
                    showQuickAdd = false
                    quickAmountInput = ""
                    quickNoteInput = ""
                    quickDatePreset = QuickAddDatePreset.TODAY
                }
            },
            onDismiss = {
                showQuickAdd = false
                quickAmountInput = ""
                quickNoteInput = ""
                quickDatePreset = QuickAddDatePreset.TODAY
                quickCategoryId = addExpense.categoryId ?: categories.firstOrNull()?.id
            },
        )
    }

    pendingRestoreUri?.let { restoreUri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(localized(AppTextKey.DIALOG_CONFIRM_RESTORE_TITLE)) },
            text = {
                Text(localized(AppTextKey.DIALOG_CONFIRM_RESTORE_MESSAGE))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreFromBackup(restoreUri)
                        pendingRestoreUri = null
                    },
                ) {
                    Text(localized(AppTextKey.DIALOG_CONFIRM_RESTORE_ACTION))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(localized(AppTextKey.COMMON_CANCEL))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickAddExpenseSheet(
    currency: String,
    categories: List<CategoryModel>,
    amountInput: String,
    selectedCategoryId: Long?,
    selectedPaymentMethod: PaymentMethod,
    datePreset: QuickAddDatePreset,
    noteInput: String,
    onAmountInputChange: (String) -> Unit,
    onCategorySelect: (Long) -> Unit,
    onPaymentSelect: (PaymentMethod) -> Unit,
    onDatePresetSelect: (QuickAddDatePreset) -> Unit,
    onNoteInputChange: (String) -> Unit,
    focusRequestId: Int,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val amountFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(categories, selectedCategoryId) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            onCategorySelect(categories.first().id)
        }
    }

    LaunchedEffect(focusRequestId) {
        if (focusRequestId > 0) {
            repeat(4) { attempt ->
                if (attempt > 0) delay(120)
                amountFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(localized(AppTextKey.UI_QUICK_ADD), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester),
                label = { Text("${localized(AppTextKey.SETTINGS_CSV_AMOUNT)} ($currency)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("10", "20", "50", "100").forEach { preset ->
                    AssistChip(
                        onClick = { onAmountInputChange(preset) },
                        label = { Text("$preset $currency") },
                    )
                }
            }

            Text(localized(AppTextKey.SETTINGS_CSV_CATEGORY), fontWeight = FontWeight.SemiBold)
            if (categories.isEmpty()) {
                Text(localized(AppTextKey.UI_NO_CATEGORY_AVAILABLE))
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { onCategorySelect(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }

            Text(localized(AppTextKey.UI_PAYMENT), fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { onPaymentSelect(method) },
                        label = { Text(localized(method.labelKey())) },
                    )
                }
            }

            Text(localized(AppTextKey.SETTINGS_CSV_DATE), fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = datePreset == QuickAddDatePreset.TODAY,
                    onClick = { onDatePresetSelect(QuickAddDatePreset.TODAY) },
                    label = { Text(localized(AppTextKey.UI_TODAY)) },
                )
                FilterChip(
                    selected = datePreset == QuickAddDatePreset.YESTERDAY,
                    onClick = { onDatePresetSelect(QuickAddDatePreset.YESTERDAY) },
                    label = { Text(localized(AppTextKey.UI_YESTERDAY)) },
                )
            }

            OutlinedTextField(
                value = noteInput,
                onValueChange = onNoteInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                maxLines = 2,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = categories.isNotEmpty() && selectedCategoryId != null && amountInput.isNotBlank(),
                ) {
                    Text(localized(AppTextKey.COMMON_SAVE))
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(localized(AppTextKey.COMMON_CANCEL))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DashboardScreen(
    dashboard: DashboardUiState,
    monthPicker: MonthPickerUiState,
    currency: String,
    onGoToAdd: () -> Unit,
    onGoToCheckpoints: () -> Unit,
    onGoToBudgets: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val homeTintColor = MaterialTheme.colorScheme.secondaryContainer
    val homeSectionStrength = if (isDarkTheme) 0.22f else 0.14f
    val homeHeroTintAlpha = if (isDarkTheme) 0.3f else 0.18f
    val homeSummaryTintAlpha = if (isDarkTheme) 0.26f else 0.16f
    val homeCardBorderAlpha = if (isDarkTheme) 0.3f else 0.68f
    val lightHeroContainer = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.background,
        0.34f,
    )
    val lightSummaryContainer = androidx.compose.ui.graphics.lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.background,
        0.24f,
    )

    val monthBudget = dashboard.monthBudgetMinor
    val monthSpent = dashboard.monthTotalMinor
    val budgetProgress = when {
        monthBudget <= 0L -> 0f
        else -> (monthSpent.toDouble() / monthBudget.toDouble()).toFloat().coerceIn(0f, 1f)
    }
    val usagePercent = dashboard.budgetUsagePercent

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(18.dp),
    ) {
        item {
            SoltraTintedCard(
                modifier = Modifier.fillMaxWidth(),
                containerAlpha = homeHeroTintAlpha,
                tintColor = homeTintColor,
                containerColor = if (isDarkTheme) null else lightHeroContainer,
                borderColor = MaterialTheme.colorScheme.outline,
                borderAlpha = homeCardBorderAlpha,
                elevation = if (isDarkTheme) 2.dp else 3.dp,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SoltraSectionHeader(title = localized(AppTextKey.UI_ANALYZED_PERIOD))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onPreviousMonth) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localized(AppTextKey.GRAPH_PERIOD_PREVIOUS_MONTH))
                        }
                        Text(monthPicker.label, fontWeight = FontWeight.Medium)
                        IconButton(onClick = onNextMonth, enabled = !monthPicker.isCurrentMonth) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = localized(AppTextKey.UI_NEXT_MONTH))
                        }
                    }
                    if (!monthPicker.isCurrentMonth) {
                        TextButton(onClick = onResetMonth) { Text(localized(AppTextKey.UI_BACK_TO_CURRENT_MONTH)) }
                    }
                }
            }
        }
        item {
            SoltraTintedCard(
                modifier = Modifier.fillMaxWidth(),
                containerAlpha = homeSummaryTintAlpha,
                tintColor = homeTintColor,
                containerColor = if (isDarkTheme) null else lightSummaryContainer,
                borderColor = MaterialTheme.colorScheme.outline,
                borderAlpha = homeCardBorderAlpha,
                elevation = if (isDarkTheme) 2.dp else 3.dp,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoltraSectionHeader(
                        title = localized(AppTextKey.UI_FINANCIAL_SUMMARY),
                        subtitle = monthPicker.label,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = localized(AppTextKey.UI_TODAY_S_EXPENSES),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = dashboard.todayTotalMinor.minorToMoneyString(currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "${localized(AppTextKey.BUDGETS_EXPENSES)} ${monthPicker.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = monthSpent.minorToMoneyString(currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = localized(AppTextKey.UI_MONTHLY_BUDGET),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = monthBudget.minorToMoneyString(currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "${localized(AppTextKey.UI_REMAINING)} ${monthPicker.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = dashboard.monthRemainingMinor.minorToMoneyString(currency),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (dashboard.monthRemainingMinor < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (monthBudget > 0L) {
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            if (usagePercent <= 100) "Budget utilise: $usagePercent%" else "Depassement budget: ${usagePercent - 100}%",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SoltraMetricBlock(
                            label = localized(AppTextKey.UI_T_7_DAYS),
                            value = dashboard.rolling7DaysTotalMinor.minorToMoneyString(currency),
                            modifier = Modifier.weight(1f),
                        )
                        SoltraMetricBlock(
                            label = localized(AppTextKey.UI_T_30_DAYS),
                            value = dashboard.rolling30DaysTotalMinor.minorToMoneyString(currency),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    val trendPercent = dashboard.trendVsPreviousPercent
                    val trendLabel = when {
                        trendPercent == null -> "Tendance: periode precedente indisponible."
                        trendPercent > 0 -> "Tendance: +$trendPercent% vs periode precedente."
                        trendPercent < 0 -> "Tendance: $trendPercent% vs periode precedente."
                        else -> "Tendance stable vs periode precedente."
                    }
                    Text(trendLabel, style = MaterialTheme.typography.bodySmall)
                    if (trendPercent != null) {
                        val trendMinor = dashboard.trendVsPreviousMinor
                        Text(
                            "Ecart valeur: ${trendMinor.minorToMoneyString(currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                trendMinor > 0L -> MaterialTheme.colorScheme.error
                                trendMinor < 0L -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                    dashboard.projectedPeriodTotalMinor?.let { projected ->
                        Text(
                            "Projection fin periode: ${projected.minorToMoneyString(currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    dashboard.projectedOverBudgetMinor?.let { over ->
                        Text(
                            "Alerte projection: depassement estime ${over.minorToMoneyString(currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "Actions rapides",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onGoToAdd, modifier = Modifier.weight(1f)) { Text(localized(AppTextKey.UI_ADD_EXPENSE)) }
                        OutlinedButton(onClick = onGoToCheckpoints, modifier = Modifier.weight(1f)) { Text(localized(AppTextKey.UI_NEW_CHECKPOINT)) }
                    }
                }
            }
        }
        if (monthBudget <= 0L || (monthBudget > 0L && usagePercent >= 80) || dashboard.projectedOverBudgetMinor != null || dashboard.latestCheckpoint == null) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (monthBudget <= 0L) {
                            Text(
                                "Aucun budget global defini.",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(localized(AppTextKey.UI_SET_A_MONTHLY_BUDGET_TO_ENABLE_ALERTS_AND_PROJEC))
                        }
                        if (monthBudget > 0L && usagePercent >= 80) {
                            val severityLabel = when {
                                usagePercent >= 100 -> "Budget depasse"
                                usagePercent >= 90 -> "Alerte budget"
                                else -> "Attention budget"
                            }
                            val detail = when {
                                usagePercent >= 100 -> "Tu as depasse ${usagePercent - 100}% de ton budget mensuel."
                                else -> "Tu as deja utilise $usagePercent% de ton budget mensuel."
                            }
                            Text(severityLabel, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            Text(detail)
                        }
                        dashboard.projectedOverBudgetMinor?.let { over ->
                            Text(
                                "Au rythme actuel, depassement estime: ${over.minorToMoneyString(currency)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        if (dashboard.latestCheckpoint == null) {
                            Text(localized(AppTextKey.UI_ADD_A_FIRST_CHECKPOINT_TO_TRACK_ACTUAL_VS_EXPECT))
                        }
                        OutlinedButton(onClick = onGoToBudgets) {
                            Text(localized(AppTextKey.UI_ADJUST_BUDGETS))
                        }
                    }
                }
            }
        }
        item {
            SoltraSectionCard(
                modifier = Modifier.fillMaxWidth(),
                tintColor = homeTintColor,
                tintStrength = homeSectionStrength,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoltraSectionHeader(title = localized(AppTextKey.UI_CASH_PROJECTION_30_DAYS))
                    val projection = dashboard.futureProjection
                    if (projection == null) {
                        Text(localized(AppTextKey.UI_PROJECTION_UNAVAILABLE))
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(localized(AppTextKey.UI_STARTING_BALANCE))
                            Text(projection.startBalanceMinor.minorToMoneyString(currency), fontWeight = FontWeight.Medium)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(localized(AppTextKey.UI_PROJECTED_BALANCE))
                            Text(
                                projection.endBalanceMinor.minorToMoneyString(currency),
                                fontWeight = FontWeight.SemiBold,
                                color = if (projection.endBalanceMinor < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (projection.events.isEmpty()) {
                            Text("${localized(AppTextKey.UI_NO_RECURRING_EXPENSE_PLANNED_FOR)} ${projection.horizonDays} ${localized(AppTextKey.UI_DAYS)}")
                        } else {
                            Text(localized(AppTextKey.UI_UPCOMING_OUTFLOWS), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            projection.events.take(8).forEach { event ->
                                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column {
                                            val eventLabel = if (event.label.isBlank()) {
                                                localized(AppTextKey.RECURRING_EVENT_DEFAULT_LABEL)
                                            } else {
                                                event.label
                                            }
                                            Text(eventLabel, fontWeight = FontWeight.Medium)
                                            Text(event.dateEpochMillis.formatDate(), style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("-${event.amountMinor.minorToMoneyString(currency)}", color = MaterialTheme.colorScheme.error)
                                            Text(
                                                event.projectedBalanceAfterMinor.minorToMoneyString(currency),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SoltraSectionCard(
                modifier = Modifier.fillMaxWidth(),
                tintColor = homeTintColor,
                tintStrength = homeSectionStrength,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoltraSectionHeader(title = localized(AppTextKey.UI_LATEST_CHECKPOINT))
                    val cp = dashboard.latestCheckpoint
                    if (cp == null) {
                        Text(localized(AppTextKey.CHECKPOINT_NO_RECORD))
                    } else {
                        Text("${localized(AppTextKey.SETTINGS_CSV_DATE)}: ${cp.recordedAtEpochMillis.formatDateTime()}")
                        Text("${localized(AppTextKey.UI_BANK)}: ${cp.bankBalanceMinor.minorToMoneyString(currency)}")
                        Text("${localized(AppTextKey.UI_CASH)}: ${cp.cashBalanceMinor.minorToMoneyString(currency)}")
                        if (!cp.note.isNullOrBlank()) Text("${localized(AppTextKey.BUDGETS_NOTE_PREFIX)}: ${cp.note}")
                    }
                }
            }
        }
        item {
            SoltraSectionCard(
                modifier = Modifier.fillMaxWidth(),
                tintColor = homeTintColor,
                tintStrength = homeSectionStrength,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoltraSectionHeader(title = localized(AppTextKey.UI_CHECKPOINT_AUDIT))
                    val audit = dashboard.latestCheckpointAudit
                    if (audit?.uncontrolledMinor == null) {
                        Text(localized(AppTextKey.UI_ADD_AT_LEAST_2_CHECKPOINTS_TO_MEASURE_UNCONTROLL))
                    } else {
                        val periodStart = audit.previousCheckpointEpochMillis?.formatDate() ?: "?"
                        val periodEnd = audit.currentCheckpointEpochMillis.formatDate()
                        val delta = audit.uncontrolledMinor
                        Text("${localized(AppTextKey.UI_PERIOD)}: $periodStart -> $periodEnd")
                        Text("${localized(AppTextKey.UI_EXPENSES_BETWEEN_CHECKPOINTS)}: ${(audit.expensesBetweenMinor ?: 0L).minorToMoneyString(currency)}")
                        Text("${localized(AppTextKey.UI_EXPECTED_BALANCE_AT_CHECKPOINT)}: ${(audit.expectedCurrentTotalMinor ?: 0L).minorToMoneyString(currency)}")
                        Text("${localized(AppTextKey.UI_ACTUAL_BALANCE_AT_CHECKPOINT)}: ${audit.actualCurrentTotalMinor.minorToMoneyString(currency)}")
                        val nonControleLabel = when {
                            delta > 0L -> "Non controle (sortie non tracee): ${abs(delta).minorToMoneyString(currency)}"
                            delta < 0L -> "Non controle (entree non tracee): ${abs(delta).minorToMoneyString(currency)}"
                            else -> "Non controle: ${0L.minorToMoneyString(currency)}"
                        }
                        Text(
                            nonControleLabel,
                            color = when {
                                delta > 0L -> MaterialTheme.colorScheme.error
                                delta < 0L -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    dashboard.spentSinceLatestCheckpointMinor?.let { spent ->
                        Text("${localized(AppTextKey.UI_EXPENSES_SINCE_LATEST_CHECKPOINT)}: ${spent.minorToMoneyString(currency)}")
                    }
                }
            }
        }
        item {
            SoltraSectionCard(
                modifier = Modifier.fillMaxWidth(),
                tintColor = homeTintColor,
                tintStrength = homeSectionStrength,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoltraSectionHeader(title = "${localized(AppTextKey.UI_TOP_CATEGORIES)} (${monthPicker.label})")
                    if (dashboard.byCategory.isEmpty()) {
                        Text("${localized(AppTextKey.UI_NO_EXPENSES_YET_FOR)} ${monthPicker.label}")
                    } else {
                        dashboard.byCategory.forEach {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(it.categoryName)
                                Text(it.totalMinor.minorToMoneyString(currency), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GraphsScreen(
    graphEditor: GraphEditorUiState,
    graphWidgets: List<GraphWidgetUiState>,
    currency: String,
    onUpdateGraphEditorConfig: ((GraphConfigModel) -> GraphConfigModel) -> Unit,
    onStartEditGraphWidget: (Long) -> Unit,
    onCancelGraphWidgetEditing: () -> Unit,
    onAddGraphWidget: () -> Unit,
    onSaveGraphWidget: () -> Unit,
    onDeleteGraphWidget: (Long) -> Unit,
    onMoveGraphWidget: (Long, Int) -> Unit,
) {
    val config = graphEditor.config
    val editingWidgetId = graphEditor.editingWidgetId
    val density = LocalDensity.current
    val dragThresholdPx = remember(density) { with(density) { 72.dp.toPx() } }
    var draggingWidgetId by remember { mutableStateOf<Long?>(null) }
    val dragOffsets = remember { mutableStateMapOf<Long, Float>() }

    fun commitGraphMoveFromOffset(widgetId: Long) {
        val finalOffset = dragOffsets[widgetId] ?: 0f
        if (kotlin.math.abs(finalOffset) < dragThresholdPx) return
        val moveCount = (kotlin.math.abs(finalOffset) / dragThresholdPx).toInt().coerceAtLeast(1)
        when {
            finalOffset > 0f -> repeat(moveCount) { onMoveGraphWidget(widgetId, 1) }
            finalOffset < 0f -> repeat(moveCount) { onMoveGraphWidget(widgetId, -1) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = draggingWidgetId == null,
    ) {
        item {
            val editorAccent = graphTypeAccentColor(config.type)
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = editorAccent.copy(alpha = 0.08f)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (editingWidgetId == null) "Nouveau graphe" else "Modifier graphe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = config.title,
                        onValueChange = { title ->
                            onUpdateGraphEditorConfig { current -> current.copy(title = title.take(40)) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localized(AppTextKey.UI_TITLE)) },
                        singleLine = true,
                    )
                    Text(localized(AppTextKey.UI_TYPE), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GraphType.entries.forEach { type ->
                            GraphTypePickerCard(
                                graphType = type,
                                label = localized(type.labelKey()),
                                selected = config.type == type,
                                onClick = { onUpdateGraphEditorConfig { current -> current.copy(type = type) } },
                            )
                        }
                    }
                    Text(
                        text = "Type selectionne: ${localized(config.type.labelKey())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(localized(AppTextKey.UI_PERIOD_2), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GraphPeriod.entries.forEach { period ->
                            FilterChip(
                                selected = config.period == period,
                                onClick = {
                                    onUpdateGraphEditorConfig { current ->
                                        if (period == GraphPeriod.CUSTOM) {
                                            val now = System.currentTimeMillis()
                                            current.copy(
                                                period = period,
                                                customFromEpochMillis = current.customFromEpochMillis ?: (now - 30L * 24L * 60L * 60L * 1000L),
                                                customToEpochMillis = current.customToEpochMillis ?: now,
                                            )
                                        } else {
                                            current.copy(period = period)
                                        }
                                    }
                                },
                                label = { Text(localized(period.labelKey())) },
                            )
                        }
                    }
                    if (config.period == GraphPeriod.CUSTOM) {
                        DateTimeField(
                            label = "Date debut",
                            epochMillis = config.customFromEpochMillis ?: System.currentTimeMillis(),
                            onValueChange = { epoch ->
                                onUpdateGraphEditorConfig { current -> current.copy(customFromEpochMillis = epoch) }
                            },
                        )
                        DateTimeField(
                            label = "Date fin",
                            epochMillis = config.customToEpochMillis ?: System.currentTimeMillis(),
                            onValueChange = { epoch ->
                                onUpdateGraphEditorConfig { current -> current.copy(customToEpochMillis = epoch) }
                            },
                        )
                    }
                    Text(localized(AppTextKey.UI_GROUPING), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GraphGrouping.entries.forEach { grouping ->
                            FilterChip(
                                selected = config.grouping == grouping,
                                onClick = { onUpdateGraphEditorConfig { current -> current.copy(grouping = grouping) } },
                                label = { Text(localized(grouping.labelKey())) },
                            )
                        }
                    }
                    Text(
                        "Appui long sur une carte graphe puis glisse vers le haut ou le bas pour la deplacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (editingWidgetId == null) {
                            Button(onClick = onAddGraphWidget, modifier = Modifier.weight(1f)) {
                                Text("${localized(AppTextKey.UI_ADD_CHART)} (${localized(config.type.labelKey())})")
                            }
                        } else {
                            Button(onClick = onSaveGraphWidget, modifier = Modifier.weight(1f)) { Text(localized(AppTextKey.UI_SAVE_CHANGES)) }
                            OutlinedButton(onClick = onCancelGraphWidgetEditing, modifier = Modifier.weight(1f)) { Text(localized(AppTextKey.COMMON_CANCEL)) }
                        }
                    }
                }
            }
        }

        if (graphWidgets.isEmpty()) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(localized(AppTextKey.UI_NO_CHART_CONFIGURED), fontWeight = FontWeight.SemiBold)
                        Text(localized(AppTextKey.UI_CONFIGURE_YOUR_CHART_ABOVE_THEN_ADD_IT_TO_THE_DA))
                    }
                }
            }
        } else {
            item {
                Text(
                    "Mes graphes (${graphWidgets.size}) - appui long + glisser pour reordonner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(graphWidgets, key = { it.id }) { widget ->
                val rawOffset = dragOffsets[widget.id] ?: 0f
                val isDragging = draggingWidgetId == widget.id
                val animatedOffset by animateFloatAsState(
                    targetValue = if (isDragging) rawOffset else 0f,
                    label = "widgetDragOffset-${widget.id}",
                )
                val widgetAccent = graphTypeAccentColor(widget.preview.chartType)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .offset { IntOffset(0, animatedOffset.roundToInt()) }
                        .graphicsLayer {
                            shadowElevation = if (isDragging) 18f else 0f
                            alpha = if (isDragging) 0.97f else 1f
                        }
                        .pointerInput(widget.id, dragThresholdPx) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggingWidgetId = widget.id },
                                onDragEnd = {
                                    commitGraphMoveFromOffset(widget.id)
                                    draggingWidgetId = null
                                    dragOffsets[widget.id] = 0f
                                },
                                onDragCancel = {
                                    commitGraphMoveFromOffset(widget.id)
                                    draggingWidgetId = null
                                    dragOffsets[widget.id] = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val next = (dragOffsets[widget.id] ?: 0f) + dragAmount.y
                                    when {
                                        next >= dragThresholdPx -> {
                                            onMoveGraphWidget(widget.id, 1)
                                            dragOffsets[widget.id] = next - dragThresholdPx
                                        }
                                        next <= -dragThresholdPx -> {
                                            onMoveGraphWidget(widget.id, -1)
                                            dragOffsets[widget.id] = next + dragThresholdPx
                                        }
                                        else -> {
                                            dragOffsets[widget.id] = next
                                        }
                                    }
                                },
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = widgetAccent.copy(alpha = if (isDragging) 0.16f else 0.1f),
                    ),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    GraphTypeThumbnail(
                                        graphType = widget.preview.chartType,
                                        modifier = Modifier.size(36.dp),
                                    )
                                    Text(widget.preview.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "${widget.preview.periodLabel} - ${widget.preview.groupingLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "Periode: ${widget.preview.fromEpochMillis.formatDate()} -> ${widget.preview.toEpochMillis.formatDate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onStartEditGraphWidget(widget.id) }) { Text(localized(AppTextKey.BUDGETS_EDIT)) }
                                OutlinedButton(onClick = { onDeleteGraphWidget(widget.id) }) { Text(localized(AppTextKey.COMMON_DELETE)) }
                            }
                        }
                        Text(
                            "Total: ${widget.preview.totalMinor.minorToMoneyString(currency)}",
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!widget.preview.hasData) {
                            SoltraOutlineCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(localized(AppTextKey.UI_NO_DATA_FOR_THIS_PERIOD), fontWeight = FontWeight.Medium)
                                    Text(localized(AppTextKey.UI_ADJUST_THE_PERIOD_OR_ADD_EXPENSES_TO_DISPLAY_THE))
                                }
                            }
                        } else {
                            GraphPreviewChart(
                                graphType = widget.preview.chartType,
                                points = widget.preview.points,
                                totalMinor = widget.preview.totalMinor,
                                currency = currency,
                            )
                            GraphLegend(points = widget.preview.points, totalMinor = widget.preview.totalMinor, currency = currency)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphPreviewChart(
    graphType: GraphType,
    points: List<GraphPointUiState>,
    totalMinor: Long,
    currency: String,
) {
    when (graphType) {
        GraphType.PIE -> PieOrDonutChart(points = points, totalMinor = totalMinor, currency = currency, donut = false)
        GraphType.DONUT -> PieOrDonutChart(points = points, totalMinor = totalMinor, currency = currency, donut = true)
        GraphType.BAR -> BarGraph(points = points)
    }
}

@Composable
private fun PieOrDonutChart(
    points: List<GraphPointUiState>,
    totalMinor: Long,
    currency: String,
    donut: Boolean,
) {
    val safeTotal = totalMinor.coerceAtLeast(1L)
    val progress by animateFloatAsState(targetValue = 1f, label = "graphPieProgress")
    val fallback = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                points.filter { it.valueMinor > 0L }.forEach { point ->
                    val sweepAngle = ((point.valueMinor.toFloat() / safeTotal.toFloat()) * 360f) * progress
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = parseColorOrFallback(point.colorHex, fallback),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = !donut,
                            style = if (donut) Stroke(width = size.minDimension * 0.24f) else androidx.compose.ui.graphics.drawscope.Fill,
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            if (donut) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(localized(AppTextKey.UI_TOTAL), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalMinor.minorToMoneyString(currency), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BarGraph(points: List<GraphPointUiState>) {
    val nonNegativePoints = points.map { point -> point.copy(valueMinor = point.valueMinor.coerceAtLeast(0L)) }
    val maxValue = nonNegativePoints.maxOfOrNull { it.valueMinor }?.coerceAtLeast(1L) ?: 1L
    val fallback = MaterialTheme.colorScheme.primary
    val progress by animateFloatAsState(targetValue = 1f, label = "graphBarProgress")

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        if (nonNegativePoints.isEmpty()) return@Canvas

        val spacing = 8.dp.toPx()
        val count = nonNegativePoints.size
        val availableWidth = (size.width - spacing * (count + 1)).coerceAtLeast(1f)
        val barWidth = (availableWidth / count.toFloat()).coerceAtLeast(6.dp.toPx())

        nonNegativePoints.forEachIndexed { index, point ->
            val normalized = point.valueMinor.toFloat() / maxValue.toFloat()
            val barHeight = (size.height * 0.9f * normalized * progress).coerceAtLeast(2f)
            val left = spacing + index * (barWidth + spacing)
            val top = size.height - barHeight
            drawRoundRect(
                color = parseColorOrFallback(point.colorHex, fallback),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = CornerRadius(10f, 10f),
            )
        }
    }
}

@Composable
private fun GraphLegend(
    points: List<GraphPointUiState>,
    totalMinor: Long,
    currency: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        points.take(10).forEach { point ->
            val ratio = if (totalMinor > 0L) (point.valueMinor.toDouble() * 100.0 / totalMinor.toDouble()) else 0.0
            val fallback = MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = parseColorOrFallback(point.colorHex, fallback),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        ),
                )
                Text(point.label, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${"%.1f".format(Locale.getDefault(), ratio)}%", style = MaterialTheme.typography.bodySmall)
                Text(point.valueMinor.minorToMoneyString(currency), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpensesScreen(
    addState: AddExpenseUiState,
    categories: List<CategoryModel>,
    templates: List<QuickTemplateModel>,
    filterState: ExpenseFilterUiState,
    expenses: List<com.hdk.soltra.domain.ExpenseRecord>,
    currency: String,
    onUpdateAdd: ((AddExpenseUiState) -> AddExpenseUiState) -> Unit,
    onSaveExpense: () -> Unit,
    onApplyTemplate: (QuickTemplateModel) -> Unit,
    onStartEditExpense: (com.hdk.soltra.domain.ExpenseRecord) -> Unit,
    onCancelEditExpense: () -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterCategory: (Long?) -> Unit,
    onFilterPayment: (PaymentMethod?) -> Unit,
) {
    var pendingDeleteExpense by remember { mutableStateOf<com.hdk.soltra.domain.ExpenseRecord?>(null) }
    var addFormExpanded by rememberSaveable { mutableStateOf(addState.editingExpenseId != null) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val categoriesById = remember(categories) { categories.associateBy { it.id } }

    LaunchedEffect(addState.editingExpenseId) {
        if (addState.editingExpenseId != null) {
            addFormExpanded = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val editing = addState.editingExpenseId != null
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (editing) "Modifier depense" else "Ajout manuel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!editing) {
                            TextButton(onClick = { addFormExpanded = !addFormExpanded }) {
                                Text(if (addFormExpanded) "Masquer" else "Afficher")
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = editing || addFormExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (templates.isNotEmpty()) {
                            Text(localized(AppTextKey.UI_QUICK_TEMPLATES))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                templates.take(3).forEach { template ->
                                    AssistChip(
                                        onClick = { onApplyTemplate(template) },
                                        label = { Text(template.name) },
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = addState.amountInput,
                            onValueChange = { onUpdateAdd { s -> s.copy(amountInput = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("${localized(AppTextKey.SETTINGS_CSV_AMOUNT)} ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        DateTimeField(
                            label = "Date",
                            epochMillis = addState.dateEpochMillis,
                            onValueChange = { epoch -> onUpdateAdd { s -> s.copy(dateEpochMillis = epoch) } },
                        )
                        CategoryDropdown(
                            label = localized(AppTextKey.SETTINGS_CSV_CATEGORY),
                            categories = categories,
                            selectedCategoryId = addState.categoryId,
                            onCategorySelect = { categoryId -> onUpdateAdd { s -> s.copy(categoryId = categoryId) } },
                        )
                        PaymentDropdown(
                            label = localized(AppTextKey.UI_PAYMENT),
                            selected = addState.paymentMethod,
                            onSelect = { payment -> onUpdateAdd { s -> s.copy(paymentMethod = payment) } },
                        )
                        OutlinedTextField(
                            value = addState.merchantOrLabel,
                            onValueChange = { onUpdateAdd { s -> s.copy(merchantOrLabel = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(localized(AppTextKey.UI_MERCHANT_LABEL_OPTIONAL)) },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = addState.note,
                            onValueChange = { onUpdateAdd { s -> s.copy(note = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = onSaveExpense, modifier = Modifier.weight(1f)) {
                                Text(if (editing) "Mettre a jour" else "Enregistrer la depense")
                            }
                            if (editing) {
                                OutlinedButton(
                                    onClick = onCancelEditExpense,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(localized(AppTextKey.COMMON_CANCEL))
                                }
                            }
                        }
                        }
                    }
                    if (!editing && !addFormExpanded) {
                        Text(
                            "Utilise le bouton + pour une saisie ultra rapide, ou ouvre ce formulaire pour une saisie complete.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(localized(AppTextKey.UI_FILTERS), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                            Text(if (filtersExpanded) "Masquer" else "Afficher")
                        }
                    }
                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = filterState.search,
                            onValueChange = onSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(localized(AppTextKey.UI_SEARCH_TEXT)) },
                            singleLine = true,
                        )
                        CategoryDropdown(
                            label = localized(AppTextKey.SETTINGS_CSV_CATEGORY),
                            categories = categories,
                            selectedCategoryId = filterState.categoryId,
                            onCategorySelect = onFilterCategory,
                            withAll = true,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(selected = filterState.paymentMethod == null, onClick = { onFilterPayment(null) }, label = { Text(localized(AppTextKey.UI_ALL)) })
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = filterState.paymentMethod == method,
                                    onClick = { onFilterPayment(method) },
                                    label = { Text(localized(method.labelKey())) },
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = {
                                    onSearchChange("")
                                    onFilterCategory(null)
                                    onFilterPayment(null)
                                },
                            ) {
                                Text(localized(AppTextKey.UI_RESET))
                            }
                        }
                        }
                    }
                    if (!filtersExpanded) {
                        Text(
                            "Filtres compacts actifs: ${if (filterState.search.isBlank()) "aucune recherche" else "recherche"} / " +
                                "${filterState.categoryId?.let { "categorie" } ?: "toutes categories"} / " +
                                "${if (filterState.paymentMethod == null) localized(AppTextKey.PAYMENT_METHOD_ALL) else localized(filterState.paymentMethod.labelKey())}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item { Text(localized(AppTextKey.UI_HISTORY), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        val expensesByDay = expenses.groupBy { expense -> expense.occurredAtEpochMillis.formatDate() }
        if (expensesByDay.isEmpty()) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(localized(AppTextKey.UI_NO_EXPENSE_FOR_THIS_FILTER), fontWeight = FontWeight.SemiBold)
                        Text(localized(AppTextKey.UI_ADJUST_FILTERS_OR_ADD_AN_EXPENSE_TO_GET_STARTED))
                    }
                }
            }
        } else {
            expensesByDay.forEach { (dayLabel, dayExpenses) ->
                val dayTotalMinor = dayExpenses.sumOf { it.amountMinor }
                item(key = "header-$dayLabel") {
                    SoltraOutlineCard(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(dayLabel, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${dayExpenses.size} operations - ${dayTotalMinor.minorToMoneyString(currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                items(dayExpenses, key = { it.id }) { expense ->
                    val category = categoriesById[expense.categoryId]
                    val categoryIconName = category?.iconName ?: "tag"
                    val categoryColorHex = category?.colorHex ?: expense.categoryColorHex
                    val categoryTint = rememberCategoryColor(categoryColorHex)
                    val cardTint by animateColorAsState(
                        targetValue = categoryTint.copy(alpha = 0.1f),
                        label = "expenseCategoryCardTint",
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = cardTint,
                        ),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryLabelChip(
                                    name = expense.categoryName,
                                    iconName = categoryIconName,
                                    colorHex = categoryColorHex,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(expense.amountMinor.minorToMoneyString(currency), fontWeight = FontWeight.Bold)
                            }
                            Text(expense.occurredAtEpochMillis.formatDateTime())
                            if (!expense.merchantOrLabel.isNullOrBlank()) Text(expense.merchantOrLabel)
                            if (!expense.note.isNullOrBlank()) Text("${localized(AppTextKey.BUDGETS_NOTE_PREFIX)}: ${expense.note}")
                            Text(localized(expense.paymentMethod.labelKey()))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { onStartEditExpense(expense) }) {
                                    Icon(Icons.Default.Edit, contentDescription = localized(AppTextKey.UI_EDIT))
                                }
                                IconButton(onClick = { pendingDeleteExpense = expense }) {
                                    Icon(Icons.Default.Delete, contentDescription = localized(AppTextKey.COMMON_DELETE))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteExpense?.let { expense ->
        ConfirmActionDialog(
            title = localized(AppTextKey.UI_DELETE_THIS_EXPENSE),
            message = "${expense.amountMinor.minorToMoneyString(currency)} - ${expense.occurredAtEpochMillis.formatDateTime()}",
            confirmLabel = localized(AppTextKey.COMMON_DELETE),
            onConfirm = {
                onDeleteExpense(expense.id)
                pendingDeleteExpense = null
            },
            onDismiss = { pendingDeleteExpense = null },
        )
    }
}

@Composable
private fun CheckpointsScreen(
    state: AddCheckpointUiState,
    checkpointItems: List<CheckpointHistoryItemUiState>,
    currency: String,
    onUpdate: ((AddCheckpointUiState) -> AddCheckpointUiState) -> Unit,
    onSave: () -> Unit,
    onStartEdit: (com.hdk.soltra.domain.BalanceCheckpointModel) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    var pendingDeleteCheckpoint by remember { mutableStateOf<com.hdk.soltra.domain.BalanceCheckpointModel?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SoltraSectionCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val editing = state.editingCheckpointId != null
                    Text(
                        if (editing) localized(AppTextKey.UI_EDIT_CHECKPOINT) else localized(AppTextKey.UI_NEW_CHECKPOINT),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    DateTimeField(
                        label = localized(AppTextKey.UI_CHECKPOINT_DATE),
                        epochMillis = state.dateEpochMillis,
                        onValueChange = { epoch -> onUpdate { s -> s.copy(dateEpochMillis = epoch) } },
                    )
                    OutlinedTextField(
                        value = state.bankInput,
                        onValueChange = { onUpdate { s -> s.copy(bankInput = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("${localized(AppTextKey.UI_BANK_BALANCE)} ($currency)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = state.cashInput,
                        onValueChange = { onUpdate { s -> s.copy(cashInput = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("${localized(AppTextKey.UI_CASH_IN_HAND)} ($currency)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { onUpdate { s -> s.copy(note = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                            Text(if (editing) "Mettre a jour checkpoint" else "Enregistrer checkpoint")
                        }
                        if (editing) {
                            OutlinedButton(onClick = onCancelEdit, modifier = Modifier.weight(1f)) {
                                Text(localized(AppTextKey.COMMON_CANCEL))
                            }
                        }
                    }
                }
            }
        }

        item { Text(localized(AppTextKey.UI_CHECKPOINT_HISTORY), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(checkpointItems, key = { it.checkpoint.id }) { item ->
            val cp = item.checkpoint
            val audit = item.audit
            SoltraSectionCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(cp.recordedAtEpochMillis.formatDateTime(), fontWeight = FontWeight.Medium)
                    Text("${localized(AppTextKey.UI_BANK)}: ${cp.bankBalanceMinor.minorToMoneyString(currency)}")
                    Text("${localized(AppTextKey.UI_CASH)}: ${cp.cashBalanceMinor.minorToMoneyString(currency)}")
                    if (!cp.note.isNullOrBlank()) Text("${localized(AppTextKey.BUDGETS_NOTE_PREFIX)}: ${cp.note}")
                    if (audit?.uncontrolledMinor == null) {
                        Text(localized(AppTextKey.UI_AUDIT_STARTING_POINT_NO_PREVIOUS_COMPARISON))
                    } else {
                        val delta = audit.uncontrolledMinor
                        val periodStart = audit.previousCheckpointEpochMillis?.formatDate() ?: "?"
                        val periodEnd = audit.currentCheckpointEpochMillis.formatDate()
                        Text("${localized(AppTextKey.UI_PERIOD)}: $periodStart -> $periodEnd")
                        Text("${localized(AppTextKey.UI_EXPENSES_OVER_PERIOD)}: ${(audit.expensesBetweenMinor ?: 0L).minorToMoneyString(currency)}")
                        val label = when {
                            delta > 0L -> "Non controle (sortie non tracee): ${abs(delta).minorToMoneyString(currency)}"
                            delta < 0L -> "Non controle (entree non tracee): ${abs(delta).minorToMoneyString(currency)}"
                            else -> "Non controle: ${0L.minorToMoneyString(currency)}"
                        }
                        Text(
                            label,
                            color = when {
                                delta > 0L -> MaterialTheme.colorScheme.error
                                delta < 0L -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { onStartEdit(cp) }) {
                            Icon(Icons.Default.Edit, contentDescription = localized(AppTextKey.UI_EDIT_CHECKPOINT))
                        }
                        IconButton(onClick = { pendingDeleteCheckpoint = cp }) {
                            Icon(Icons.Default.Delete, contentDescription = localized(AppTextKey.UI_DELETE_CHECKPOINT))
                        }
                    }
                }
            }
        }
        if (checkpointItems.isEmpty()) {
            item {
                SoltraOutlineCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(localized(AppTextKey.CHECKPOINT_NO_RECORD), fontWeight = FontWeight.SemiBold)
                        Text(localized(AppTextKey.UI_ADD_A_CHECKPOINT_TO_COMPARE_ACTUAL_VS_EXPECTED))
                    }
                }
            }
        }
    }

    pendingDeleteCheckpoint?.let { checkpoint ->
        ConfirmActionDialog(
            title = localized(AppTextKey.UI_DELETE_THIS_CHECKPOINT),
            message = checkpoint.recordedAtEpochMillis.formatDateTime(),
            confirmLabel = localized(AppTextKey.COMMON_DELETE),
            onConfirm = {
                onDelete(checkpoint.id)
                pendingDeleteCheckpoint = null
            },
            onDismiss = { pendingDeleteCheckpoint = null },
        )
    }
}

@Composable
private fun AppLockGate(
    mode: AppLockMode,
    expectedPin: String?,
    errorMessage: String?,
    onPinUnlock: (String) -> Unit,
) {
    var pinInput by rememberSaveable { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        SoltraSectionCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(localized(AppTextKey.LOCK_GATE_TITLE), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (mode == AppLockMode.PIN && !expectedPin.isNullOrBlank()) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter(Char::isDigit).take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localized(AppTextKey.LOCK_GATE_PIN_LABEL)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )
                    Button(
                        onClick = { onPinUnlock(pinInput) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pinInput.length >= 4,
                    ) {
                        Text(localized(AppTextKey.LOCK_GATE_UNLOCK))
                    }
                }
                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ConfirmActionDialog(
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
private fun CsvMappingField(
    label: String,
    headers: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    allowNone: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = selected ?: if (allowNone) localized(AppTextKey.DROPDOWN_NONE) else localized(AppTextKey.DROPDOWN_CHOOSE_COLUMN)
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $display")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowNone) {
                DropdownMenuItem(
                    text = { Text(localized(AppTextKey.DROPDOWN_NONE)) },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    },
                )
            }
            headers.forEach { header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = {
                        onSelected(header)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountDropdown(
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
private fun AccountTypeDropdown(
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
private fun CategoryDropdown(
    label: String,
    categories: List<CategoryModel>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit,
    withAll: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = when {
        withAll && selectedCategoryId == null -> localized(AppTextKey.DROPDOWN_ALL_CATEGORIES)
        selectedCategoryId == null -> localized(AppTextKey.DROPDOWN_CHOOSE_CATEGORY)
        else -> categories.firstOrNull { it.id == selectedCategoryId }?.name ?: localized(AppTextKey.DROPDOWN_CHOOSE_CATEGORY)
    }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedLabel")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (withAll) {
                DropdownMenuItem(
                    text = { Text(localized(AppTextKey.DROPDOWN_ALL_CATEGORIES)) },
                    onClick = {
                        onCategorySelect(null)
                        expanded = false
                    },
                )
            }
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelect(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PaymentDropdown(
    label: String,
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${localized(selected.labelKey())}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PaymentMethod.entries.forEach { method ->
                DropdownMenuItem(
                    text = { Text(localized(method.labelKey())) },
                    onClick = {
                        onSelect(method)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RecurrenceFrequencyDropdown(
    selected: RecurrenceFrequency,
    onSelect: (RecurrenceFrequency) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("${localized(AppTextKey.FIELD_FREQUENCY)}: ${localized(selected.labelKey())}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RecurrenceFrequency.entries.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(localized(frequency.labelKey())) },
                    onClick = {
                        onSelect(frequency)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    epochMillis: Long,
    onValueChange: (Long) -> Unit,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val current = Calendar.getInstance().apply { timeInMillis = epochMillis }
                    current.set(year, month, dayOfMonth)
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            current.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            current.set(Calendar.MINUTE, minute)
                            current.set(Calendar.SECOND, 0)
                            current.set(Calendar.MILLISECOND, 0)
                            onValueChange(current.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true,
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            ).show()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("$label: ${epochMillis.formatDateTime()}")
    }
}

private fun QuickAddDatePreset.toEpochMillis(nowEpochMillis: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = nowEpochMillis }
    if (this == QuickAddDatePreset.YESTERDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }
    return calendar.timeInMillis
}






