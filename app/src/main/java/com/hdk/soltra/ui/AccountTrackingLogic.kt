package com.hdk.soltra.ui

import com.hdk.soltra.domain.AccountModel
import com.hdk.soltra.domain.AccountTransferModel
import java.time.Instant
import java.time.ZoneId

data class AccountTrendUiState(
    val accountId: Long,
    val estimatedStartBalanceMinor: Long,
    val currentPeriodNetMinor: Long,
    val previousPeriodNetMinor: Long,
    val deltaVsPreviousPeriodMinor: Long,
    val hasTransferHistory: Boolean,
    val windowDays: Int,
)

fun buildAccountTrendMap(
    accounts: List<AccountModel>,
    transfers: List<AccountTransferModel>,
    zoneId: ZoneId,
    nowEpochMillis: Long = System.currentTimeMillis(),
    windowDays: Int = 30,
): Map<Long, AccountTrendUiState> {
    if (accounts.isEmpty()) return emptyMap()

    val currentEnd = nowEpochMillis
    val currentStartDate = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate().minusDays((windowDays - 1).toLong())
    val currentStart = currentStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val previousStart = currentStartDate.minusDays(windowDays.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val previousEnd = currentStart - 1L

    val hasTransferHistoryByAccountId = mutableMapOf<Long, Boolean>()
    val currentPeriodNetByAccountId = mutableMapOf<Long, Long>()
    val previousPeriodNetByAccountId = mutableMapOf<Long, Long>()

    transfers.forEach { transfer ->
        hasTransferHistoryByAccountId[transfer.fromAccountId] = true
        hasTransferHistoryByAccountId[transfer.toAccountId] = true

        if (transfer.occurredAtEpochMillis in currentStart..currentEnd) {
            currentPeriodNetByAccountId.merge(transfer.fromAccountId, -transfer.amountMinor, Long::plus)
            currentPeriodNetByAccountId.merge(transfer.toAccountId, transfer.amountMinor, Long::plus)
        } else if (transfer.occurredAtEpochMillis in previousStart..previousEnd) {
            previousPeriodNetByAccountId.merge(transfer.fromAccountId, -transfer.amountMinor, Long::plus)
            previousPeriodNetByAccountId.merge(transfer.toAccountId, transfer.amountMinor, Long::plus)
        }
    }

    return accounts.associate { account ->
        val currentPeriodNet = currentPeriodNetByAccountId[account.id] ?: 0L
        val previousPeriodNet = previousPeriodNetByAccountId[account.id] ?: 0L
        account.id to AccountTrendUiState(
            accountId = account.id,
            estimatedStartBalanceMinor = account.balanceMinor - currentPeriodNet,
            currentPeriodNetMinor = currentPeriodNet,
            previousPeriodNetMinor = previousPeriodNet,
            deltaVsPreviousPeriodMinor = currentPeriodNet - previousPeriodNet,
            hasTransferHistory = hasTransferHistoryByAccountId[account.id] == true,
            windowDays = windowDays,
        )
    }
}
