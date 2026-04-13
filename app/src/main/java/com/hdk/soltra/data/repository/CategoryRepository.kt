package com.hdk.soltra.data.repository

import androidx.room.withTransaction
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.CategoryEntity
import com.hdk.soltra.domain.CategoryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val database: AppDatabase,
) {
    private val categoryDao = database.categoryDao()
    private val expenseDao = database.expenseDao()
    private val quickTemplateDao = database.quickTemplateDao()
    private val recurringRuleDao = database.recurringRuleDao()

    fun observeActive(): Flow<List<CategoryModel>> {
        return categoryDao.observeActive().map { categories -> categories.map { it.toDomain() } }
    }

    fun observeAll(): Flow<List<CategoryModel>> {
        return categoryDao.observeAll().map { categories -> categories.map { it.toDomain() } }
    }

    suspend fun getAll(): List<CategoryModel> {
        return categoryDao.getAll().map { it.toDomain() }
    }

    suspend fun updateBudget(categoryId: Long, budgetMinor: Long) {
        categoryDao.updateBudget(categoryId, budgetMinor)
    }

    suspend fun createCategory(name: String): Long {
        val all = categoryDao.getAll()
        val nextSortOrder = (all.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val created = CategoryEntity(
            name = name.trim(),
            colorHex = generatedColorFor(nextSortOrder),
            iconName = generatedIconFor(nextSortOrder),
            sortOrder = nextSortOrder,
            isActive = true,
            monthlyBudgetMinor = 0L,
        )
        return categoryDao.insert(created)
    }

    suspend fun renameCategory(categoryId: Long, name: String) {
        categoryDao.updateName(categoryId, name.trim())
    }

    suspend fun setCategoryActive(categoryId: Long, isActive: Boolean) {
        categoryDao.updateActive(categoryId, isActive)
    }

    suspend fun updateCategoryColor(categoryId: Long, colorHex: String) {
        val normalized = normalizeColor(colorHex) ?: return
        categoryDao.updateColor(categoryId, normalized)
    }

    suspend fun updateCategoryIcon(categoryId: Long, iconName: String) {
        val normalized = iconName.trim().ifBlank { "tag" }
        categoryDao.updateIcon(categoryId, normalized)
    }

    suspend fun moveCategory(categoryId: Long, direction: Int): Boolean {
        if (direction == 0) return false
        return database.withTransaction {
            val sorted = categoryDao.getAll().sortedBy { it.sortOrder }
            val currentIndex = sorted.indexOfFirst { it.id == categoryId }
            if (currentIndex == -1) return@withTransaction false
            val targetIndex = currentIndex + direction
            if (targetIndex !in sorted.indices) return@withTransaction false
            val current = sorted[currentIndex]
            val target = sorted[targetIndex]
            categoryDao.updateSortOrder(current.id, target.sortOrder)
            categoryDao.updateSortOrder(target.id, current.sortOrder)
            true
        }
    }

    suspend fun deleteCategory(
        categoryId: Long,
        replacementCategoryId: Long?,
    ): CategoryDeletionResult {
        val category = categoryDao.getById(categoryId)
            ?: return CategoryDeletionResult(false, "Categorie introuvable")
        val allCategories = categoryDao.getAll()
        if (allCategories.size <= 1) {
            return CategoryDeletionResult(false, "Garde au moins une categorie")
        }
        if (replacementCategoryId == categoryId) {
            return CategoryDeletionResult(false, "Choisis une categorie de remplacement differente")
        }

        val expenseRefs = expenseDao.countByCategoryId(categoryId)
        val templateRefs = quickTemplateDao.countByCategoryId(categoryId)
        val recurringRefs = recurringRuleDao.countByCategoryId(categoryId)
        val totalRefs = expenseRefs + templateRefs + recurringRefs

        if (totalRefs == 0) {
            categoryDao.deleteById(categoryId)
            return CategoryDeletionResult(true, "Categorie supprimee")
        }

        val replacementId = replacementCategoryId
            ?: return CategoryDeletionResult(
                false,
                buildUsageMessage(
                    expenseRefs = expenseRefs,
                    templateRefs = templateRefs,
                    recurringRefs = recurringRefs,
                ),
            )
        val replacement = categoryDao.getById(replacementId)
            ?: return CategoryDeletionResult(false, "Categorie de remplacement introuvable")

        database.withTransaction {
            expenseDao.reassignCategory(categoryId, replacement.id)
            quickTemplateDao.reassignCategory(categoryId, replacement.id)
            recurringRuleDao.reassignCategory(categoryId, replacement.id)
            val mergedBudget = replacement.monthlyBudgetMinor + category.monthlyBudgetMinor
            categoryDao.updateBudget(replacement.id, mergedBudget)
            categoryDao.deleteById(categoryId)
        }
        return CategoryDeletionResult(
            true,
            "Categorie supprimee et donnees reassignees vers ${replacement.name}",
        )
    }

    private fun buildUsageMessage(
        expenseRefs: Int,
        templateRefs: Int,
        recurringRefs: Int,
    ): String {
        val parts = buildList {
            if (expenseRefs > 0) add("$expenseRefs depense(s)")
            if (templateRefs > 0) add("$templateRefs modele(s)")
            if (recurringRefs > 0) add("$recurringRefs regle(s) recurrente(s)")
        }
        return "Categorie utilisee par ${parts.joinToString(", ")}. Choisis une categorie de remplacement pour la supprimer."
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

    private fun normalizeColor(input: String): String? {
        val cleaned = input.trim().uppercase()
        return if (Regex("^#[0-9A-F]{6}$").matches(cleaned)) cleaned else null
    }
}

data class CategoryDeletionResult(
    val success: Boolean,
    val message: String,
)
