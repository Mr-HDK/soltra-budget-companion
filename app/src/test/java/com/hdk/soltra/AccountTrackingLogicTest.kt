package com.hdk.soltra

import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import com.hdk.soltra.domain.AccountType
import com.hdk.soltra.ui.buildAccountTrendMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AccountTrackingLogicTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val nowEpochMillis = LocalDate.of(2026, 4, 1)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    @Test
    fun `buildAccountTrendMap computes rolling and previous period deltas`() {
        val accounts = listOf(
            AccountModel(1L, "Main bank", AccountType.BANK, 10_000L, "#1565C0", true),
            AccountModel(2L, "Crypto wallet", AccountType.EWALLET, 5_000L, "#8E24AA", true),
        )
        val transfers = listOf(
            transfer(2L, 1L, 1_000L, epoch(2026, 3, 10)),
            transfer(1L, 2L, 3_000L, epoch(2026, 3, 20)),
            transfer(2L, 1L, 500L, epoch(2026, 2, 15)),
        )

        val trends = buildAccountTrendMap(
            accounts = accounts,
            transfers = transfers,
            zoneId = zone,
            nowEpochMillis = nowEpochMillis,
            windowDays = 30,
        )

        val bankTrend = requireNotNull(trends[1L])
        assertTrue(bankTrend.hasTransferHistory)
        assertEquals(-2_000L, bankTrend.currentPeriodNetMinor)
        assertEquals(500L, bankTrend.previousPeriodNetMinor)
        assertEquals(-2_500L, bankTrend.deltaVsPreviousPeriodMinor)
        assertEquals(12_000L, bankTrend.estimatedStartBalanceMinor)
    }

    @Test
    fun `buildAccountTrendMap leaves accounts without transfers marked as unavailable`() {
        val trends = buildAccountTrendMap(
            accounts = listOf(AccountModel(9L, "Home cash", AccountType.CASH, 4_200L, "#2E7D32", true)),
            transfers = emptyList(),
            zoneId = zone,
            nowEpochMillis = nowEpochMillis,
            windowDays = 30,
        )

        val homeCashTrend = requireNotNull(trends[9L])
        assertFalse(homeCashTrend.hasTransferHistory)
        assertEquals(4_200L, homeCashTrend.estimatedStartBalanceMinor)
        assertEquals(0L, homeCashTrend.currentPeriodNetMinor)
        assertEquals(0L, homeCashTrend.previousPeriodNetMinor)
    }

    private fun transfer(
        fromAccountId: Long,
        toAccountId: Long,
        amountMinor: Long,
        occurredAtEpochMillis: Long,
    ): AccountTransferModel {
        return AccountTransferModel(
            id = occurredAtEpochMillis,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            fromAccountName = "From",
            toAccountName = "To",
            amountMinor = amountMinor,
            occurredAtEpochMillis = occurredAtEpochMillis,
            note = null,
        )
    }

    private fun epoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }
}
