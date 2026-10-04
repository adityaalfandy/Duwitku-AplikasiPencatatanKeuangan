package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.DateGroup
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import javax.inject.Inject

class GroupTransactionsByDateUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): List<DateGroup> {
        if (transactions.isEmpty()) return emptyList()
        
        val groupedMap = mutableMapOf<java.time.LocalDate, MutableList<Transaction>>()
        
        for (transaction in transactions) {
            val list = groupedMap.getOrPut(transaction.date) { mutableListOf() }
            list.add(transaction)
        }
        
        return groupedMap.map { (date, items) ->
            var dayTotal = 0L
            for (item in items) {
                if (item.type == TransactionType.INCOME) dayTotal += item.amount
                else dayTotal -= item.amount
            }
            
            DateGroup(
                date = date,
                items = items.toList(), // preserve original input order within the day
                dayTotal = dayTotal
            )
        }.sortedByDescending { it.date }
    }
}
