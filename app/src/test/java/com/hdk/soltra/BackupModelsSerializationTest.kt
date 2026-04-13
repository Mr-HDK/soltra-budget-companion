package com.hdk.soltra

import com.hdk.soltra.data.repository.BackupBudgetConfig
import com.hdk.soltra.data.repository.BackupCategory
import com.hdk.soltra.data.repository.BackupCheckpoint
import com.hdk.soltra.data.repository.BackupExpense
import com.hdk.soltra.data.repository.BackupRecurringRule
import com.hdk.soltra.data.repository.BackupSnapshot
import com.hdk.soltra.data.repository.BackupTemplate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupModelsSerializationTest {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun `backup snapshot can serialize and deserialize`() {
        val snapshot = BackupSnapshot(
            schemaVersion = 2,
            exportedAtEpochMillis = 1710000000000L,
            categories = listOf(
                BackupCategory(1, "Alimentation", "#2E7D32", "utensils", 0, true, 50000L),
            ),
            expenses = listOf(
                BackupExpense(
                    id = 10,
                    amountMinor = 2345,
                    occurredAtEpochMillis = 1710000000000L,
                    categoryId = 1,
                    paymentMethod = "CARTE_TPE",
                    merchantOrLabel = "Cafe X",
                    note = null,
                    createdAtEpochMillis = 1710000000000L,
                    updatedAtEpochMillis = 1710000000000L,
                    source = "manual",
                ),
            ),
            checkpoints = listOf(
                BackupCheckpoint(
                    id = 11,
                    recordedAtEpochMillis = 1710000000000L,
                    bankBalanceMinor = 100000,
                    cashBalanceMinor = 5000,
                    note = "fin mois",
                    createdAtEpochMillis = 1710000000000L,
                    updatedAtEpochMillis = 1710000000000L,
                ),
            ),
            budgetConfig = BackupBudgetConfig(200000, "EUR", 1),
            templates = listOf(
                BackupTemplate(
                    id = 2,
                    name = "Taxi",
                    defaultAmountMinor = 800,
                    defaultCategoryId = 3,
                    defaultPaymentMethod = "LIQUIDE",
                    defaultNote = null,
                    isPinned = true,
                ),
            ),
            recurringRules = listOf(
                BackupRecurringRule(
                    id = 9,
                    name = "Loyer",
                    amountMinor = 120000,
                    categoryId = 1,
                    paymentMethod = "VIREMENT",
                    note = "Mensuel",
                    frequency = "MONTHLY",
                    intervalValue = 1,
                    nextRunEpochMillis = 1712600000000L,
                    isActive = true,
                    createdAtEpochMillis = 1710000000000L,
                    updatedAtEpochMillis = 1710000000000L,
                ),
            ),
        )

        val encoded = json.encodeToString(BackupSnapshot.serializer(), snapshot)
        val decoded = json.decodeFromString(BackupSnapshot.serializer(), encoded)
        assertEquals(snapshot, decoded)
    }

    @Test
    fun `backup snapshot can decode legacy payload without template or recurring fields`() {
        val legacyJson = """
            {
              "schemaVersion": 1,
              "exportedAtEpochMillis": 1710000000000,
              "categories": [],
              "expenses": [],
              "checkpoints": [],
              "budgetConfig": {
                "monthlyBudgetMinor": 0,
                "currencyCode": "TND",
                "monthStartDay": 1
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString(BackupSnapshot.serializer(), legacyJson)
        assertTrue(decoded.templates.isEmpty())
        assertTrue(decoded.recurringRules.isEmpty())
        assertEquals(0L, decoded.budgetConfig.monthlyBudgetMinor)
        assertEquals("TND", decoded.budgetConfig.currencyCode)
    }
}
