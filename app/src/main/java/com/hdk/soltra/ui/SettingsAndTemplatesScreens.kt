package com.hdk.soltra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdk.soltra.domain.AppLockMode
import com.hdk.soltra.domain.AppThemeMode
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.QuickTemplateModel
import com.hdk.soltra.domain.RecurrenceFrequency
import com.hdk.soltra.domain.RecurringRuleModel
import com.hdk.soltra.i18n.AppLanguagePreference
import com.hdk.soltra.i18n.AppTextKey
import com.hdk.soltra.i18n.localized
import com.hdk.soltra.util.minorToInputString
import com.hdk.soltra.util.minorToMoneyString

@Composable
fun TemplatesScreen(
    categories: List<CategoryModel>,
    templates: List<QuickTemplateModel>,
    templateEditor: TemplateEditorUiState,
    recurringRules: List<RecurringRuleModel>,
    recurringEditor: RecurringRuleEditorUiState,
    onUpdateTemplateEditor: ((TemplateEditorUiState) -> TemplateEditorUiState) -> Unit,
    onSaveTemplate: () -> Unit,
    onEditTemplate: (QuickTemplateModel) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onResetTemplate: () -> Unit,
    onUpdateRecurringEditor: ((RecurringRuleEditorUiState) -> RecurringRuleEditorUiState) -> Unit,
    onSaveRecurring: () -> Unit,
    onEditRecurring: (RecurringRuleModel) -> Unit,
    onDeleteRecurring: (Long) -> Unit,
    onResetRecurring: () -> Unit,
    onRunRecurringNow: () -> Unit,
) {
    val activeCategories = remember(categories) { categories.filter { it.isActive } }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(localized(AppTextKey.UI_EDIT_TEMPLATE), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = templateEditor.name,
                    onValueChange = { value -> onUpdateTemplateEditor { it.copy(name = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.UI_TEMPLATE_NAME)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = templateEditor.amountInput,
                    onValueChange = { value -> onUpdateTemplateEditor { it.copy(amountInput = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.UI_DEFAULT_AMOUNT_OPTIONAL)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = templateEditor.note,
                    onValueChange = { value -> onUpdateTemplateEditor { it.copy(note = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                    singleLine = true,
                )
                ChipSelectCategory(
                    categories = activeCategories,
                    selectedCategoryId = templateEditor.categoryId,
                    onSelect = { id -> onUpdateTemplateEditor { it.copy(categoryId = id) } },
                )
                ChipSelectPayment(
                    selected = templateEditor.paymentMethod,
                    onSelect = { method -> onUpdateTemplateEditor { it.copy(paymentMethod = method) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSaveTemplate) { Text(localized(AppTextKey.UI_SAVE_TEMPLATE)) }
                    OutlinedButton(onClick = onResetTemplate) { Text(localized(AppTextKey.UI_RESET)) }
                }
            }
        }

        item { HorizontalDivider() }

        item {
            Text(localized(AppTextKey.UI_QUICK_TEMPLATES), style = MaterialTheme.typography.titleMedium)
        }
        items(templates, key = { it.id }) { template ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val amount = template.defaultAmountMinor?.minorToMoneyString("EUR") ?: "-"
                Text("${template.name} ($amount)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEditTemplate(template) }) { Text(localized(AppTextKey.UI_EDIT)) }
                    OutlinedButton(onClick = { onDeleteTemplate(template.id) }) { Text(localized(AppTextKey.COMMON_DELETE)) }
                }
            }
        }

        item { HorizontalDivider() }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(localized(AppTextKey.UI_EDIT_RECURRING), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = recurringEditor.name,
                    onValueChange = { value -> onUpdateRecurringEditor { it.copy(name = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.UI_RULE_NAME)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = recurringEditor.amountInput,
                    onValueChange = { value -> onUpdateRecurringEditor { it.copy(amountInput = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.SETTINGS_CSV_AMOUNT)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = recurringEditor.intervalValueInput,
                    onValueChange = { value -> onUpdateRecurringEditor { it.copy(intervalValueInput = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.UI_INTERVAL)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = recurringEditor.note,
                    onValueChange = { value -> onUpdateRecurringEditor { it.copy(note = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized(AppTextKey.BUDGETS_NOTE_OPTIONAL)) },
                    singleLine = true,
                )
                ChipSelectCategory(
                    categories = activeCategories,
                    selectedCategoryId = recurringEditor.categoryId,
                    onSelect = { id -> onUpdateRecurringEditor { it.copy(categoryId = id) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceFrequency.entries.forEach { frequency ->
                        FilterChip(
                            selected = recurringEditor.frequency == frequency,
                            onClick = { onUpdateRecurringEditor { it.copy(frequency = frequency) } },
                            label = { Text(frequency.name) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSaveRecurring) { Text(localized(AppTextKey.UI_SAVE_RULE)) }
                    OutlinedButton(onClick = onResetRecurring) { Text(localized(AppTextKey.UI_RESET_2)) }
                    OutlinedButton(onClick = onRunRecurringNow) { Text(localized(AppTextKey.UI_RUN_DUE)) }
                }
            }
        }

        item {
            Text(localized(AppTextKey.UI_RECURRING_RULES), style = MaterialTheme.typography.titleMedium)
        }
        items(recurringRules, key = { it.id }) { rule ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${rule.name} (${rule.amountMinor.minorToInputString()})")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEditRecurring(rule) }) { Text(localized(AppTextKey.UI_EDIT)) }
                    OutlinedButton(onClick = { onDeleteRecurring(rule.id) }) { Text(localized(AppTextKey.COMMON_DELETE)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    exportFolderUri: String?,
    backupFileUri: String?,
    autoBackupEnabled: Boolean,
    backupIntervalHours: Int,
    onPickFolder: () -> Unit,
    onPickBackupFile: () -> Unit,
    onExportNow: () -> Unit,
    onPickRestoreFile: () -> Unit,
    onPickCsvFile: () -> Unit,
    csvImportUi: CsvImportUiState,
    onUpdateCsvMapping: ((CsvImportUiState) -> CsvImportUiState) -> Unit,
    onImportCsv: () -> Unit,
    onClearCsvState: () -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit,
    onChangeBackupInterval: (Int) -> Unit,
    remindersEnabled: Boolean,
    noExpenseReminderEnabled: Boolean,
    checkpointReminderEnabled: Boolean,
    noExpenseReminderDays: Int,
    checkpointReminderDays: Int,
    budgetWarningPercent: Int,
    onToggleReminders: (Boolean) -> Unit,
    onToggleNoExpenseReminder: (Boolean) -> Unit,
    onToggleCheckpointReminder: (Boolean) -> Unit,
    onChangeNoExpenseReminderDays: (Int) -> Unit,
    onChangeCheckpointReminderDays: (Int) -> Unit,
    onChangeBudgetWarningPercent: (Int) -> Unit,
    appLockMode: AppLockMode,
    onSetAppLockMode: (AppLockMode) -> Unit,
    onSetAppLockPin: (String) -> Unit,
    appThemeMode: AppThemeMode,
    onSetAppThemeMode: (AppThemeMode) -> Unit,
    appLanguagePreference: AppLanguagePreference,
    onSetAppLanguagePreference: (AppLanguagePreference) -> Unit,
    widgetCategories: List<CategoryModel>,
    widgetDefaultCategoryId: Long?,
    onSetWidgetDefaultCategory: (Long?) -> Unit,
    widgetDefaultPaymentMethod: PaymentMethod,
    onSetWidgetDefaultPaymentMethod: (PaymentMethod) -> Unit,
) {
    var backupHoursInput by rememberSaveable(backupIntervalHours) { mutableStateOf(backupIntervalHours.toString()) }
    var inactivityDaysInput by rememberSaveable(noExpenseReminderDays) { mutableStateOf(noExpenseReminderDays.toString()) }
    var checkpointDaysInput by rememberSaveable(checkpointReminderDays) { mutableStateOf(checkpointReminderDays.toString()) }
    var budgetPercentInput by rememberSaveable(budgetWarningPercent) { mutableStateOf(budgetWarningPercent.toString()) }
    var pinInput by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_STORAGE_TITLE)) {
                Text("${localized(AppTextKey.SETTINGS_CURRENT_FOLDER)}: ${exportFolderUri ?: localized(AppTextKey.COMMON_NOT_CONFIGURED)}")
                Text("${localized(AppTextKey.SETTINGS_BACKUP_FILE)}: ${backupFileUri ?: localized(AppTextKey.COMMON_NOT_CONFIGURED)}")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPickFolder) { Text(localized(AppTextKey.SETTINGS_CHOOSE_EXPORT_FOLDER)) }
                    OutlinedButton(onClick = onPickBackupFile) { Text(localized(AppTextKey.SETTINGS_CHOOSE_BACKUP_FILE_FALLBACK)) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onExportNow) { Text(localized(AppTextKey.SETTINGS_EXPORT_NOW)) }
                    OutlinedButton(onClick = onPickRestoreFile) { Text(localized(AppTextKey.SETTINGS_RESTORE_FROM_BACKUP)) }
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_AUTO_BACKUP_TITLE)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(localized(AppTextKey.SETTINGS_ENABLE_AUTO_BACKUP))
                    Switch(checked = autoBackupEnabled, onCheckedChange = onToggleAutoBackup)
                }
                OutlinedTextField(
                    value = backupHoursInput,
                    onValueChange = { backupHoursInput = it.filter(Char::isDigit) },
                    label = { Text(localized(AppTextKey.SETTINGS_INTERVAL_HOURS)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedButton(
                    onClick = { backupHoursInput.toIntOrNull()?.let(onChangeBackupInterval) },
                ) {
                    Text(localized(AppTextKey.COMMON_APPLY))
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_REMINDERS_TITLE)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(localized(AppTextKey.SETTINGS_ENABLE_REMINDERS))
                    Switch(checked = remindersEnabled, onCheckedChange = onToggleReminders)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(localized(AppTextKey.SETTINGS_REMINDER_INACTIVITY_EXPENSE))
                    Switch(checked = noExpenseReminderEnabled, onCheckedChange = onToggleNoExpenseReminder)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(localized(AppTextKey.SETTINGS_REMINDER_WEEKLY_CHECKPOINT))
                    Switch(checked = checkpointReminderEnabled, onCheckedChange = onToggleCheckpointReminder)
                }
                OutlinedTextField(
                    value = inactivityDaysInput,
                    onValueChange = { inactivityDaysInput = it.filter(Char::isDigit) },
                    label = { Text(localized(AppTextKey.SETTINGS_REMINDER_THRESHOLD_DAYS)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedButton(onClick = { inactivityDaysInput.toIntOrNull()?.let(onChangeNoExpenseReminderDays) }) {
                    Text(localized(AppTextKey.COMMON_APPLY))
                }
                OutlinedTextField(
                    value = checkpointDaysInput,
                    onValueChange = { checkpointDaysInput = it.filter(Char::isDigit) },
                    label = { Text("Checkpoint threshold (days)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedButton(onClick = { checkpointDaysInput.toIntOrNull()?.let(onChangeCheckpointReminderDays) }) {
                    Text(localized(AppTextKey.COMMON_APPLY))
                }
                OutlinedTextField(
                    value = budgetPercentInput,
                    onValueChange = { budgetPercentInput = it.filter(Char::isDigit) },
                    label = { Text("Budget alert threshold (%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedButton(onClick = { budgetPercentInput.toIntOrNull()?.let(onChangeBudgetWarningPercent) }) {
                    Text(localized(AppTextKey.COMMON_APPLY))
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_QUICK_WIDGET_TITLE)) {
                ChipSelectCategory(
                    categories = widgetCategories,
                    selectedCategoryId = widgetDefaultCategoryId,
                    onSelect = onSetWidgetDefaultCategory,
                )
                ChipSelectPayment(
                    selected = widgetDefaultPaymentMethod,
                    onSelect = onSetWidgetDefaultPaymentMethod,
                )
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.LANGUAGE_SECTION_TITLE)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguagePreference.entries.forEach { pref ->
                        FilterChip(
                            selected = appLanguagePreference == pref,
                            onClick = { onSetAppLanguagePreference(pref) },
                            label = { Text(pref.storageValue.uppercase()) },
                        )
                    }
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_APPEARANCE_TITLE)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = appThemeMode == mode,
                            onClick = { onSetAppThemeMode(mode) },
                            label = { Text(mode.storageValue.uppercase()) },
                        )
                    }
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_SECURITY_TITLE)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLockMode.entries.forEach { mode ->
                        FilterChip(
                            selected = appLockMode == mode,
                            onClick = { onSetAppLockMode(mode) },
                            label = { Text(mode.storageValue.uppercase()) },
                        )
                    }
                }
                if (appLockMode == AppLockMode.PIN) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter(Char::isDigit).take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localized(AppTextKey.SETTINGS_PIN_LABEL)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )
                    OutlinedButton(onClick = { onSetAppLockPin(pinInput) }) { Text(localized(AppTextKey.SETTINGS_SAVE_PIN)) }
                }
            }
        }

        item {
            SettingsSection(title = localized(AppTextKey.SETTINGS_CSV_TITLE)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPickCsvFile) { Text(localized(AppTextKey.SETTINGS_CHOOSE_CSV_FILE)) }
                    OutlinedButton(onClick = onImportCsv) { Text(localized(AppTextKey.COMMON_IMPORT)) }
                    OutlinedButton(onClick = onClearCsvState) { Text(localized(AppTextKey.COMMON_CLEAR)) }
                }
                csvImportUi.preview?.let { preview ->
                    Text("${localized(AppTextKey.SETTINGS_COLUMNS_DETECTED)}: ${preview.headers.joinToString()}")
                    CsvMappingDropdown(
                        label = localized(AppTextKey.SETTINGS_CSV_DATE),
                        headers = preview.headers,
                        selected = csvImportUi.dateColumn,
                        onSelect = { value -> onUpdateCsvMapping { it.copy(dateColumn = value) } },
                    )
                    CsvMappingDropdown(
                        label = localized(AppTextKey.SETTINGS_CSV_AMOUNT),
                        headers = preview.headers,
                        selected = csvImportUi.amountColumn,
                        onSelect = { value -> onUpdateCsvMapping { it.copy(amountColumn = value) } },
                    )
                    CsvMappingDropdown(
                        label = localized(AppTextKey.SETTINGS_CSV_CATEGORY),
                        headers = preview.headers,
                        selected = csvImportUi.categoryColumn,
                        onSelect = { value -> onUpdateCsvMapping { it.copy(categoryColumn = value) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipSelectPayment(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethod.entries.forEach { method ->
            FilterChip(
                selected = selected == method,
                onClick = { onSelect(method) },
                label = { Text(method.name) },
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun ChipSelectCategory(
    categories: List<CategoryModel>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.take(6).forEach { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
            )
        }
    }
}

@Composable
private fun CsvMappingDropdown(
    label: String,
    headers: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selected ?: localized(AppTextKey.DROPDOWN_CHOOSE_COLUMN)
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedText")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            headers.forEach { header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = {
                        onSelect(header)
                        expanded = false
                    },
                )
            }
        }
    }
}
