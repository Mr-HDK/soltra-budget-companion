package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hdk.soltra.data.local.ExpenseWithCategory
import com.hdk.soltra.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query(
        """
        SELECT e.*, c.name AS categoryName, c.colorHex AS categoryColorHex
        FROM expenses e
        INNER JOIN categories c ON c.id = e.categoryId
        WHERE (:categoryId IS NULL OR e.categoryId = :categoryId)
          AND (:paymentMethod IS NULL OR e.paymentMethod = :paymentMethod)
          AND (:fromEpochMillis IS NULL OR e.occurredAtEpochMillis >= :fromEpochMillis)
          AND (:toEpochMillis IS NULL OR e.occurredAtEpochMillis <= :toEpochMillis)
          AND (
            :search IS NULL OR :search = '' OR
            e.merchantOrLabel LIKE '%' || :search || '%' OR
            e.note LIKE '%' || :search || '%'
          )
        ORDER BY e.occurredAtEpochMillis DESC, e.id DESC
        """,
    )
    fun observeFiltered(
        search: String?,
        categoryId: Long?,
        paymentMethod: String?,
        fromEpochMillis: Long?,
        toEpochMillis: Long?,
    ): Flow<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE occurredAtEpochMillis BETWEEN :fromEpochMillis AND :toEpochMillis")
    suspend fun sumAmountBetween(fromEpochMillis: Long, toEpochMillis: Long): Long

    @Query(
        """
        SELECT c.name AS categoryName, COALESCE(SUM(e.amountMinor), 0) AS totalMinor
        FROM categories c
        LEFT JOIN expenses e ON e.categoryId = c.id AND e.occurredAtEpochMillis BETWEEN :fromEpochMillis AND :toEpochMillis
        WHERE c.isActive = 1
        GROUP BY c.id
        ORDER BY totalMinor DESC, c.name ASC
        """,
    )
    suspend fun totalsByCategory(fromEpochMillis: Long, toEpochMillis: Long): List<CategoryTotalRow>

    @Query("SELECT * FROM expenses ORDER BY occurredAtEpochMillis DESC, id DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT occurredAtEpochMillis FROM expenses ORDER BY occurredAtEpochMillis DESC, id DESC LIMIT 1")
    suspend fun latestOccurredAtEpochMillisOrNull(): Long?

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int

    @Query("UPDATE expenses SET categoryId = :targetCategoryId WHERE categoryId = :sourceCategoryId")
    suspend fun reassignCategory(sourceCategoryId: Long, targetCategoryId: Long): Int

    @Query("DELETE FROM expenses")
    suspend fun clear()
}

data class CategoryTotalRow(
    val categoryName: String,
    val totalMinor: Long,
)
