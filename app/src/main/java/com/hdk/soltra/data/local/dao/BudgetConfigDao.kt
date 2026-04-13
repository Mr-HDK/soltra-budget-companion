package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hdk.soltra.data.local.entity.BudgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetConfigDao {
    @Query("SELECT * FROM budget_config WHERE id = 1")
    fun observe(): Flow<BudgetConfigEntity?>

    @Query("SELECT * FROM budget_config WHERE id = 1")
    suspend fun getOrNull(): BudgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BudgetConfigEntity)
}
