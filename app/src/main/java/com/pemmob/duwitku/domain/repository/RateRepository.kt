package com.pemmob.duwitku.domain.repository

import com.pemmob.duwitku.domain.model.RatesResult

interface RateRepository {
    suspend fun getRates(): Result<RatesResult>
}
