package com.hdk.soltra.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hdk.soltra.BudgetCompanionApp
import kotlinx.coroutines.flow.first

class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? BudgetCompanionApp ?: return Result.retry()
        val folderUri = app.container.userSettingsRepository.exportFolderUri.first()
        val backupFileUri = app.container.userSettingsRepository.backupFileUri.first()
        val result = when {
            !folderUri.isNullOrBlank() -> app.container.importExportRepository.exportAllToFolder(folderUri)
            !backupFileUri.isNullOrBlank() -> app.container.importExportRepository.exportBackupJsonToFile(backupFileUri)
            else -> return Result.success()
        }
        return if (result.success) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "auto_backup_work"
    }
}
