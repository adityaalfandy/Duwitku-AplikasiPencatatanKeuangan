package com.pemmob.duwitku.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val localeId = Locale.forLanguageTag("id-ID")
    
    // Senin, 4 Oktober 2026
    private val fullFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
    
    // 4 Okt
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM", localeId)
    
    fun formatFull(date: LocalDate): String {
        return date.format(fullFormatter)
    }
    
    fun formatShort(date: LocalDate): String {
        return date.format(shortFormatter)
    }
}
