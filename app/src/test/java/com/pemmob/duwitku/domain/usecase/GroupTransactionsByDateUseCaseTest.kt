package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GroupTransactionsByDateUseCaseTest {

    private val useCase = GroupTransactionsByDateUseCase()

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        amount: Long,
        date: LocalDate
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        category = Category.FOOD,
        date = date,
        note = ""
    )

    @Test
    fun `invoke with empty list returns empty list`() {
        val result = useCase(emptyList())
        assertEquals(true, result.isEmpty())
    }

    @Test
    fun `invoke groups transactions by date and sorts date descending`() {
        val date1 = LocalDate.of(2026, 1, 1)
        val date2 = LocalDate.of(2026, 1, 2)
        val date3 = LocalDate.of(2026, 1, 3)
        
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, 5000, date1),
            createTransaction(2, TransactionType.EXPENSE, 2000, date2),
            createTransaction(3, TransactionType.INCOME, 10000, date3),
            createTransaction(4, TransactionType.EXPENSE, 3000, date2)
        )
        
        val result = useCase(transactions)
        
        assertEquals(3, result.size)
        
        // Date 3 should be first (newest)
        assertEquals(date3, result[0].date)
        assertEquals(1, result[0].items.size)
        assertEquals(3L, result[0].items[0].id)
        
        // Date 2 should be second
        assertEquals(date2, result[1].date)
        assertEquals(2, result[1].items.size)
        // Order within group should follow input order (2 then 4)
        assertEquals(2L, result[1].items[0].id)
        assertEquals(4L, result[1].items[1].id)
        
        // Date 1 should be third (oldest)
        assertEquals(date1, result[2].date)
        assertEquals(1, result[2].items.size)
        assertEquals(1L, result[2].items[0].id)
    }

    @Test
    fun `invoke calculates day total correctly`() {
        val date1 = LocalDate.of(2026, 1, 1)
        
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, 15000, date1),
            createTransaction(2, TransactionType.EXPENSE, 5000, date1),
            createTransaction(3, TransactionType.EXPENSE, 2000, date1)
        )
        
        val result = useCase(transactions)
        
        assertEquals(1, result.size)
        assertEquals(8000L, result[0].dayTotal) // 15000 - 5000 - 2000
    }
}
