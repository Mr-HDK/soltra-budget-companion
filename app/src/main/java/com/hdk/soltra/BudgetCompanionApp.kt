package com.hdk.soltra

import android.app.Application
import com.hdk.soltra.sync.BackupScheduler
import com.hdk.soltra.sync.ReminderNotifications
import com.hdk.soltra.sync.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetCompanionApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderNotifications.ensureChannel(this)
        appScope.launch {
            if (container.userSettingsRepository.autoBackupEnabled.first()) {
                val hours = container.userSettingsRepository.backupIntervalHours.first()
                BackupScheduler.schedule(this@BudgetCompanionApp, hours)
            }
            if (container.userSettingsRepository.remindersEnabled.first()) {
                ReminderScheduler.schedule(this@BudgetCompanionApp)
            }
        }
    }
}
