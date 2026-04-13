package com.hdk.soltra

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.data.local.entity.AccountTransferEntity
import com.hdk.soltra.data.repository.AccountRepository
import com.hdk.soltra.domain.AccountType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: AccountRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = AccountRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `deleteAccount removes account when no transfer references`() = runBlocking {
        val accountId = repository.createAccount(
            name = "Main",
            type = AccountType.BANK,
            initialBalanceMinor = 10_000L,
        )

        val result = repository.deleteAccount(accountId)

        assertTrue(result.success)
        assertEquals("Compte supprime", result.message)
        assertEquals(null, database.accountDao().getById(accountId))
    }

    @Test
    fun `deleteAccount refuses deletion without replacement when transfer exists`() = runBlocking {
        val fromId = repository.createAccount(
            name = "From",
            type = AccountType.BANK,
            initialBalanceMinor = 20_000L,
        )
        val toId = repository.createAccount(
            name = "To",
            type = AccountType.CASH,
            initialBalanceMinor = 0L,
        )
        database.accountTransferDao().insert(
            AccountTransferEntity(
                fromAccountId = fromId,
                toAccountId = toId,
                amountMinor = 2_500L,
                occurredAtEpochMillis = 1_000L,
                note = null,
                createdAtEpochMillis = 1_000L,
            ),
        )

        val result = repository.deleteAccount(fromId)

        assertFalse(result.success)
        assertTrue(result.message.contains("remplacement"))
        assertNotNull(database.accountDao().getById(fromId))
    }

    @Test
    fun `deleteAccount reassigns transfers when replacement is provided`() = runBlocking {
        val sourceId = repository.createAccount(
            name = "Source",
            type = AccountType.BANK,
            initialBalanceMinor = 20_000L,
        )
        val peerId = repository.createAccount(
            name = "Peer",
            type = AccountType.CARD,
            initialBalanceMinor = 5_000L,
        )
        val replacementId = repository.createAccount(
            name = "Replacement",
            type = AccountType.CASH,
            initialBalanceMinor = 1_000L,
        )
        database.accountTransferDao().insert(
            AccountTransferEntity(
                fromAccountId = sourceId,
                toAccountId = peerId,
                amountMinor = 2_500L,
                occurredAtEpochMillis = 1_000L,
                note = "outgoing",
                createdAtEpochMillis = 1_000L,
            ),
        )
        database.accountTransferDao().insert(
            AccountTransferEntity(
                fromAccountId = peerId,
                toAccountId = sourceId,
                amountMinor = 700L,
                occurredAtEpochMillis = 2_000L,
                note = "incoming",
                createdAtEpochMillis = 2_000L,
            ),
        )

        val result = repository.deleteAccount(
            accountId = sourceId,
            replacementAccountId = replacementId,
        )
        val transfers = database.accountTransferDao().getAll()

        assertTrue(result.success)
        assertTrue(result.message.contains("reassignes"))
        assertEquals(null, database.accountDao().getById(sourceId))
        assertEquals(0, database.accountTransferDao().countByAccountId(sourceId))
        assertEquals(2, database.accountTransferDao().countByAccountId(replacementId))
        assertTrue(transfers.all { it.fromAccountId != sourceId && it.toAccountId != sourceId })
    }

    @Test
    fun `updateTypeAndBalance updates persisted account fields`() = runBlocking {
        val accountId = repository.createAccount(
            name = "Wallet",
            type = AccountType.OTHER,
            initialBalanceMinor = 500L,
        )

        val updated = repository.updateTypeAndBalance(
            accountId = accountId,
            type = AccountType.EWALLET,
            balanceMinor = 7_500L,
        )
        val persisted = database.accountDao().getById(accountId)

        assertTrue(updated)
        assertNotNull(persisted)
        assertEquals(AccountType.EWALLET.name, persisted?.type)
        assertEquals(7_500L, persisted?.balanceMinor)
    }
}
