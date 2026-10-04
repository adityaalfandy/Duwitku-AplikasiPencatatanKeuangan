package com.pemmob.duwitku.data.mapper

import com.pemmob.duwitku.data.remote.RateResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RateMapperTest {

    @Test
    fun toDomain_mapsSuccessfully() {
        val dto = RateResponseDto(
            base = "IDR",
            date = "2026-10-02",
            rates = mapOf(
                "USD" to 0.000061,
                "EUR" to 0.000052,
                "MISSING_OR_ZERO" to 0.0,
                "NEGATIVE" to -0.5
            )
        )
        
        val result = dto.toDomain()
        
        assertEquals(LocalDate.of(2026, 10, 2), result.date)
        assertEquals(2, result.rates.size)
        
        val usdRate = result.rates.find { it.currencyCode == "USD" }
        assertEquals(1.0 / 0.000061, usdRate?.rateToIdr ?: 0.0, 0.001)
        
        val eurRate = result.rates.find { it.currencyCode == "EUR" }
        assertEquals(1.0 / 0.000052, eurRate?.rateToIdr ?: 0.0, 0.001)
    }
}
