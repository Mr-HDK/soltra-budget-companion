package com.hdk.soltra

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.CategoryEntity
import com.hdk.soltra.data.local.entity.ExpenseEntity
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import com.hdk.soltra.data.repository.CategoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoryRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = CategoryRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `deleteCategory deletes directly when category is not referenced`() = runBlocking {
        val sourceId = insertCategory(name = "Source", budgetMinor = 1_000L, sortOrder = 0)
        insertCategory(name = "Other", budgetMinor = 2_000L, sortOrder = 1)

        val result = repository.deleteCategory(sourceId, replacementCategoryId = null)

        assertTrue(result.success)
        assertEquals("Categorie supprimee", result.message)
        assertNull(database.categoryDao().getById(sourceId))
    }

    @Test
    fun `deleteCategory refuses deletion without replacement when references exist`() = runBlocking {
        val sourceId = insertCategory(name = "Source", budgetMinor = 600L, sortOrder = 0)
        insertCategory(name = "Other", budgetMinor = 400L, sortOrder = 1)
        insertExpense(categoryId = sourceId)

        val result = repository.deleteCategory(sourceId, replacementCategoryId = null)

        assertFalse(result.success)
        assertTrue(result.message.contains("Choisis une categorie de remplacement"))
        assertNotNull(database.categoryDao().getById(sourceId))
    }

    @Test
    fun `deleteCategory reassigns linked data and merges budgets when replacement is provided`() = runBlocking {
        val sourceId = insertCategory(name = "Source", budgetMinor = 900L, sortOrder = 0)
        val replacementId = insertCategory(name = "Replacement", budgetMinor = 300L, sortOrder = 1)
        insertExpense(categoryId = sourceId)
        insertTemplate(categoryId = sourceId)
        insertRecurringRule(categoryId = sourceId)

        val result = repository.deleteCategory(sourceId, replacementCategoryId = replacementId)
        val replacement = database.categoryDao().getById(replacementId)

        assertTrue(result.success)
        assertTrue(result.message.contains("reassignees"))
        assertNull(database.categoryDao().getById(sourceId))
        assertNotNull(replacement)
        assertEquals(1_200L, replacement?.monthlyBudgetMinor)
        assertEquals(0, database.expenseDao().countByCategoryId(sourceId))
        assertEquals(1, database.expenseDao().countByCategoryId(replacementId))
        assertEquals(0, database.quickTemplateDao().countByCategoryId(sourceId))
        assertEquals(1, database.quickTemplateDao().countByCategoryId(replacementId))
        assertEquals(0, database.recurringRuleDao().countByCategoryId(sourceId))
        assertEquals(1, database.recurringRuleDao().countByCategoryId(replacementId))
    }

    private suspend fun insertCategory(
        name: String,
        budgetMinor: Long,
        sortOrder: Int,
    ): Long {
        return database.categoryDao().insert(
            CategoryEntity(
                name = name,
                colorHex = "#1565C0",
                iconName = "tag",
                sortOrder = sortOrder,
                isActive = true,
                monthlyBudgetMinor = budgetMinor,
            ),
        )
    }

    private suspend fun insertExpense(categoryId: Long) {
        database.expenseDao().insert(
            ExpenseEntity(
                amountMinor = 3_000L,
                occurredAtEpochMillis = 1_000L,
                categoryId = categoryId,
                paymentMethod = "CARTE_TPE",
                merchantOrLabel = "Store",
                note = null,
                createdAtEpochMillis = 1_000L,
                updatedAtEpochMillis = 1_000L,
            ),
        )
    }

    private suspend fun insertTemplate(categoryId: Long) {
        database.quickTemplateDao().insert(
            QuickTemplateEntity(
                name = "Template",
                defaultAmountMinor = 500L,
                defaultCategoryId = categoryId,
                defaultPaymentMethod = "CARTE_TPE",
                defaultNote = null,
                isPinned = false,
            ),
        )
    }

    private suspend fun insertRecurringRule(categoryId: Long) {
        database.recurringRuleDao().insert(
            RecurringRuleEntity(
                name = "Rule",
                amountMinor = 700L,
                categoryId = categoryId,
                paymentMethod = "CARTE_TPE",
                note = null,
                frequency = "MONTHLY",
                intervalValue = 1,
                nextRunEpochMillis = 2_000L,
                isActive = true,
                createdAtEpochMillis = 1_000L,
                updatedAtEpochMillis = 1_000L,
            ),
        )
    }
}

