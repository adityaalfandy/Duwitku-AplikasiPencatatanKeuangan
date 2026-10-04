package com.pemmob.duwitku.data.repository

import com.pemmob.duwitku.data.remote.RateApi
import com.pemmob.duwitku.data.remote.RateResponseDto
import com.pemmob.duwitku.domain.model.RateException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RateRepositoryImplTest {

    private class FakeRateApi(
        private val successResponse: RateResponseDto? = null,
        private val exceptionToThrow: Exception? = null
    ) : RateApi {
        override suspend fun getLatest(base: String, symbols: String): RateResponseDto {
            if (exceptionToThrow != null) throw exceptionToThrow
            return successResponse ?: throw IllegalStateException("Fake not configured")
        }
    }

    @Test
    fun getRates_success() = runBlocking {
        val dto = RateResponseDto(
            base = "IDR",
            date = "2026-10-02",
            rates = mapOf("USD" to 0.000061)
        )
        val repo = RateRepositoryImpl(FakeRateApi(successResponse = dto))
        
        val result = repo.getRates()
        assertTrue(result.isSuccess)
    }

    @Test
    fun getRates_emptyRates_returnsInvalidDataException() = runBlocking {
        val dto = RateResponseDto(
            base = "IDR",
            date = "2026-10-02",
            rates = emptyMap()
        )
        val repo = RateRepositoryImpl(FakeRateApi(successResponse = dto))
        
        val result = repo.getRates()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RateException.InvalidDataException)
    }

    @Test
    fun getRates_ioException_returnsNoInternetException() = runBlocking {
        val repo = RateRepositoryImpl(FakeRateApi(exceptionToThrow = IOException()))
        val result = repo.getRates()
        assertTrue(result.exceptionOrNull() is RateException.NoInternetException)
        
        val repo2 = RateRepositoryImpl(FakeRateApi(exceptionToThrow = UnknownHostException()))
        val result2 = repo2.getRates()
        assertTrue(result2.exceptionOrNull() is RateException.NoInternetException)
    }

    @Test
    fun getRates_timeout_returnsTimeoutException() = runBlocking {
        val repo = RateRepositoryImpl(FakeRateApi(exceptionToThrow = SocketTimeoutException()))
        val result = repo.getRates()
        assertTrue(result.exceptionOrNull() is RateException.TimeoutException)
    }

    @Test
    fun getRates_httpException_returnsServerException() = runBlocking {
        val response = Response.error<RateResponseDto>(
            500,
            "Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        val repo = RateRepositoryImpl(FakeRateApi(exceptionToThrow = HttpException(response)))
        val result = repo.getRates()
        assertTrue(result.exceptionOrNull() is RateException.ServerException)
    }

    @Test(expected = kotlinx.coroutines.CancellationException::class)
    fun getRates_cancellationException_isRethrown(): Unit = runBlocking {
        val repo = RateRepositoryImpl(FakeRateApi(exceptionToThrow = kotlinx.coroutines.CancellationException()))
        repo.getRates() // Should throw CancellationException directly
    }
}
