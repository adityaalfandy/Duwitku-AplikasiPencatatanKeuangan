package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.CategoryTotal
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryTotalsUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>, month: YearMonth): List<CategoryTotal> {
        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE && YearMonth.from(it.date) == month
        }
        
        if (expenses.isEmpty()) return emptyList()
        
        var totalExpense = 0L
        val totalsMap = mutableMapOf<Category, Long>()
        
        for (expense in expenses) {
            totalExpense += expense.amount
            val current = totalsMap[expense.category] ?: 0L
            totalsMap[expense.category] = current + expense.amount
        }
        
        if (totalExpense == 0L) return emptyList()
        
        return totalsMap.map { (category, total) ->
            CategoryTotal(
                category = category,
                total = total,
                percent = total.toFloat() / totalExpense.toFloat()
            )
        }.sortedWith(
            compareByDescending<CategoryTotal> { it.total }
                .thenBy { it.category.ordinal }
        )
    }
}
