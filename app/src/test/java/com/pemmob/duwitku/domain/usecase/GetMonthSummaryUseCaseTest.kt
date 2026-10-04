package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class GetMonthSummaryUseCaseTest {

    private val useCase = GetMonthSummaryUseCase()

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        amount: Long,
        date: LocalDate
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        category = if (type == TransactionType.INCOME) Category.SALARY else Category.FOOD,
        date = date,
        note = ""
    )

    @Test
    fun `invoke with empty list returns zero summary`() {
        val result = useCase(emptyList(), YearMonth.of(2026, 1))
        
        assertEquals(0L, result.totalIncome)
        assertEquals(0L, result.totalExpense)
        assertEquals(0L, result.balance)
    }

    @Test
    fun `invoke only includes transactions in the specified month`() {
        val monthToTest = YearMonth.of(2026, 1) // January 2026
        
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, 5000, LocalDate.of(2025, 12, 31)), // out
            createTransaction(2, TransactionType.INCOME, 10000, LocalDate.of(2026, 1, 1)), // in
            createTransaction(3, TransactionType.EXPENSE, 2000, LocalDate.of(2026, 1, 31)), // in
            createTransaction(4, TransactionType.EXPENSE, 3000, LocalDate.of(2026, 2, 1)) // out
        )
        
        val result = useCase(transactions, monthToTest)
        
        assertEquals(10000L, result.totalIncome)
        assertEquals(2000L, result.totalExpense)
        assertEquals(8000L, result.balance)
    }

    @Test
    fun `invoke handles leap year correctly`() {
        val leapMonth = YearMonth.of(2024, 2)
        
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, 5000, LocalDate.of(2024, 2, 28)),
            createTransaction(2, TransactionType.EXPENSE, 10000, LocalDate.of(2024, 2, 29)), // leap day
            createTransaction(3, TransactionType.EXPENSE, 2000, LocalDate.of(2024, 3, 1)) // out
        )
        
        val result = useCase(transactions, leapMonth)
        
        assertEquals(0L, result.totalIncome)
        assertEquals(15000L, result.totalExpense)
        assertEquals(-15000L, result.balance)
    }
}
