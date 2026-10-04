package com.pemmob.duwitku.data.repository

import com.pemmob.duwitku.data.mapper.toDomain
import com.pemmob.duwitku.data.remote.RateApi
import com.pemmob.duwitku.domain.model.RateException
import com.pemmob.duwitku.domain.model.RatesResult
import com.pemmob.duwitku.domain.repository.RateRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class RateRepositoryImpl @Inject constructor(
    private val api: RateApi
) : RateRepository {

    override suspend fun getRates(): Result<RatesResult> {
        return try {
            val response = api.getLatest()
            if (response.rates.isEmpty()) {
                Result.failure(RateException.InvalidDataException())
            } else {
                Result.success(response.toDomain())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapException(e))
        }
    }

    private fun mapException(e: Exception): RateException {
        return when (e) {
            is SocketTimeoutException -> RateException.TimeoutException()
            is UnknownHostException, is IOException -> RateException.NoInternetException()
            is HttpException -> RateException.ServerException()
            is kotlinx.serialization.SerializationException -> RateException.InvalidDataException()
            is IllegalArgumentException -> RateException.InvalidDataException()
            else -> RateException.UnknownException(e)
        }
    }
}
