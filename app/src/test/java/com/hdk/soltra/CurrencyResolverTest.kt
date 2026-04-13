package com.hdk.soltra

import com.hdk.soltra.util.resolveDefaultCurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencyResolverTest {
    @Test
    fun `resolver maps locale with known currency`() {
        assertEquals("EUR", resolveDefaultCurrencyCode(Locale.FRANCE))
        assertEquals("USD", resolveDefaultCurrencyCode(Locale.US))
    }

    @Test
    fun `resolver falls back to EUR when locale has no currency`() {
        assertEquals("EUR", resolveDefaultCurrencyCode(Locale("zz")))
    }
}

