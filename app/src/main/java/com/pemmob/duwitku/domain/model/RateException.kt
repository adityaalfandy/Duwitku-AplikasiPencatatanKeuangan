package com.pemmob.duwitku.domain.model

sealed class RateException(message: String) : Exception(message) {
    class NoInternetException : RateException("Tidak ada koneksi internet")
    class TimeoutException : RateException("Koneksi terlalu lama")
    class ServerException : RateException("Layanan bermasalah")
    class InvalidDataException : RateException("Data tidak valid")
    class UnknownException(cause: Throwable) : RateException(cause.message ?: "Kesalahan tidak diketahui")
}
