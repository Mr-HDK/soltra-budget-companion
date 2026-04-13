package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY isActive DESC, nextRunEpochMillis ASC, id ASC")
    fun observeAll(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextRunEpochMillis ASC, id ASC")
    suspend fun getActive(): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringRuleEntity): Long

    @Update
    suspend fun update(entity: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM recurring_rules ORDER BY isActive DESC, nextRunEpochMillis ASC, id ASC")
    suspend fun getAll(): List<RecurringRuleEntity>

    @Query("SELECT COUNT(*) FROM recurring_rules WHERE categoryId = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int

    @Query("UPDATE recurring_rules SET categoryId = :targetCategoryId WHERE categoryId = :sourceCategoryId")
    suspend fun reassignCategory(sourceCategoryId: Long, targetCategoryId: Long): Int
}
