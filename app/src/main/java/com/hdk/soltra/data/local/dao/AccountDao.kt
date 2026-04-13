package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdk.soltra.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isActive DESC, name ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY isActive DESC, name ASC")
    suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET name = :name, updatedAtEpochMillis = :updatedAt WHERE id = :accountId")
    suspend fun updateName(accountId: Long, name: String, updatedAt: Long)

    @Query("UPDATE accounts SET isActive = :isActive, updatedAtEpochMillis = :updatedAt WHERE id = :accountId")
    suspend fun updateActive(accountId: Long, isActive: Boolean, updatedAt: Long)

    @Query("UPDATE accounts SET balanceMinor = :balanceMinor, updatedAtEpochMillis = :updatedAt WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, balanceMinor: Long, updatedAt: Long)

    @Query(
        """
        UPDATE accounts
        SET type = :type,
            balanceMinor = :balanceMinor,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :accountId
        """,
    )
    suspend fun updateTypeAndBalance(accountId: Long, type: String, balanceMinor: Long, updatedAt: Long)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteById(accountId: Long)
}
