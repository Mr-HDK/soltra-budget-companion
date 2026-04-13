package com.hdk.soltra.data.repository

import com.hdk.soltra.data.local.dao.QuickTemplateDao
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.domain.QuickTemplateModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuickTemplateRepository(
    private val dao: QuickTemplateDao,
) {
    fun observeAll(): Flow<List<QuickTemplateModel>> {
        return dao.observeAll().map { list -> list.map(QuickTemplateEntity::toDomain) }
    }

    suspend fun saveTemplate(
        id: Long?,
        name: String,
        defaultAmountMinor: Long?,
        defaultCategoryId: Long,
        defaultPaymentMethod: PaymentMethod,
        defaultNote: String?,
        isPinned: Boolean,
    ) {
        val entity = QuickTemplateEntity(
            id = id ?: 0L,
            name = name.trim(),
            defaultAmountMinor = defaultAmountMinor,
            defaultCategoryId = defaultCategoryId,
            defaultPaymentMethod = defaultPaymentMethod.name,
            defaultNote = defaultNote?.trim()?.ifBlank { null },
            isPinned = isPinned,
        )
        dao.insert(entity)
    }

    suspend fun deleteTemplate(id: Long) {
        dao.deleteById(id)
    }
}
