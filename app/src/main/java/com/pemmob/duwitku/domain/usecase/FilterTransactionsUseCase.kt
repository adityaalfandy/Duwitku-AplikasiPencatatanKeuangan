package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import javax.inject.Inject

class FilterTransactionsUseCase @Inject constructor() {
    operator fun invoke(
        transactions: List<Transaction>,
        type: TransactionType?,
        query: String
    ): List<Transaction> {
        val trimmedQuery = query.trim()
        
        return transactions.filter { transaction ->
            val matchType = if (type != null) transaction.type == type else true
            val matchQuery = if (trimmedQuery.isNotEmpty()) {
                transaction.note.contains(trimmedQuery, ignoreCase = true)
            } else {
                true
            }
            
            matchType && matchQuery
        }
    }
}
