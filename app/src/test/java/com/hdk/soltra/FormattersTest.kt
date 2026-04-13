package com.hdk.soltra

import com.hdk.soltra.util.moneyInputToMinorOrNull
import com.hdk.soltra.util.minorToMoneyString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class FormattersTest {
    @Test
    fun `moneyInputToMinorOrNull parses dot decimals`() {
        assertEquals(12345L, "123.45".moneyInputToMinorOrNull())
    }

    @Test
    fun `moneyInputToMinorOrNull parses comma decimals`() {
        assertEquals(12345L, "123,45".moneyInputToMinorOrNull())
    }

    @Test
    fun `moneyInputToMinorOrNull rejects invalid value`() {
        assertNull("abc".moneyInputToMinorOrNull())
    }

    @Test
    fun `minorToMoneyString defaults to EUR`() {
        assertTrue(0L.minorToMoneyString().endsWith("EUR"))
    }

    @Test
    fun `minorToMoneyString uses locale-specific separators`() {
        val french = 12345L.minorToMoneyString(currency = "EUR", locale = Locale.FRANCE)
        val us = 12345L.minorToMoneyString(currency = "USD", locale = Locale.US)

        assertTrue(french.contains(","))
        assertTrue(french.endsWith("EUR"))
        assertTrue(us.contains("."))
        assertTrue(us.endsWith("USD"))
    }
}
