package com.pemmob.duwitku.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RateResponseDto(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
