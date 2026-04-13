package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickTemplateDao {
    @Query("SELECT * FROM quick_templates ORDER BY isPinned DESC, name ASC")
    fun observeAll(): Flow<List<QuickTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuickTemplateEntity): Long

    @Update
    suspend fun update(entity: QuickTemplateEntity)

    @Query("DELETE FROM quick_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM quick_templates ORDER BY isPinned DESC, name ASC")
    suspend fun getAll(): List<QuickTemplateEntity>

    @Query("SELECT COUNT(*) FROM quick_templates WHERE defaultCategoryId = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int

    @Query("UPDATE quick_templates SET defaultCategoryId = :targetCategoryId WHERE defaultCategoryId = :sourceCategoryId")
    suspend fun reassignCategory(sourceCategoryId: Long, targetCategoryId: Long): Int

    @Query("DELETE FROM quick_templates")
    suspend fun clear()
}
