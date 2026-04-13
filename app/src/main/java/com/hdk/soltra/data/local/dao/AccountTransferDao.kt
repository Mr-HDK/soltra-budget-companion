package com.hdk.soltra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hdk.soltra.data.local.AccountTransferWithAccounts
import com.hdk.soltra.data.local.entity.AccountTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountTransferDao {
    @Transaction
    @Query(
        """
        SELECT t.*, aFrom.name AS fromAccountName, aTo.name AS toAccountName
        FROM account_transfers t
        INNER JOIN accounts aFrom ON aFrom.id = t.fromAccountId
        INNER JOIN accounts aTo ON aTo.id = t.toAccountId
        ORDER BY t.occurredAtEpochMillis DESC, t.id DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int = 30): Flow<List<AccountTransferWithAccounts>>

    @Transaction
    @Query(
        """
        SELECT t.*, aFrom.name AS fromAccountName, aTo.name AS toAccountName
        FROM account_transfers t
        INNER JOIN accounts aFrom ON aFrom.id = t.fromAccountId
        INNER JOIN accounts aTo ON aTo.id = t.toAccountId
        ORDER BY t.occurredAtEpochMillis DESC, t.id DESC
        """,
    )
    fun observeAll(): Flow<List<AccountTransferWithAccounts>>

    @Query("SELECT * FROM account_transfers ORDER BY occurredAtEpochMillis DESC, id DESC")
    suspend fun getAll(): List<AccountTransferEntity>

    @Query("SELECT COUNT(*) FROM account_transfers WHERE fromAccountId = :accountId OR toAccountId = :accountId")
    suspend fun countByAccountId(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM account_transfers WHERE fromAccountId = :accountId")
    suspend fun countByFromAccountId(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM account_transfers WHERE toAccountId = :accountId")
    suspend fun countByToAccountId(accountId: Long): Int

    @Query("UPDATE account_transfers SET fromAccountId = :replacementAccountId WHERE fromAccountId = :accountId")
    suspend fun reassignFromAccount(accountId: Long, replacementAccountId: Long)

    @Query("UPDATE account_transfers SET toAccountId = :replacementAccountId WHERE toAccountId = :accountId")
    suspend fun reassignToAccount(accountId: Long, replacementAccountId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: AccountTransferEntity): Long
}
