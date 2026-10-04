package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.MonthSummary
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

class GetMonthSummaryUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>, month: YearMonth): MonthSummary {
        val filtered = transactions.filter {
            YearMonth.from(it.date) == month
        }
        
        var totalIncome = 0L
        var totalExpense = 0L
        
        for (transaction in filtered) {
            when (transaction.type) {
                TransactionType.INCOME -> totalIncome += transaction.amount
                TransactionType.EXPENSE -> totalExpense += transaction.amount
            }
        }
        
        return MonthSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense
        )
    }
}
