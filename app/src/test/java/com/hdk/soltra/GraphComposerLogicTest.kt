package com.hdk.soltra

import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphGrouping
import com.hdk.soltra.domain.GraphPeriod
import com.hdk.soltra.domain.GraphType
import com.hdk.soltra.domain.GraphWidgetConfigModel
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.ui.GraphComposerLogic
import com.hdk.soltra.domain.ExpenseRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GraphComposerLogicTest {
    private val zone = ZoneId.of("Europe/Paris")

    @Test
    fun `sanitizeGraphConfig normalizes title and swaps invalid custom range`() {
        val raw = GraphConfigModel(
            title = "   ",
            period = GraphPeriod.CUSTOM,
            customFromEpochMillis = epoch(2026, 3, 31),
            customToEpochMillis = epoch(2026, 1, 1),
        )

        val sanitized = GraphComposerLogic.sanitizeGraphConfig(raw)

        assertEquals("Depenses", sanitized.title)
        assertTrue((sanitized.customFromEpochMillis ?: 0L) <= (sanitized.customToEpochMillis ?: 0L))
    }

    @Test
    fun `buildGraphPreview aggregates by category with configured category color`() {
        val categories = listOf(
            CategoryModel(1L, "Repas", "#2E7D32", "utensils", 0, true, 0L),
            CategoryModel(2L, "Transport", "#1565C0", "transport", 1, true, 0L),
        )
        val expenses = listOf(
            expense(id = 1, amount = 1200L, categoryId = 1L, categoryName = "Repas", color = "#000000"),
            expense(id = 2, amount = 3800L, categoryId = 2L, categoryName = "Transport", color = "#FFFFFF"),
            expense(id = 3, amount = 800L, categoryId = 1L, categoryName = "Repas", color = "#FFFFFF"),
        )
        val config = GraphConfigModel(
            title = "Mars",
            type = GraphType.PIE,
            period = GraphPeriod.CUSTOM,
            grouping = GraphGrouping.CATEGORY,
            customFromEpochMillis = epoch(2026, 3, 1),
            customToEpochMillis = epoch(2026, 3, 31),
        )

        val preview = GraphComposerLogic.buildGraphPreview(
            config = config,
            monthStartDay = 1,
            allExpenses = expenses,
            categories = categories,
            zoneId = zone,
        )

        assertEquals(5800L, preview.totalMinor)
        assertEquals(2, preview.points.size)
        assertEquals("Transport", preview.points.first().label)
        assertEquals("#1565C0", preview.points.first().colorHex)
    }

    @Test
    fun `buildGraphPreview month grouping fills empty months in range`() {
        val expenses = listOf(
            expense(
                id = 1,
                amount = 2500L,
                categoryId = 1L,
                categoryName = "Repas",
                color = "#2E7D32",
                occurredAt = epoch(2026, 2, 15),
            ),
        )
        val config = GraphConfigModel(
            type = GraphType.BAR,
            period = GraphPeriod.CUSTOM,
            grouping = GraphGrouping.MONTH,
            customFromEpochMillis = epoch(2026, 1, 1),
            customToEpochMillis = epoch(2026, 3, 31),
        )

        val preview = GraphComposerLogic.buildGraphPreview(
            config = config,
            monthStartDay = 1,
            allExpenses = expenses,
            categories = emptyList(),
            zoneId = zone,
        )

        assertEquals(3, preview.points.size)
        assertEquals(0L, preview.points[0].valueMinor)
        assertEquals(2500L, preview.points[1].valueMinor)
        assertEquals(0L, preview.points[2].valueMinor)
    }

    @Test
    fun `normalizeWidgetOrders reindexes and sanitizes configs`() {
        val widgets = listOf(
            GraphWidgetConfigModel(
                id = 10L,
                order = 4,
                config = GraphConfigModel(title = "  "),
            ),
            GraphWidgetConfigModel(
                id = 11L,
                order = 2,
                config = GraphConfigModel(title = "Graph B"),
            ),
        )

        val normalized = GraphComposerLogic.normalizeWidgetOrders(widgets)

        assertEquals(2, normalized.size)
        assertEquals(0, normalized[0].order)
        assertEquals(1, normalized[1].order)
        assertEquals("Graph B", normalized[0].config.title)
        assertEquals("Depenses", normalized[1].config.title)
    }

    private fun expense(
        id: Long,
        amount: Long,
        categoryId: Long,
        categoryName: String,
        color: String,
        occurredAt: Long = epoch(2026, 3, 10),
    ): ExpenseRecord {
        return ExpenseRecord(
            id = id,
            amountMinor = amount,
            occurredAtEpochMillis = occurredAt,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorHex = color,
            paymentMethod = PaymentMethod.CARTE_TPE,
            merchantOrLabel = null,
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
