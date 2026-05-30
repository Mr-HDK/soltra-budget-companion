package com.hdk.soltra

import com.hdk.soltra.util.amountExpressionToMinorOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountExpressionTest {
    @Test
    fun parsesPlainAmount() {
        assertEquals(1_050L, "10.50".amountExpressionToMinorOrNull())
    }

    @Test
    fun parsesAdditionWithSpaces() {
        assertEquals(1_500L, "10 + 5".amountExpressionToMinorOrNull())
    }

    @Test
    fun respectsOperatorPrecedence() {
        assertEquals(2_000L, "10 + 5 * 2".amountExpressionToMinorOrNull())
    }

    @Test
    fun rejectsInvalidOrNonPositiveInput() {
        assertNull("10+".amountExpressionToMinorOrNull())
        assertNull("-2".amountExpressionToMinorOrNull())
    }
}
