package com.pemmob.duwitku.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateFormatterTest {

    @Test
    fun formatFull_formatsCorrectly() {
        val date = LocalDate.of(2026, 10, 4)
        // 4 Oktober 2026 jatuh pada hari Minggu
        assertEquals("Minggu, 4 Oktober 2026", DateFormatter.formatFull(date))
    }

    @Test
    fun formatShort_formatsCorrectly() {
        val date = LocalDate.of(2026, 10, 4)
        assertEquals("4 Okt", DateFormatter.formatShort(date))
    }

    @Test
    fun formatMonthYear_formatsCorrectly() {
        val month = java.time.YearMonth.of(2026, 10)
        assertEquals("Oktober 2026", DateFormatter.formatMonthYear(month))
    }
}
