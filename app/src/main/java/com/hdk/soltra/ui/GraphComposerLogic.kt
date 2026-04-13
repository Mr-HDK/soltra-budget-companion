package com.hdk.soltra.ui

import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.ExpenseRecord
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphGrouping
import com.hdk.soltra.domain.GraphPeriod
import com.hdk.soltra.domain.GraphWidgetConfigModel
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.util.monthRangeEpochMillis
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object GraphComposerLogic {
    private val monthGraphPalette = listOf(
        "#1565C0",
        "#2E7D32",
        "#EF6C00",
        "#8E24AA",
        "#00897B",
        "#C62828",
        "#3949AB",
        "#6D4C41",
    )

    fun sanitizeGraphConfig(config: GraphConfigModel): GraphConfigModel {
        val normalizedTitle = config.title.trim().take(40)
        val from = config.customFromEpochMillis
        val to = config.customToEpochMillis
        val normalizedRange = when {
            from == null || to == null -> from to to
            from <= to -> from to to
            else -> to to from
        }
        return config.copy(
            title = if (normalizedTitle.isBlank()) "Depenses" else normalizedTitle,
            customFromEpochMillis = normalizedRange.first,
            customToEpochMillis = normalizedRange.second,
        )
    }

    fun normalizeWidgetOrders(widgets: List<GraphWidgetConfigModel>): List<GraphWidgetConfigModel> {
        return widgets
            .sortedBy { it.order }
            .mapIndexed { index, widget ->
                widget.copy(order = index, config = sanitizeGraphConfig(widget.config))
            }
    }

    fun buildGraphPreview(
        config: GraphConfigModel,
        monthStartDay: Int,
        allExpenses: List<ExpenseRecord>,
        categories: List<CategoryModel>,
        zoneId: ZoneId,
        nowEpochMillis: Long = System.currentTimeMillis(),
        locale: Locale = Locale.getDefault(),
    ): GraphPreviewUiState {
        val normalized = sanitizeGraphConfig(config)
        val (from, to) = graphRangeFor(
            config = normalized,
            monthStartDay = monthStartDay,
            nowEpochMillis = nowEpochMillis,
            zoneId = zoneId,
        )
        val filtered = allExpenses.filter { expense -> expense.occurredAtEpochMillis in from..to }
        val points = buildGraphPoints(
            expenses = filtered,
            categories = categories,
            grouping = normalized.grouping,
            fromEpochMillis = from,
            toEpochMillis = to,
            zoneId = zoneId,
            locale = locale,
        )
        return GraphPreviewUiState(
            title = normalized.title.ifBlank { graphDefaultTitle(locale) },
            chartType = normalized.type,
            periodLabel = localizedPeriodLabel(normalized.period, locale),
            groupingLabel = localizedGroupingLabel(normalized.grouping, locale),
            fromEpochMillis = from,
            toEpochMillis = to,
            totalMinor = filtered.sumOf { it.amountMinor },
            points = points,
        )
    }

    fun graphRangeFor(
        config: GraphConfigModel,
        monthStartDay: Int,
        nowEpochMillis: Long,
        zoneId: ZoneId,
    ): Pair<Long, Long> {
        val nowDate = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()
        return when (config.period) {
            GraphPeriod.CURRENT_MONTH -> monthRangeEpochMillis(nowEpochMillis, monthStartDay)
            GraphPeriod.PREVIOUS_MONTH -> {
                val (currentFrom, _) = monthRangeEpochMillis(nowEpochMillis, monthStartDay)
                monthRangeEpochMillis((currentFrom - 86_400_000L).coerceAtLeast(0L), monthStartDay)
            }
            GraphPeriod.CURRENT_YEAR -> yearRange(nowDate.year, zoneId)
            GraphPeriod.PREVIOUS_YEAR -> yearRange(nowDate.year - 1, zoneId)
            GraphPeriod.CUSTOM -> {
                val customFrom = config.customFromEpochMillis
                val customTo = config.customToEpochMillis
                if (customFrom == null || customTo == null) {
                    monthRangeEpochMillis(nowEpochMillis, monthStartDay)
                } else if (customFrom <= customTo) {
                    customFrom to customTo
                } else {
                    customTo to customFrom
                }
            }
        }
    }

    fun buildGraphPoints(
        expenses: List<ExpenseRecord>,
        categories: List<CategoryModel>,
        grouping: GraphGrouping,
        fromEpochMillis: Long,
        toEpochMillis: Long,
        zoneId: ZoneId,
        locale: Locale = Locale.getDefault(),
    ): List<GraphPointUiState> {
        if (expenses.isEmpty() && grouping != GraphGrouping.MONTH) return emptyList()

        return when (grouping) {
            GraphGrouping.CATEGORY -> {
                val categoryById = categories.associateBy { it.id }
                expenses
                    .groupBy { it.categoryId }
                    .map { (categoryId, rows) ->
                        val first = rows.first()
                        val category = categoryById[categoryId]
                        GraphPointUiState(
                            key = categoryId.toString(),
                            label = category?.name ?: first.categoryName,
                            valueMinor = rows.sumOf { it.amountMinor },
                            colorHex = category?.colorHex ?: first.categoryColorHex,
                        )
                    }
                    .filter { it.valueMinor > 0L }
                    .sortedByDescending { it.valueMinor }
            }

            GraphGrouping.PAYMENT_METHOD -> {
                expenses
                    .groupBy { it.paymentMethod }
                    .map { (method, rows) ->
                        GraphPointUiState(
                            key = method.name,
                            label = localizedPaymentMethodLabel(method, locale),
                            valueMinor = rows.sumOf { it.amountMinor },
                            colorHex = graphPaymentMethodColor(method),
                        )
                    }
                    .filter { it.valueMinor > 0L }
                    .sortedByDescending { it.valueMinor }
            }

            GraphGrouping.MONTH -> {
                val totalsByMonth = expenses
                    .groupBy { expense ->
                        YearMonth.from(Instant.ofEpochMilli(expense.occurredAtEpochMillis).atZone(zoneId).toLocalDate())
                    }
                    .mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }
                val startMonth = YearMonth.from(Instant.ofEpochMilli(fromEpochMillis).atZone(zoneId).toLocalDate())
                val endMonth = YearMonth.from(Instant.ofEpochMilli(toEpochMillis).atZone(zoneId).toLocalDate())
                val graphMonthFormatter = DateTimeFormatter.ofPattern("MMM yy", locale)

                val points = mutableListOf<GraphPointUiState>()
                var cursor = startMonth
                var index = 0
                while (!cursor.isAfter(endMonth) && index < 60) {
                    points += GraphPointUiState(
                        key = cursor.toString(),
                        label = cursor.atDay(1).format(graphMonthFormatter).replaceFirstChar { c -> c.titlecase(locale) },
                        valueMinor = totalsByMonth[cursor] ?: 0L,
                        colorHex = monthGraphPalette[index % monthGraphPalette.size],
                    )
                    cursor = cursor.plusMonths(1)
                    index += 1
                }
                points
            }
        }
    }

    private fun yearRange(year: Int, zoneId: ZoneId): Pair<Long, Long> {
        val from = LocalDate.of(year, 1, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val toExclusive = LocalDate.of(year + 1, 1, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return from to (toExclusive - 1L)
    }

    private fun graphPaymentMethodColor(method: PaymentMethod): String {
        return when (method) {
            PaymentMethod.LIQUIDE -> "#2E7D32"
            PaymentMethod.CARTE_TPE -> "#1565C0"
            PaymentMethod.VIREMENT -> "#8E24AA"
        }
    }

    private fun graphDefaultTitle(locale: Locale): String {
        return if (locale.language.equals("fr", ignoreCase = true)) "Depenses" else "Expenses"
    }

    private fun localizedPaymentMethodLabel(method: PaymentMethod, locale: Locale): String {
        val isFrench = locale.language.equals("fr", ignoreCase = true)
        return when (method) {
            PaymentMethod.LIQUIDE -> if (isFrench) "Liquide" else "Cash"
            PaymentMethod.CARTE_TPE -> if (isFrench) "Carte/TPE" else "Card/POS"
            PaymentMethod.VIREMENT -> if (isFrench) "Virement" else "Transfer"
        }
    }

    private fun localizedPeriodLabel(period: GraphPeriod, locale: Locale): String {
        val isFrench = locale.language.equals("fr", ignoreCase = true)
        return when (period) {
            GraphPeriod.CURRENT_MONTH -> if (isFrench) "Mois courant" else "Current month"
            GraphPeriod.PREVIOUS_MONTH -> if (isFrench) "Mois precedent" else "Previous month"
            GraphPeriod.CURRENT_YEAR -> if (isFrench) "Annee courante" else "Current year"
            GraphPeriod.PREVIOUS_YEAR -> if (isFrench) "Annee precedente" else "Previous year"
            GraphPeriod.CUSTOM -> if (isFrench) "Periode personnalisable" else "Custom period"
        }
    }

    private fun localizedGroupingLabel(grouping: GraphGrouping, locale: Locale): String {
        val isFrench = locale.language.equals("fr", ignoreCase = true)
        return when (grouping) {
            GraphGrouping.CATEGORY -> if (isFrench) "Par categorie" else "By category"
            GraphGrouping.PAYMENT_METHOD -> if (isFrench) "Par paiement" else "By payment"
            GraphGrouping.MONTH -> if (isFrench) "Par mois" else "By month"
        }
    }
}
