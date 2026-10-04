package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import javax.inject.Inject

class GetTotalBalanceUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): Long {
        var balance = 0L
        for (transaction in transactions) {
            when (transaction.type) {
                TransactionType.INCOME -> balance += transaction.amount
                TransactionType.EXPENSE -> balance -= transaction.amount
            }
        }
        return balance
    }
}
