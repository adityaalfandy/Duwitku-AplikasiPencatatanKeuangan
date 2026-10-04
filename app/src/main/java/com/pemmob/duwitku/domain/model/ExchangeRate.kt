package com.pemmob.duwitku.domain.model

import java.time.LocalDate

data class ExchangeRate(
    val currencyCode: String,
    val rateToIdr: Double
)

data class RatesResult(
    val date: LocalDate,
    val rates: List<ExchangeRate>
)
