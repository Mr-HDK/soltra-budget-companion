package com.hdk.soltra.data.repository

import com.hdk.soltra.data.local.dao.BalanceCheckpointDao
import com.hdk.soltra.data.local.entity.BalanceCheckpointEntity
import com.hdk.soltra.domain.BalanceCheckpointModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BalanceCheckpointRepository(
    private val dao: BalanceCheckpointDao,
) {
    fun observeAll(): Flow<List<BalanceCheckpointModel>> {
        return dao.observeAll().map { it.map(BalanceCheckpointEntity::toDomain) }
    }

    suspend fun addCheckpoint(
        recordedAtEpochMillis: Long,
        bankBalanceMinor: Long,
        cashBalanceMinor: Long,
        note: String?,
    ) {
        val now = System.currentTimeMillis()
        dao.insert(
            BalanceCheckpointEntity(
                recordedAtEpochMillis = recordedAtEpochMillis,
                bankBalanceMinor = bankBalanceMinor,
                cashBalanceMinor = cashBalanceMinor,
                note = note?.trim()?.ifBlank { null },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun updateCheckpoint(
        id: Long,
        recordedAtEpochMillis: Long,
        bankBalanceMinor: Long,
        cashBalanceMinor: Long,
        note: String?,
    ) {
        val existing = dao.getById(id) ?: return
        val now = System.currentTimeMillis()
        dao.update(
            BalanceCheckpointEntity(
                id = existing.id,
                recordedAtEpochMillis = recordedAtEpochMillis,
                bankBalanceMinor = bankBalanceMinor,
                cashBalanceMinor = cashBalanceMinor,
                note = note?.trim()?.ifBlank { null },
                createdAtEpochMillis = existing.createdAtEpochMillis,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun deleteCheckpoint(id: Long) {
        dao.deleteById(id)
    }

    suspend fun latestOrNull(): BalanceCheckpointModel? {
        return dao.latestOrNull()?.toDomain()
    }

    suspend fun latestRecordedAtEpochMillisOrNull(): Long? {
        return dao.latestRecordedAtEpochMillisOrNull()
    }
}
