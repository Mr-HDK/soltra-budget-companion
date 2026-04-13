package com.hdk.soltra

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hdk.soltra.i18n.AppI18nProvider
import com.hdk.soltra.ui.BudgetCompanionRoot
import com.hdk.soltra.ui.MainViewModel
import com.hdk.soltra.ui.theme.BudgetCompanionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as BudgetCompanionApp).container)
    }

    private val notificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BudgetCompanion)
        super.onCreate(savedInstanceState)
        handleLaunchIntent(intent)
        requestNotificationsPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val appThemeMode = viewModel.appThemeMode.collectAsStateWithLifecycle().value
            val appLanguageTag = viewModel.appLanguageTag.collectAsStateWithLifecycle().value
            AppI18nProvider(forcedLanguageTag = appLanguageTag) {
                BudgetCompanionTheme(themeMode = appThemeMode) {
                    BudgetCompanionRoot(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        val pendingFocusFromWidget = consumePendingWidgetQuickAdd()
        val shouldOpenQuickAdd = intent?.action == ACTION_OPEN_QUICK_ADD ||
            intent?.getBooleanExtra(EXTRA_OPEN_QUICK_ADD, false) == true ||
            pendingFocusFromWidget != null
        if (shouldOpenQuickAdd) {
            val shouldFocusAmount = intent?.getBooleanExtra(EXTRA_FOCUS_QUICK_ADD_AMOUNT, false) == true ||
                pendingFocusFromWidget == true
            viewModel.requestQuickAddOpen(focusAmount = shouldFocusAmount)
        }
    }

    private fun consumePendingWidgetQuickAdd(): Boolean? {
        val prefs = getSharedPreferences(PREFS_WIDGET_QUICK_ADD, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_PENDING_WIDGET_QUICK_ADD_FOCUS)) return null
        val focus = prefs.getBoolean(KEY_PENDING_WIDGET_QUICK_ADD_FOCUS, true)
        prefs.edit().remove(KEY_PENDING_WIDGET_QUICK_ADD_FOCUS).apply()
        return focus
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val ACTION_OPEN_QUICK_ADD = "com.hdk.soltra.action.OPEN_QUICK_ADD"
        const val EXTRA_OPEN_QUICK_ADD = "open_quick_add"
        const val EXTRA_FOCUS_QUICK_ADD_AMOUNT = "focus_quick_add_amount"
        private const val PREFS_WIDGET_QUICK_ADD = "widget_quick_add_launch"
        private const val KEY_PENDING_WIDGET_QUICK_ADD_FOCUS = "pending_widget_quick_add_focus"

        fun markPendingWidgetQuickAdd(context: Context, focusAmount: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_QUICK_ADD, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PENDING_WIDGET_QUICK_ADD_FOCUS, focusAmount).apply()
        }
    }
}
