package com.hdk.soltra.util

import java.util.Currency
import java.util.Locale

private const val FALLBACK_CURRENCY_CODE = "EUR"

fun resolveDefaultCurrencyCode(locale: Locale = Locale.getDefault()): String {
    return runCatching { Currency.getInstance(locale).currencyCode }
        .getOrElse { FALLBACK_CURRENCY_CODE }
        .ifBlank { FALLBACK_CURRENCY_CODE }
}

