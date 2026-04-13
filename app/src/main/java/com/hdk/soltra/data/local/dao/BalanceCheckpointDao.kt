package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdk.soltra.data.local.entity.BalanceCheckpointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceCheckpointDao {
    @Query("SELECT * FROM balance_checkpoints ORDER BY recordedAtEpochMillis DESC, id DESC")
    fun observeAll(): Flow<List<BalanceCheckpointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BalanceCheckpointEntity): Long

    @Update
    suspend fun update(entity: BalanceCheckpointEntity)

    @Query("SELECT * FROM balance_checkpoints WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BalanceCheckpointEntity?

    @Query("DELETE FROM balance_checkpoints WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM balance_checkpoints ORDER BY recordedAtEpochMillis DESC, id DESC LIMIT 1")
    suspend fun latestOrNull(): BalanceCheckpointEntity?

    @Query("SELECT * FROM balance_checkpoints ORDER BY recordedAtEpochMillis DESC, id DESC")
    suspend fun getAll(): List<BalanceCheckpointEntity>

    @Query("SELECT recordedAtEpochMillis FROM balance_checkpoints ORDER BY recordedAtEpochMillis DESC, id DESC LIMIT 1")
    suspend fun latestRecordedAtEpochMillisOrNull(): Long?

    @Query("DELETE FROM balance_checkpoints")
    suspend fun clear()
}
