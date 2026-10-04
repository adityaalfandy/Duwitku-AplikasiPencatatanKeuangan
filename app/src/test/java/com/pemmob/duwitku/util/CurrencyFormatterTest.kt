package com.pemmob.duwitku.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun formatRp_zero_returnsRp0() {
        assertEquals("Rp 0", CurrencyFormatter.formatRp(0))
    }

    @Test
    fun formatRp_thousands_returnsFormatted() {
        assertEquals("Rp 1.500", CurrencyFormatter.formatRp(1500))
    }

    @Test
    fun formatRp_millions_returnsFormatted() {
        assertEquals("Rp 2.500.000", CurrencyFormatter.formatRp(2500000))
    }

    @Test
    fun formatRp_negative_returnsFormattedWithMinus() {
        assertEquals("-Rp 1.500", CurrencyFormatter.formatRp(-1500))
    }

    @Test
    fun formatRpWithSign_income_returnsPlus() {
        assertEquals("+ Rp 1.500", CurrencyFormatter.formatRpWithSign(1500, isIncome = true))
    }

    @Test
    fun formatRpWithSign_expense_returnsMinus() {
        assertEquals("- Rp 1.500", CurrencyFormatter.formatRpWithSign(1500, isIncome = false))
    }
    
    @Test
    fun formatRpWithSign_zero_returnsNoSign() {
        assertEquals("Rp 0", CurrencyFormatter.formatRpWithSign(0, isIncome = true))
        assertEquals("Rp 0", CurrencyFormatter.formatRpWithSign(0, isIncome = false))
    }
}
