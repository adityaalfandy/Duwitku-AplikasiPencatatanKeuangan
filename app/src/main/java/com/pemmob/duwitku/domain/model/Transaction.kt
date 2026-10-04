package com.pemmob.duwitku.domain.model

import java.time.LocalDate

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val category: Category,
    val date: LocalDate,
    val note: String
)
