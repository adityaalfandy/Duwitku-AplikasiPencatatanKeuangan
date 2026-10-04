package com.pemmob.duwitku.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object TransactionsRoute

@Serializable
data object StatsRoute

@Serializable
data object RatesRoute

@Serializable
data class TransactionFormRoute(
    val transactionId: Long? = null,
    val type: String? = null,
)

@Serializable
data class TransactionDetailRoute(
    val transactionId: Long,
)
