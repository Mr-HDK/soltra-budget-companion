package com.hdk.soltra.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(
    private val context: Context,
) {
    val exportFolderUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EXPORT_FOLDER_URI]
    }

    val backupFileUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKUP_FILE_URI]
    }

    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_BACKUP_ENABLED] ?: false
    }

    val backupIntervalHours: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKUP_INTERVAL_HOURS] ?: 24
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDERS_ENABLED] ?: true
    }

    val noExpenseReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NO_EXPENSE_REMINDER_ENABLED] ?: true
    }

    val checkpointReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CHECKPOINT_REMINDER_ENABLED] ?: true
    }

    val noExpenseReminderDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_NO_EXPENSE_REMINDER_DAYS] ?: 2
    }

    val appLockMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_MODE] ?: "none"
    }

    val appLockPin: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_PIN]
    }

    val appThemeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_THEME_MODE] ?: "system"
    }

    val appLanguagePreference: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LANGUAGE_PREFERENCE] ?: "system"
    }

    val quickWidgetDefaultCategoryId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_QUICK_WIDGET_DEFAULT_CATEGORY_ID]
    }

    val quickWidgetDefaultPaymentMethod: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_QUICK_WIDGET_DEFAULT_PAYMENT_METHOD] ?: "LIQUIDE"
    }

    val graphPreviewConfigJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_GRAPH_PREVIEW_CONFIG_JSON]
    }

    val graphWidgetsJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_GRAPH_WIDGETS_JSON]
    }

    suspend fun setExportFolderUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(KEY_EXPORT_FOLDER_URI)
            } else {
                prefs[KEY_EXPORT_FOLDER_URI] = uri
            }
        }
    }

    suspend fun setBackupFileUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(KEY_BACKUP_FILE_URI)
            } else {
                prefs[KEY_BACKUP_FILE_URI] = uri
            }
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setBackupIntervalHours(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BACKUP_INTERVAL_HOURS] = hours.coerceIn(1, 24 * 7)
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setNoExpenseReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NO_EXPENSE_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setCheckpointReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHECKPOINT_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setNoExpenseReminderDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NO_EXPENSE_REMINDER_DAYS] = days.coerceIn(1, 30)
        }
    }

    suspend fun setAppLockMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_MODE] = mode
        }
    }

    suspend fun setAppLockPin(pin: String?) {
        context.dataStore.edit { prefs ->
            if (pin.isNullOrBlank()) {
                prefs.remove(KEY_APP_LOCK_PIN)
            } else {
                prefs[KEY_APP_LOCK_PIN] = pin
            }
        }
    }

    suspend fun setAppThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_THEME_MODE] = mode
        }
    }

    suspend fun setAppLanguagePreference(preference: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE_PREFERENCE] = preference
        }
    }

    suspend fun setQuickWidgetDefaultCategoryId(categoryId: Long?) {
        context.dataStore.edit { prefs ->
            if (categoryId == null) {
                prefs.remove(KEY_QUICK_WIDGET_DEFAULT_CATEGORY_ID)
            } else {
                prefs[KEY_QUICK_WIDGET_DEFAULT_CATEGORY_ID] = categoryId
            }
        }
    }

    suspend fun setQuickWidgetDefaultPaymentMethod(method: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUICK_WIDGET_DEFAULT_PAYMENT_METHOD] = method
        }
    }

    suspend fun setGraphPreviewConfigJson(configJson: String?) {
        context.dataStore.edit { prefs ->
            if (configJson.isNullOrBlank()) {
                prefs.remove(KEY_GRAPH_PREVIEW_CONFIG_JSON)
            } else {
                prefs[KEY_GRAPH_PREVIEW_CONFIG_JSON] = configJson
            }
        }
    }

    suspend fun setGraphWidgetsJson(widgetsJson: String?) {
        context.dataStore.edit { prefs ->
            if (widgetsJson.isNullOrBlank()) {
                prefs.remove(KEY_GRAPH_WIDGETS_JSON)
            } else {
                prefs[KEY_GRAPH_WIDGETS_JSON] = widgetsJson
            }
        }
    }

    companion object {
        private val KEY_EXPORT_FOLDER_URI = stringPreferencesKey("export_folder_uri")
        private val KEY_BACKUP_FILE_URI = stringPreferencesKey("backup_file_uri")
        private val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val KEY_BACKUP_INTERVAL_HOURS = intPreferencesKey("backup_interval_hours")
        private val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val KEY_NO_EXPENSE_REMINDER_ENABLED = booleanPreferencesKey("no_expense_reminder_enabled")
        private val KEY_CHECKPOINT_REMINDER_ENABLED = booleanPreferencesKey("checkpoint_reminder_enabled")
        private val KEY_NO_EXPENSE_REMINDER_DAYS = intPreferencesKey("no_expense_reminder_days")
        private val KEY_APP_LOCK_MODE = stringPreferencesKey("app_lock_mode")
        private val KEY_APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        private val KEY_APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_APP_LANGUAGE_PREFERENCE = stringPreferencesKey("app_language_preference")
        private val KEY_QUICK_WIDGET_DEFAULT_CATEGORY_ID = longPreferencesKey("quick_widget_default_category_id")
        private val KEY_QUICK_WIDGET_DEFAULT_PAYMENT_METHOD = stringPreferencesKey("quick_widget_default_payment_method")
        private val KEY_GRAPH_PREVIEW_CONFIG_JSON = stringPreferencesKey("graph_preview_config_json")
        private val KEY_GRAPH_WIDGETS_JSON = stringPreferencesKey("graph_widgets_json")
    }
}
