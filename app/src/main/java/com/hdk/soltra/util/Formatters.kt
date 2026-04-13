package com.hdk.soltra.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

fun Long.minorToMoneyString(
    currency: String = "EUR",
    locale: Locale = Locale.getDefault(),
): String {
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val major = this / 100.0
    return "${formatter.format(major)} $currency"
}

fun Long.minorToInputString(): String {
    val sign = if (this < 0) "-" else ""
    val absolute = abs(this)
    val integer = absolute / 100
    val decimal = absolute % 100
    return if (decimal == 0L) {
        "$sign$integer"
    } else {
        "$sign$integer.${decimal.toString().padStart(2, '0')}"
    }
}

fun String.moneyInputToMinorOrNull(): Long? {
    if (isBlank()) return null
    val normalized = trim().replace(',', '.')
    val number = normalized.toBigDecimalOrNull() ?: return null
    return number.movePointRight(2).toLong()
}

fun Long.toLocalDateMillisAtStartOfDay(): Long {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun Long.formatDate(): String {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
}

fun Long.formatDateTime(): String {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter)
}

fun monthRangeEpochMillis(
    nowEpochMillis: Long = System.currentTimeMillis(),
    monthStartDay: Int = 1,
): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val now = Instant.ofEpochMilli(nowEpochMillis).atZone(zone)
    val safeStartDay = monthStartDay.coerceIn(1, 28)
    val date = now.toLocalDate()
    val periodStartDate = if (date.dayOfMonth >= safeStartDay) {
        date.withDayOfMonth(safeStartDay)
    } else {
        date.minusMonths(1).withDayOfMonth(safeStartDay)
    }
    val first = periodStartDate.atStartOfDay(zone).toInstant().toEpochMilli()
    val lastExclusive = periodStartDate.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return first to (lastExclusive - 1)
}

fun todayRangeEpochMillis(nowEpochMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zone).toLocalDate()
    val first = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val lastExclusive = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return first to (lastExclusive - 1)
}

fun epochToLocalDate(epochMillis: Long): LocalDate {
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}
