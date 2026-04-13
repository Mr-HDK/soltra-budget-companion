package com.hdk.soltra.data.repository

import androidx.room.withTransaction
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.AccountEntity
import com.hdk.soltra.data.local.entity.AccountTransferEntity
import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val database: AppDatabase,
) {
    private val accountDao = database.accountDao()
    private val transferDao = database.accountTransferDao()

    fun observeAccounts(): Flow<List<AccountModel>> {
        return accountDao.observeAll().map { list -> list.map(AccountEntity::toDomain) }
    }

    fun observeRecentTransfers(limit: Int = 30): Flow<List<AccountTransferModel>> {
        return transferDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }
    }

    fun observeAllTransfers(): Flow<List<AccountTransferModel>> {
        return transferDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceMinor: Long,
    ): Long {
        val now = System.currentTimeMillis()
        val all = accountDao.getAll()
        val color = generatedColorFor(all.size)
        return accountDao.insert(
            AccountEntity(
                name = name.trim(),
                type = type.name,
                balanceMinor = initialBalanceMinor,
                colorHex = color,
                isActive = true,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun renameAccount(accountId: Long, name: String) {
        accountDao.updateName(accountId, name.trim(), System.currentTimeMillis())
    }

    suspend fun setAccountActive(accountId: Long, isActive: Boolean) {
        accountDao.updateActive(accountId, isActive, System.currentTimeMillis())
    }

    suspend fun updateTypeAndBalance(
        accountId: Long,
        type: AccountType,
        balanceMinor: Long,
    ): Boolean {
        val existing = accountDao.getById(accountId) ?: return false
        accountDao.updateTypeAndBalance(
            accountId = existing.id,
            type = type.name,
            balanceMinor = balanceMinor,
            updatedAt = System.currentTimeMillis(),
        )
        return true
    }

    suspend fun deleteAccount(
        accountId: Long,
        replacementAccountId: Long? = null,
    ): AccountDeletionResult {
        val existing = accountDao.getById(accountId)
            ?: return AccountDeletionResult(false, "Compte introuvable")

        val sourceRefs = transferDao.countByFromAccountId(accountId)
        val destinationRefs = transferDao.countByToAccountId(accountId)
        val totalRefs = sourceRefs + destinationRefs

        if (totalRefs == 0) {
            accountDao.deleteById(existing.id)
            return AccountDeletionResult(true, "Compte supprime")
        }

        if (replacementAccountId == accountId) {
            return AccountDeletionResult(false, "Choisis un compte de remplacement different")
        }
        val replacementId = replacementAccountId
            ?: return AccountDeletionResult(
                false,
                buildTransferUsageMessage(
                    sourceRefs = sourceRefs,
                    destinationRefs = destinationRefs,
                ),
            )
        val replacement = accountDao.getById(replacementId)
            ?: return AccountDeletionResult(false, "Compte de remplacement introuvable")

        database.withTransaction {
            transferDao.reassignFromAccount(accountId, replacement.id)
            transferDao.reassignToAccount(accountId, replacement.id)
            accountDao.deleteById(existing.id)
        }
        return AccountDeletionResult(
            true,
            "Compte supprime et transferts reassignes vers ${replacement.name}",
        )
    }

    suspend fun transfer(
        fromAccountId: Long,
        toAccountId: Long,
        amountMinor: Long,
        occurredAtEpochMillis: Long,
        note: String?,
    ): TransferResult {
        if (fromAccountId == toAccountId) {
            return TransferResult(false, "Compte source et destination identiques")
        }
        if (amountMinor <= 0L) {
            return TransferResult(false, "Montant de transfert invalide")
        }

        return database.withTransaction {
            val fromAccount = accountDao.getById(fromAccountId)
            val toAccount = accountDao.getById(toAccountId)
            if (fromAccount == null || toAccount == null) {
                return@withTransaction TransferResult(false, "Compte introuvable")
            }
            if (!fromAccount.isActive || !toAccount.isActive) {
                return@withTransaction TransferResult(false, "Activer les deux comptes avant transfert")
            }
            if (fromAccount.balanceMinor < amountMinor) {
                return@withTransaction TransferResult(false, "Solde insuffisant sur le compte source")
            }

            val now = System.currentTimeMillis()
            accountDao.updateBalance(
                accountId = fromAccount.id,
                balanceMinor = fromAccount.balanceMinor - amountMinor,
                updatedAt = now,
            )
            accountDao.updateBalance(
                accountId = toAccount.id,
                balanceMinor = toAccount.balanceMinor + amountMinor,
                updatedAt = now,
            )
            transferDao.insert(
                AccountTransferEntity(
                    fromAccountId = fromAccount.id,
                    toAccountId = toAccount.id,
                    amountMinor = amountMinor,
                    occurredAtEpochMillis = occurredAtEpochMillis,
                    note = note?.trim()?.ifBlank { null },
                    createdAtEpochMillis = now,
                ),
            )
            TransferResult(true, "Transfert enregistre")
        }
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

    private fun buildTransferUsageMessage(
        sourceRefs: Int,
        destinationRefs: Int,
    ): String {
        val parts = buildList {
            if (sourceRefs > 0) add("$sourceRefs transfert(s) sortant(s)")
            if (destinationRefs > 0) add("$destinationRefs transfert(s) entrant(s)")
        }
        return "Compte utilise par ${parts.joinToString(", ")}. Choisis un compte de remplacement pour le supprimer."
    }
}

data class TransferResult(
    val success: Boolean,
    val message: String,
)

data class AccountDeletionResult(
    val success: Boolean,
    val message: String,
)
