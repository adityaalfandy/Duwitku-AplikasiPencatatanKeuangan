package com.pemmob.duwitku.data.mapper

import com.pemmob.duwitku.data.remote.RateResponseDto
import com.pemmob.duwitku.domain.model.ExchangeRate
import com.pemmob.duwitku.domain.model.RatesResult
import java.time.LocalDate

fun RateResponseDto.toDomain(): RatesResult {
    val date = LocalDate.parse(this.date)
    val exchangeRates = this.rates.mapNotNull { (code, rate) ->
        if (rate <= 0.0) null
        else ExchangeRate(currencyCode = code, rateToIdr = 1.0 / rate)
    }
    return RatesResult(date = date, rates = exchangeRates)
}
