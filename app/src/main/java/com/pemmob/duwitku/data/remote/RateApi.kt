package com.pemmob.duwitku.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RateApi {
    @GET("v1/latest")
    suspend fun getLatest(
        @Query("base") base: String = "IDR",
        @Query("symbols") symbols: String = "USD,EUR,SGD,JPY,MYR,AUD"
    ): RateResponseDto
}
