package com.hdk.soltra.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.AccountEntity
import com.hdk.soltra.data.local.entity.AccountTransferEntity
import com.hdk.soltra.data.local.entity.BalanceCheckpointEntity
import com.hdk.soltra.data.local.entity.BudgetConfigEntity
import com.hdk.soltra.data.local.entity.CategoryEntity
import com.hdk.soltra.data.local.entity.ExpenseEntity
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.RecurrenceFrequency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class ImportExportRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val zoneId: ZoneId = ZoneId.systemDefault()

    suspend fun exportAllToFolder(folderUriString: String): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val folderUri = Uri.parse(folderUriString)
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            if (folder == null || !folder.isDirectory) {
                return@runCatching ExportResult(false, "Dossier export inaccessible")
            }

            val now = System.currentTimeMillis()
            val monthStamp = DateTimeFormatter.ofPattern("yyyy-MM")
                .format(Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()))
            val timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                .format(Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()))

            val categories = database.categoryDao().getAll()
            val expenses = database.expenseDao().getAll()
            val accounts = database.accountDao().getAll()
            val accountTransfers = database.accountTransferDao().getAll()
            val checkpoints = database.balanceCheckpointDao().getAll()
            val budget = database.budgetConfigDao().getOrNull() ?: BudgetConfigEntity()
            val templates = database.quickTemplateDao().getAll()
            val recurringRules = database.recurringRuleDao().getAll()

            val transactionsCsv = buildTransactionsCsv(
                expenses = expenses,
                categories = categories,
                currencyCode = budget.currencyCode,
            )
            val checkpointsCsv = buildCheckpointsCsv(checkpoints)
            val budgetsCsv = buildBudgetsCsv(budget, categories)
            val accountsCsv = buildAccountsCsv(accounts, budget.currencyCode)
            val accountTransfersCsv = buildAccountTransfersCsv(accountTransfers, accounts, budget.currencyCode)
            val monthlySummaryCsv = buildMonthlySummaryCsv(expenses)

            val snapshot = buildBackupSnapshot(
                now = now,
                categories = categories,
                expenses = expenses,
                checkpoints = checkpoints,
                budget = budget,
                templates = templates,
                recurringRules = recurringRules,
                accounts = accounts,
                accountTransfers = accountTransfers,
            )
            val backupJson = json.encodeToString(BackupSnapshot.serializer(), snapshot)

            val files = listOf(
                "transactions.csv" to transactionsCsv,
                "transactions-$monthStamp.csv" to transactionsCsv,
                "checkpoints.csv" to checkpointsCsv,
                "checkpoints-$monthStamp.csv" to checkpointsCsv,
                "budgets.csv" to budgetsCsv,
                "accounts.csv" to accountsCsv,
                "account_transfers.csv" to accountTransfersCsv,
                "monthly_summary.csv" to monthlySummaryCsv,
                "backup.json" to backupJson,
                "backup-$timestamp.json" to backupJson,
            )
            files.forEach { (name, content) ->
                writeTextFile(
                    resolver = context.contentResolver,
                    folder = folder,
                    fileName = name,
                    content = content,
                )
            }
            ExportResult(true, "Exports et backup mis a jour")
        }.getOrElse { error ->
            ExportResult(false, "Export echoue: ${error.message ?: "erreur inconnue"}")
        }
    }

    suspend fun exportBackupJsonToFile(fileUriString: String): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val fileUri = Uri.parse(fileUriString)
            val now = System.currentTimeMillis()
            val snapshot = buildBackupSnapshot(
                now = now,
                categories = database.categoryDao().getAll(),
                expenses = database.expenseDao().getAll(),
                accounts = database.accountDao().getAll(),
                accountTransfers = database.accountTransferDao().getAll(),
                checkpoints = database.balanceCheckpointDao().getAll(),
                budget = database.budgetConfigDao().getOrNull() ?: BudgetConfigEntity(),
                templates = database.quickTemplateDao().getAll(),
                recurringRules = database.recurringRuleDao().getAll(),
            )
            val backupJson = json.encodeToString(BackupSnapshot.serializer(), snapshot)
            writeTextToUri(
                resolver = context.contentResolver,
                uri = fileUri,
                content = backupJson,
            )
            ExportResult(true, "Backup JSON mis a jour")
        }.getOrElse { error ->
            ExportResult(false, "Export backup echoue: ${error.message ?: "erreur inconnue"}")
        }
    }

    suspend fun restoreFromBackup(backupUri: Uri): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(backupUri)?.bufferedReader()?.use { it.readText() }
                ?: return@runCatching ExportResult(false, "Lecture backup impossible")
            val snapshot = json.decodeFromString(BackupSnapshot.serializer(), content)
            val merged = mergeSnapshot(snapshot)
            ExportResult(
                success = true,
                message = "Restauration fusionnee: +${merged.addedExpenses} depenses, +${merged.addedAccounts} comptes, +${merged.addedTransfers} transferts, +${merged.addedCheckpoints} checkpoints, +${merged.addedTemplates} templates, +${merged.addedRecurringRules} regles recurrentes",
            )
        }.getOrElse { error ->
            ExportResult(false, "Restauration echouee: ${error.message ?: "erreur inconnue"}")
        }
    }

    suspend fun previewCsv(csvUri: Uri, maxRows: Int = 12): CsvPreview? = withContext(Dispatchers.IO) {
        val lines = context.contentResolver.openInputStream(csvUri)?.bufferedReader()?.use { reader ->
            reader.readLines()
        } ?: return@withContext null
        if (lines.isEmpty()) return@withContext null

        val parsed = lines.map { parseCsvLine(it) }.filter { it.isNotEmpty() }
        if (parsed.isEmpty()) return@withContext null
        val headers = parsed.first()
        val rows = parsed.drop(1).take(maxRows)
        CsvPreview(headers = headers, sampleRows = rows)
    }

    suspend fun importTransactionsCsv(
        csvUri: Uri,
        mapping: CsvMapping,
    ): CsvImportResult = withContext(Dispatchers.IO) {
        runCatching {
            val lines = context.contentResolver.openInputStream(csvUri)?.bufferedReader()?.use { it.readLines() }
                ?: return@runCatching CsvImportResult(0, 0, listOf("Lecture CSV impossible"))
            if (lines.isEmpty()) return@runCatching CsvImportResult(0, 0, listOf("CSV vide"))

            val parsed = lines.map { parseCsvLine(it) }.filter { it.isNotEmpty() }
            if (parsed.size < 2) return@runCatching CsvImportResult(0, 0, listOf("CSV sans lignes de donnees"))

            val headers = parsed.first()
            val headerToIndex = headers.mapIndexed { index, value -> normalizeKey(value) to index }.toMap()
            fun idx(column: String?): Int? = column?.let { headerToIndex[normalizeKey(it)] }

            val dateIdx = idx(mapping.dateColumn) ?: return@runCatching CsvImportResult(0, 0, listOf("Colonne date introuvable"))
            val amountIdx = idx(mapping.amountColumn) ?: return@runCatching CsvImportResult(0, 0, listOf("Colonne montant introuvable"))
            val categoryIdx = idx(mapping.categoryColumn) ?: return@runCatching CsvImportResult(0, 0, listOf("Colonne categorie introuvable"))
            val paymentIdx = idx(mapping.paymentColumn)
            val merchantIdx = idx(mapping.merchantColumn)
            val noteIdx = idx(mapping.noteColumn)

            val categoryDao = database.categoryDao()
            val expenseDao = database.expenseDao()
            val existingCategories = categoryDao.getAll().toMutableList()
            var nextSortOrder = (existingCategories.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val categoryByNormalized = existingCategories.associateBy { normalizeKey(it.name) }.toMutableMap()
            val existingExpenseKeys = expenseDao.getAll()
                .mapTo(mutableSetOf()) { entity ->
                    expenseKey(
                        amountMinor = entity.amountMinor,
                        occurredAtEpochMillis = entity.occurredAtEpochMillis,
                        categoryId = entity.categoryId,
                        paymentMethod = entity.paymentMethod,
                        merchantOrLabel = entity.merchantOrLabel,
                        note = entity.note,
                    )
                }

            var added = 0
            var skipped = 0
            val errors = mutableListOf<String>()
            parsed.drop(1).forEachIndexed { rowIndex, row ->
                val lineNo = rowIndex + 2
                fun cell(index: Int): String = if (index < row.size) row[index].trim() else ""

                val dateRaw = cell(dateIdx)
                val amountRaw = cell(amountIdx)
                val categoryRaw = cell(categoryIdx)
                if (dateRaw.isBlank() || amountRaw.isBlank() || categoryRaw.isBlank()) {
                    skipped += 1
                    errors += "Ligne $lineNo ignoree: date/montant/categorie manquant"
                    return@forEachIndexed
                }

                val occurredAt = parseDateCell(dateRaw)
                if (occurredAt == null) {
                    skipped += 1
                    errors += "Ligne $lineNo ignoree: date invalide '$dateRaw'"
                    return@forEachIndexed
                }
                val amountMinor = parseAmountMinor(amountRaw)
                if (amountMinor == null || amountMinor <= 0L) {
                    skipped += 1
                    errors += "Ligne $lineNo ignoree: montant invalide '$amountRaw'"
                    return@forEachIndexed
                }

                val normalizedCategory = normalizeKey(categoryRaw)
                val categoryId = categoryByNormalized[normalizedCategory]?.id ?: run {
                    val sortOrder = nextSortOrder
                    val colorHex = generatedColorFor(sortOrder)
                    val iconName = generatedIconFor(sortOrder)
                    val insertedId = categoryDao.insert(
                        CategoryEntity(
                            name = categoryRaw.trim(),
                            colorHex = colorHex,
                            iconName = iconName,
                            sortOrder = sortOrder,
                            isActive = true,
                        ),
                    )
                    val inserted = CategoryEntity(
                        id = insertedId,
                        name = categoryRaw.trim(),
                        colorHex = colorHex,
                        iconName = iconName,
                        sortOrder = sortOrder,
                        isActive = true,
                    )
                    nextSortOrder += 1
                    categoryByNormalized[normalizedCategory] = inserted
                    insertedId
                }

                val payment = parsePaymentMethod(paymentIdx?.let(::cell))
                val merchant = merchantIdx?.let(::cell)?.ifBlank { null }
                val note = noteIdx?.let(::cell)?.ifBlank { null }
                val dedupeKey = expenseKey(
                    amountMinor = amountMinor,
                    occurredAtEpochMillis = occurredAt,
                    categoryId = categoryId,
                    paymentMethod = payment.name,
                    merchantOrLabel = merchant,
                    note = note,
                )
                if (!existingExpenseKeys.add(dedupeKey)) {
                    skipped += 1
                    return@forEachIndexed
                }
                val now = System.currentTimeMillis()
                expenseDao.insert(
                    ExpenseEntity(
                        amountMinor = amountMinor,
                        occurredAtEpochMillis = occurredAt,
                        categoryId = categoryId,
                        paymentMethod = payment.name,
                        merchantOrLabel = merchant,
                        note = note,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                        source = "csv-import",
                    ),
                )
                added += 1
            }
            CsvImportResult(
                addedCount = added,
                skippedCount = skipped,
                errors = errors.take(50),
            )
        }.getOrElse { error ->
            CsvImportResult(
                addedCount = 0,
                skippedCount = 0,
                errors = listOf("Import echoue: ${error.message ?: "erreur inconnue"}"),
            )
        }
    }

    private fun buildTransactionsCsv(
        expenses: List<ExpenseEntity>,
        categories: List<CategoryEntity>,
        currencyCode: String,
    ): String {
        val categoryMap = categories.associateBy { it.id }
        val header = "id,date,amount_minor,currency,category,payment_method,merchant_or_label,note"
        val lines = expenses.map { expense ->
            val date = Instant.ofEpochMilli(expense.occurredAtEpochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
            listOf(
                expense.id.toString(),
                csv(date),
                expense.amountMinor.toString(),
                currencyCode,
                csv(categoryMap[expense.categoryId]?.name ?: "Unknown"),
                csv(expense.paymentMethod),
                csv(expense.merchantOrLabel.orEmpty()),
                csv(expense.note.orEmpty()),
            ).joinToString(",")
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    private fun buildCheckpointsCsv(checkpoints: List<BalanceCheckpointEntity>): String {
        val header = "id,date,bank_balance_minor,cash_balance_minor,note"
        val lines = checkpoints.map { cp ->
            val date = Instant.ofEpochMilli(cp.recordedAtEpochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
            listOf(
                cp.id.toString(),
                csv(date),
                cp.bankBalanceMinor.toString(),
                cp.cashBalanceMinor.toString(),
                csv(cp.note.orEmpty()),
            ).joinToString(",")
        }
        return (listOf(header) + lines).joinToString("\n")
    }

    private fun buildBudgetsCsv(
        budget: BudgetConfigEntity,
        categories: List<CategoryEntity>,
    ): String {
        val header = "type,name,amount_minor,currency"
        val globalLine = listOf("global", "monthly_budget", budget.monthlyBudgetMinor.toString(), budget.currencyCode)
            .joinToString(",")
        val categoryLines = categories.map { category ->
            listOf(
                "category",
                csv(category.name),
                category.monthlyBudgetMinor.toString(),
                budget.currencyCode,
            ).joinToString(",")
        }
        return (listOf(header, globalLine) + categoryLines).joinToString("\n")
    }

    private fun buildAccountsCsv(
        accounts: List<AccountEntity>,
        currencyCode: String,
    ): String {
        val header = "id,name,type,balance_minor,currency,is_active"
        val rows = accounts.map { account ->
            listOf(
                account.id.toString(),
                csv(account.name),
                account.type,
                account.balanceMinor.toString(),
                currencyCode,
                if (account.isActive) "1" else "0",
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun buildAccountTransfersCsv(
        transfers: List<AccountTransferEntity>,
        accounts: List<AccountEntity>,
        currencyCode: String,
    ): String {
        val accountById = accounts.associateBy { it.id }
        val header = "id,from_account,to_account,amount_minor,currency,occurred_at,note"
        val rows = transfers.map { transfer ->
            val fromName = accountById[transfer.fromAccountId]?.name ?: "Unknown"
            val toName = accountById[transfer.toAccountId]?.name ?: "Unknown"
            val occurredAt = Instant.ofEpochMilli(transfer.occurredAtEpochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
            listOf(
                transfer.id.toString(),
                csv(fromName),
                csv(toName),
                transfer.amountMinor.toString(),
                currencyCode,
                csv(occurredAt),
                csv(transfer.note.orEmpty()),
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun buildMonthlySummaryCsv(expenses: List<ExpenseEntity>): String {
        val header = "month,total_minor"
        val zone = ZoneId.systemDefault()
        val rows = expenses
            .groupBy { Instant.ofEpochMilli(it.occurredAtEpochMillis).atZone(zone).toLocalDate().withDayOfMonth(1) }
            .mapValues { (_, list) -> list.sumOf { it.amountMinor } }
            .toSortedMap()
            .entries
            .map { (monthStart, total) ->
                listOf(
                    csv(monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"))),
                    total.toString(),
                ).joinToString(",")
            }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun csv(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun buildBackupSnapshot(
        now: Long,
        categories: List<CategoryEntity>,
        expenses: List<ExpenseEntity>,
        accounts: List<AccountEntity>,
        accountTransfers: List<AccountTransferEntity>,
        checkpoints: List<BalanceCheckpointEntity>,
        budget: BudgetConfigEntity,
        templates: List<QuickTemplateEntity>,
        recurringRules: List<RecurringRuleEntity>,
    ): BackupSnapshot {
        return BackupSnapshot(
            schemaVersion = 4,
            exportedAtEpochMillis = now,
            categories = categories.map {
                BackupCategory(
                    id = it.id,
                    name = it.name,
                    colorHex = it.colorHex,
                    iconName = it.iconName,
                    sortOrder = it.sortOrder,
                    isActive = it.isActive,
                    monthlyBudgetMinor = it.monthlyBudgetMinor,
                )
            },
            expenses = expenses.map {
                BackupExpense(
                    id = it.id,
                    amountMinor = it.amountMinor,
                    occurredAtEpochMillis = it.occurredAtEpochMillis,
                    categoryId = it.categoryId,
                    paymentMethod = it.paymentMethod,
                    merchantOrLabel = it.merchantOrLabel,
                    note = it.note,
                    createdAtEpochMillis = it.createdAtEpochMillis,
                    updatedAtEpochMillis = it.updatedAtEpochMillis,
                    source = it.source,
                )
            },
            accounts = accounts.map {
                BackupAccount(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    balanceMinor = it.balanceMinor,
                    colorHex = it.colorHex,
                    isActive = it.isActive,
                    createdAtEpochMillis = it.createdAtEpochMillis,
                    updatedAtEpochMillis = it.updatedAtEpochMillis,
                )
            },
            accountTransfers = accountTransfers.map {
                BackupAccountTransfer(
                    id = it.id,
                    fromAccountId = it.fromAccountId,
                    toAccountId = it.toAccountId,
                    amountMinor = it.amountMinor,
                    occurredAtEpochMillis = it.occurredAtEpochMillis,
                    note = it.note,
                    createdAtEpochMillis = it.createdAtEpochMillis,
                )
            },
            checkpoints = checkpoints.map {
                BackupCheckpoint(
                    id = it.id,
                    recordedAtEpochMillis = it.recordedAtEpochMillis,
                    bankBalanceMinor = it.bankBalanceMinor,
                    cashBalanceMinor = it.cashBalanceMinor,
                    note = it.note,
                    createdAtEpochMillis = it.createdAtEpochMillis,
                    updatedAtEpochMillis = it.updatedAtEpochMillis,
                )
            },
            budgetConfig = BackupBudgetConfig(
                monthlyBudgetMinor = budget.monthlyBudgetMinor,
                currencyCode = budget.currencyCode,
                monthStartDay = budget.monthStartDay,
            ),
            templates = templates.map {
                BackupTemplate(
                    id = it.id,
                    name = it.name,
                    defaultAmountMinor = it.defaultAmountMinor,
                    defaultCategoryId = it.defaultCategoryId,
                    defaultPaymentMethod = it.defaultPaymentMethod,
                    defaultNote = it.defaultNote,
                    isPinned = it.isPinned,
                )
            },
            recurringRules = recurringRules.map {
                BackupRecurringRule(
                    id = it.id,
                    name = it.name,
                    amountMinor = it.amountMinor,
                    categoryId = it.categoryId,
                    paymentMethod = it.paymentMethod,
                    note = it.note,
                    frequency = it.frequency,
                    intervalValue = it.intervalValue,
                    nextRunEpochMillis = it.nextRunEpochMillis,
                    isActive = it.isActive,
                    createdAtEpochMillis = it.createdAtEpochMillis,
                    updatedAtEpochMillis = it.updatedAtEpochMillis,
                )
            },
        )
    }

    private fun writeTextToUri(
        resolver: ContentResolver,
        uri: Uri,
        content: String,
    ) {
        resolver.openOutputStream(uri, "wt").use { output ->
            requireNotNull(output) { "Impossible d'ouvrir le flux pour: $uri" }
            output.write(content.toByteArray(Charsets.UTF_8))
            output.flush()
        }
    }

    private fun writeTextFile(
        resolver: ContentResolver,
        folder: DocumentFile,
        fileName: String,
        content: String,
    ) {
        val file = folder.findFile(fileName) ?: folder.createFile("text/plain", fileName)
        requireNotNull(file) { "Impossible de creer le fichier: $fileName" }
        writeTextToUri(resolver = resolver, uri = file.uri, content = content)
    }

    private suspend fun mergeSnapshot(snapshot: BackupSnapshot): MergeStats {
        return database.withTransaction {
            val categoryDao = database.categoryDao()
            val expenseDao = database.expenseDao()
            val accountDao = database.accountDao()
            val accountTransferDao = database.accountTransferDao()
            val checkpointDao = database.balanceCheckpointDao()
            val templateDao = database.quickTemplateDao()
            val recurringDao = database.recurringRuleDao()
            val budgetDao = database.budgetConfigDao()

            val existingCategories = categoryDao.getAll()
            val normalizedCategoryMap = existingCategories.associateBy { normalizeKey(it.name) }.toMutableMap()
            val categoryIdMap = mutableMapOf<Long, Long>()
            var addedCategories = 0

            snapshot.categories.forEach { backupCategory ->
                val normalizedName = normalizeKey(backupCategory.name)
                val existing = normalizedCategoryMap[normalizedName]
                if (existing != null) {
                    categoryIdMap[backupCategory.id] = existing.id
                    if (existing.monthlyBudgetMinor == 0L && backupCategory.monthlyBudgetMinor > 0L) {
                        categoryDao.updateBudget(existing.id, backupCategory.monthlyBudgetMinor)
                    }
                } else {
                    val insertedId = categoryDao.insert(
                        CategoryEntity(
                            id = 0L,
                            name = backupCategory.name.trim(),
                            colorHex = backupCategory.colorHex,
                            iconName = backupCategory.iconName.ifBlank { "tag" },
                            sortOrder = backupCategory.sortOrder,
                            isActive = backupCategory.isActive,
                            monthlyBudgetMinor = backupCategory.monthlyBudgetMinor,
                        ),
                    )
                    categoryIdMap[backupCategory.id] = insertedId
                    val inserted = CategoryEntity(
                        id = insertedId,
                        name = backupCategory.name.trim(),
                        colorHex = backupCategory.colorHex,
                        iconName = backupCategory.iconName.ifBlank { "tag" },
                        sortOrder = backupCategory.sortOrder,
                        isActive = backupCategory.isActive,
                        monthlyBudgetMinor = backupCategory.monthlyBudgetMinor,
                    )
                    normalizedCategoryMap[normalizedName] = inserted
                    addedCategories += 1
                }
            }

            val existingExpenseKeys = expenseDao.getAll()
                .mapTo(mutableSetOf()) { entity ->
                    expenseKey(
                        amountMinor = entity.amountMinor,
                        occurredAtEpochMillis = entity.occurredAtEpochMillis,
                        categoryId = entity.categoryId,
                        paymentMethod = entity.paymentMethod,
                        merchantOrLabel = entity.merchantOrLabel,
                        note = entity.note,
                    )
                }
            var addedExpenses = 0
            snapshot.expenses.forEach { backupExpense ->
                val mappedCategoryId = categoryIdMap[backupExpense.categoryId] ?: return@forEach
                val payment = backupExpense.paymentMethod.takeIf { pm ->
                    runCatching { PaymentMethod.valueOf(pm) }.isSuccess
                } ?: PaymentMethod.CARTE_TPE.name
                val key = expenseKey(
                    amountMinor = backupExpense.amountMinor,
                    occurredAtEpochMillis = backupExpense.occurredAtEpochMillis,
                    categoryId = mappedCategoryId,
                    paymentMethod = payment,
                    merchantOrLabel = backupExpense.merchantOrLabel,
                    note = backupExpense.note,
                )
                if (existingExpenseKeys.add(key)) {
                    expenseDao.insert(
                        ExpenseEntity(
                            id = 0L,
                            amountMinor = backupExpense.amountMinor,
                            occurredAtEpochMillis = backupExpense.occurredAtEpochMillis,
                            categoryId = mappedCategoryId,
                            paymentMethod = payment,
                            merchantOrLabel = backupExpense.merchantOrLabel,
                            note = backupExpense.note,
                            createdAtEpochMillis = backupExpense.createdAtEpochMillis,
                            updatedAtEpochMillis = backupExpense.updatedAtEpochMillis,
                            source = backupExpense.source,
                        ),
                    )
                    addedExpenses += 1
                }
            }

            val existingAccounts = accountDao.getAll()
            val accountKeyToLocal = existingAccounts.associateBy { account ->
                accountKey(name = account.name, type = account.type)
            }.toMutableMap()
            val accountBalanceById = existingAccounts.associate { it.id to it.balanceMinor }.toMutableMap()
            val accountIdMap = mutableMapOf<Long, Long>()
            var addedAccounts = 0
            snapshot.accounts.forEach { backupAccount ->
                val key = accountKey(name = backupAccount.name, type = backupAccount.type)
                val existing = accountKeyToLocal[key]
                if (existing != null) {
                    accountIdMap[backupAccount.id] = existing.id
                } else {
                    val insertedId = accountDao.insert(
                        AccountEntity(
                            id = 0L,
                            name = backupAccount.name.trim(),
                            type = backupAccount.type,
                            balanceMinor = backupAccount.balanceMinor,
                            colorHex = backupAccount.colorHex,
                            isActive = backupAccount.isActive,
                            createdAtEpochMillis = backupAccount.createdAtEpochMillis,
                            updatedAtEpochMillis = backupAccount.updatedAtEpochMillis,
                        ),
                    )
                    accountIdMap[backupAccount.id] = insertedId
                    accountKeyToLocal[key] = AccountEntity(
                        id = insertedId,
                        name = backupAccount.name.trim(),
                        type = backupAccount.type,
                        balanceMinor = backupAccount.balanceMinor,
                        colorHex = backupAccount.colorHex,
                        isActive = backupAccount.isActive,
                        createdAtEpochMillis = backupAccount.createdAtEpochMillis,
                        updatedAtEpochMillis = backupAccount.updatedAtEpochMillis,
                    )
                    accountBalanceById[insertedId] = backupAccount.balanceMinor
                    addedAccounts += 1
                }
            }

            val existingTransferKeys = accountTransferDao.getAll()
                .mapTo(mutableSetOf()) { transfer ->
                    transferKey(
                        fromAccountId = transfer.fromAccountId,
                        toAccountId = transfer.toAccountId,
                        amountMinor = transfer.amountMinor,
                        occurredAtEpochMillis = transfer.occurredAtEpochMillis,
                        note = transfer.note,
                    )
                }
            var addedTransfers = 0
            snapshot.accountTransfers.forEach { backupTransfer ->
                val fromLocalId = accountIdMap[backupTransfer.fromAccountId] ?: return@forEach
                val toLocalId = accountIdMap[backupTransfer.toAccountId] ?: return@forEach
                if (fromLocalId == toLocalId) return@forEach
                val key = transferKey(
                    fromAccountId = fromLocalId,
                    toAccountId = toLocalId,
                    amountMinor = backupTransfer.amountMinor,
                    occurredAtEpochMillis = backupTransfer.occurredAtEpochMillis,
                    note = backupTransfer.note,
                )
                if (existingTransferKeys.add(key)) {
                    val now = System.currentTimeMillis()
                    accountTransferDao.insert(
                        AccountTransferEntity(
                            id = 0L,
                            fromAccountId = fromLocalId,
                            toAccountId = toLocalId,
                            amountMinor = backupTransfer.amountMinor,
                            occurredAtEpochMillis = backupTransfer.occurredAtEpochMillis,
                            note = backupTransfer.note,
                            createdAtEpochMillis = backupTransfer.createdAtEpochMillis,
                        ),
                    )
                    val fromBalance = (accountBalanceById[fromLocalId] ?: 0L) - backupTransfer.amountMinor
                    val toBalance = (accountBalanceById[toLocalId] ?: 0L) + backupTransfer.amountMinor
                    accountBalanceById[fromLocalId] = fromBalance
                    accountBalanceById[toLocalId] = toBalance
                    accountDao.updateBalance(fromLocalId, fromBalance, now)
                    accountDao.updateBalance(toLocalId, toBalance, now)
                    addedTransfers += 1
                }
            }

            val existingCheckpointKeys = checkpointDao.getAll()
                .mapTo(mutableSetOf()) { checkpoint ->
                    checkpointKey(
                        recordedAtEpochMillis = checkpoint.recordedAtEpochMillis,
                        bankBalanceMinor = checkpoint.bankBalanceMinor,
                        cashBalanceMinor = checkpoint.cashBalanceMinor,
                        note = checkpoint.note,
                    )
                }
            var addedCheckpoints = 0
            snapshot.checkpoints.forEach { backupCheckpoint ->
                val key = checkpointKey(
                    recordedAtEpochMillis = backupCheckpoint.recordedAtEpochMillis,
                    bankBalanceMinor = backupCheckpoint.bankBalanceMinor,
                    cashBalanceMinor = backupCheckpoint.cashBalanceMinor,
                    note = backupCheckpoint.note,
                )
                if (existingCheckpointKeys.add(key)) {
                    checkpointDao.insert(
                        BalanceCheckpointEntity(
                            id = 0L,
                            recordedAtEpochMillis = backupCheckpoint.recordedAtEpochMillis,
                            bankBalanceMinor = backupCheckpoint.bankBalanceMinor,
                            cashBalanceMinor = backupCheckpoint.cashBalanceMinor,
                            note = backupCheckpoint.note,
                            createdAtEpochMillis = backupCheckpoint.createdAtEpochMillis,
                            updatedAtEpochMillis = backupCheckpoint.updatedAtEpochMillis,
                        ),
                    )
                    addedCheckpoints += 1
                }
            }

            val existingTemplateKeys = templateDao.getAll()
                .mapTo(mutableSetOf()) { template ->
                    templateKey(
                        name = template.name,
                        amountMinor = template.defaultAmountMinor,
                        categoryId = template.defaultCategoryId,
                        paymentMethod = template.defaultPaymentMethod,
                        note = template.defaultNote,
                    )
                }
            var addedTemplates = 0
            snapshot.templates.forEach { backupTemplate ->
                val mappedCategoryId = categoryIdMap[backupTemplate.defaultCategoryId] ?: return@forEach
                val key = templateKey(
                    name = backupTemplate.name,
                    amountMinor = backupTemplate.defaultAmountMinor,
                    categoryId = mappedCategoryId,
                    paymentMethod = backupTemplate.defaultPaymentMethod,
                    note = backupTemplate.defaultNote,
                )
                if (existingTemplateKeys.add(key)) {
                    templateDao.insert(
                        QuickTemplateEntity(
                            id = 0L,
                            name = backupTemplate.name.trim(),
                            defaultAmountMinor = backupTemplate.defaultAmountMinor,
                            defaultCategoryId = mappedCategoryId,
                            defaultPaymentMethod = backupTemplate.defaultPaymentMethod.takeIf { pm ->
                                runCatching { PaymentMethod.valueOf(pm) }.isSuccess
                            } ?: PaymentMethod.CARTE_TPE.name,
                            defaultNote = backupTemplate.defaultNote,
                            isPinned = backupTemplate.isPinned,
                        ),
                    )
                    addedTemplates += 1
                }
            }

            val existingRecurringKeys = recurringDao.getAll()
                .mapTo(mutableSetOf()) { rule ->
                    recurringRuleKey(
                        name = rule.name,
                        amountMinor = rule.amountMinor,
                        categoryId = rule.categoryId,
                        paymentMethod = rule.paymentMethod,
                        note = rule.note,
                        frequency = rule.frequency,
                        intervalValue = rule.intervalValue,
                    )
                }
            var addedRecurringRules = 0
            snapshot.recurringRules.forEach { backupRule ->
                val mappedCategoryId = categoryIdMap[backupRule.categoryId] ?: return@forEach
                val payment = backupRule.paymentMethod.takeIf { pm ->
                    runCatching { PaymentMethod.valueOf(pm) }.isSuccess
                } ?: PaymentMethod.CARTE_TPE.name
                val frequency = backupRule.frequency.takeIf { freq ->
                    runCatching { RecurrenceFrequency.valueOf(freq) }.isSuccess
                } ?: RecurrenceFrequency.MONTHLY.name
                val key = recurringRuleKey(
                    name = backupRule.name,
                    amountMinor = backupRule.amountMinor,
                    categoryId = mappedCategoryId,
                    paymentMethod = payment,
                    note = backupRule.note,
                    frequency = frequency,
                    intervalValue = backupRule.intervalValue,
                )
                if (existingRecurringKeys.add(key)) {
                    recurringDao.insert(
                        RecurringRuleEntity(
                            id = 0L,
                            name = backupRule.name.trim(),
                            amountMinor = backupRule.amountMinor,
                            categoryId = mappedCategoryId,
                            paymentMethod = payment,
                            note = backupRule.note,
                            frequency = frequency,
                            intervalValue = backupRule.intervalValue.coerceIn(1, 365),
                            nextRunEpochMillis = backupRule.nextRunEpochMillis,
                            isActive = backupRule.isActive,
                            createdAtEpochMillis = backupRule.createdAtEpochMillis,
                            updatedAtEpochMillis = backupRule.updatedAtEpochMillis,
                        ),
                    )
                    addedRecurringRules += 1
                }
            }

            val currentBudget = budgetDao.getOrNull() ?: BudgetConfigEntity()
            val mergedBudget = currentBudget.copy(
                monthlyBudgetMinor = if (currentBudget.monthlyBudgetMinor > 0L) {
                    currentBudget.monthlyBudgetMinor
                } else {
                    snapshot.budgetConfig.monthlyBudgetMinor
                },
                currencyCode = if (currentBudget.currencyCode.isNotBlank()) currentBudget.currencyCode else snapshot.budgetConfig.currencyCode,
                monthStartDay = if (currentBudget.monthStartDay in 1..28) currentBudget.monthStartDay else snapshot.budgetConfig.monthStartDay.coerceIn(1, 28),
            )
            budgetDao.upsert(mergedBudget)

            MergeStats(
                addedCategories = addedCategories,
                addedExpenses = addedExpenses,
                addedAccounts = addedAccounts,
                addedTransfers = addedTransfers,
                addedCheckpoints = addedCheckpoints,
                addedTemplates = addedTemplates,
                addedRecurringRules = addedRecurringRules,
            )
        }
    }

    private fun normalizeKey(input: String?): String {
        return input.orEmpty().trim().lowercase(Locale.ROOT)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (ch == ',' && !inQuotes) {
                result += sb.toString()
                sb.setLength(0)
            } else {
                sb.append(ch)
            }
            i++
        }
        result += sb.toString()
        return result.map { it.trim() }
    }

    private fun parseDateCell(value: String): Long? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        trimmed.toLongOrNull()?.let { raw ->
            return if (raw > 9_999_999_999L) raw else raw * 1000L
        }
        runCatching { Instant.parse(trimmed).toEpochMilli() }.getOrNull()?.let { return it }
        runCatching { LocalDateTime.parse(trimmed).atZone(zoneId).toInstant().toEpochMilli() }.getOrNull()?.let { return it }

        val dateTimePatterns = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
        )
        dateTimePatterns.forEach { pattern ->
            val formatter = DateTimeFormatter.ofPattern(pattern)
            try {
                val localDateTime = LocalDateTime.parse(trimmed, formatter)
                return localDateTime.atZone(zoneId).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                // keep trying
            }
        }

        val datePatterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy")
        datePatterns.forEach { pattern ->
            val formatter = DateTimeFormatter.ofPattern(pattern)
            try {
                val localDate = LocalDate.parse(trimmed, formatter)
                return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                // keep trying
            }
        }
        return null
    }

    private fun parseAmountMinor(value: String): Long? {
        val raw = value.trim().replace(" ", "").replace("\u00A0", "")
        if (raw.isEmpty()) return null
        val normalized = when {
            raw.contains(',') && raw.contains('.') -> {
                if (raw.lastIndexOf(',') > raw.lastIndexOf('.')) {
                    raw.replace(".", "").replace(',', '.')
                } else {
                    raw.replace(",", "")
                }
            }
            raw.contains(',') -> raw.replace(',', '.')
            else -> raw
        }
        val decimal = normalized.toBigDecimalOrNull() ?: return null
        return decimal.movePointRight(2).toLong()
    }

    private fun parsePaymentMethod(value: String?): PaymentMethod {
        val key = normalizeKey(value)
        return when {
            key.contains("liq") || key.contains("cash") || key.contains("espece") || key.contains("esp") -> PaymentMethod.LIQUIDE
            key.contains("vir") || key.contains("transfer") -> PaymentMethod.VIREMENT
            key.contains("card") || key.contains("carte") || key.contains("tpe") || key.contains("cb") -> PaymentMethod.CARTE_TPE
            else -> PaymentMethod.CARTE_TPE
        }
    }

    private fun generatedColorFor(index: Int): String {
        val palette = listOf(
            "#2E7D32",
            "#6D4C41",
            "#1565C0",
            "#8E24AA",
            "#EF6C00",
            "#C62828",
            "#00897B",
            "#3949AB",
            "#7B1FA2",
            "#455A64",
            "#5D4037",
        )
        return palette[index % palette.size]
    }

    private fun generatedIconFor(index: Int): String {
        val icons = listOf(
            "utensils",
            "coffee",
            "transport",
            "party",
            "shopping",
            "health",
            "home",
            "subscription",
            "gift",
            "document",
            "tag",
        )
        return icons[index % icons.size]
    }

    private fun expenseKey(
        amountMinor: Long,
        occurredAtEpochMillis: Long,
        categoryId: Long,
        paymentMethod: String,
        merchantOrLabel: String?,
        note: String?,
    ): String {
        return listOf(
            amountMinor.toString(),
            occurredAtEpochMillis.toString(),
            categoryId.toString(),
            normalizeKey(paymentMethod),
            normalizeKey(merchantOrLabel),
            normalizeKey(note),
        ).joinToString("|")
    }

    private fun checkpointKey(
        recordedAtEpochMillis: Long,
        bankBalanceMinor: Long,
        cashBalanceMinor: Long,
        note: String?,
    ): String {
        return listOf(
            recordedAtEpochMillis.toString(),
            bankBalanceMinor.toString(),
            cashBalanceMinor.toString(),
            normalizeKey(note),
        ).joinToString("|")
    }

    private fun accountKey(
        name: String,
        type: String,
    ): String {
        return listOf(
            normalizeKey(name),
            normalizeKey(type),
        ).joinToString("|")
    }

    private fun transferKey(
        fromAccountId: Long,
        toAccountId: Long,
        amountMinor: Long,
        occurredAtEpochMillis: Long,
        note: String?,
    ): String {
        return listOf(
            fromAccountId.toString(),
            toAccountId.toString(),
            amountMinor.toString(),
            occurredAtEpochMillis.toString(),
            normalizeKey(note),
        ).joinToString("|")
    }

    private fun templateKey(
        name: String,
        amountMinor: Long?,
        categoryId: Long,
        paymentMethod: String,
        note: String?,
    ): String {
        return listOf(
            normalizeKey(name),
            (amountMinor ?: -1L).toString(),
            categoryId.toString(),
            normalizeKey(paymentMethod),
            normalizeKey(note),
        ).joinToString("|")
    }

    private fun recurringRuleKey(
        name: String,
        amountMinor: Long,
        categoryId: Long,
        paymentMethod: String,
        note: String?,
        frequency: String,
        intervalValue: Int,
    ): String {
        return listOf(
            normalizeKey(name),
            amountMinor.toString(),
            categoryId.toString(),
            normalizeKey(paymentMethod),
            normalizeKey(note),
            normalizeKey(frequency),
            intervalValue.toString(),
        ).joinToString("|")
    }
}

data class ExportResult(
    val success: Boolean,
    val message: String,
)

data class MergeStats(
    val addedCategories: Int,
    val addedExpenses: Int,
    val addedAccounts: Int,
    val addedTransfers: Int,
    val addedCheckpoints: Int,
    val addedTemplates: Int,
    val addedRecurringRules: Int,
)
