package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdk.soltra.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getById(categoryId: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET monthlyBudgetMinor = :budgetMinor WHERE id = :categoryId")
    suspend fun updateBudget(categoryId: Long, budgetMinor: Long)

    @Query("UPDATE categories SET name = :name WHERE id = :categoryId")
    suspend fun updateName(categoryId: Long, name: String)

    @Query("UPDATE categories SET isActive = :isActive WHERE id = :categoryId")
    suspend fun updateActive(categoryId: Long, isActive: Boolean)

    @Query("UPDATE categories SET colorHex = :colorHex WHERE id = :categoryId")
    suspend fun updateColor(categoryId: Long, colorHex: String)

    @Query("UPDATE categories SET iconName = :iconName WHERE id = :categoryId")
    suspend fun updateIcon(categoryId: Long, iconName: String)

    @Query("UPDATE categories SET sortOrder = :sortOrder WHERE id = :categoryId")
    suspend fun updateSortOrder(categoryId: Long, sortOrder: Int)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: Long)

    @Query("DELETE FROM categories")
    suspend fun clear()
}
