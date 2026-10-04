package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class GetCategoryTotalsUseCaseTest {

    private val useCase = GetCategoryTotalsUseCase()

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        category: Category,
        amount: Long,
        date: LocalDate = LocalDate.of(2026, 1, 15)
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        category = category,
        date = date,
        note = ""
    )

    @Test
    fun `invoke with empty list returns empty`() {
        val result = useCase(emptyList(), YearMonth.of(2026, 1))
        assertEquals(true, result.isEmpty())
    }

    @Test
    fun `invoke with only income returns empty`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, Category.SALARY, 5000)
        )
        val result = useCase(transactions, YearMonth.of(2026, 1))
        assertEquals(true, result.isEmpty())
    }

    @Test
    fun `invoke with one category calculates 100 percent`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, Category.FOOD, 5000),
            createTransaction(2, TransactionType.EXPENSE, Category.FOOD, 5000)
        )
        val result = useCase(transactions, YearMonth.of(2026, 1))
        
        assertEquals(1, result.size)
        assertEquals(Category.FOOD, result[0].category)
        assertEquals(10000L, result[0].total)
        assertEquals(1.0f, result[0].percent, 0.001f)
    }

    @Test
    fun `invoke sums correctly and percentage adds up to around 1f`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, Category.FOOD, 3000),
            createTransaction(2, TransactionType.EXPENSE, Category.TRANSPORT, 1000),
            createTransaction(3, TransactionType.EXPENSE, Category.FOOD, 2000),
            createTransaction(4, TransactionType.EXPENSE, Category.BILLS, 4000)
        ) // total 10000
        
        val result = useCase(transactions, YearMonth.of(2026, 1))
        
        assertEquals(3, result.size)
        // Ordered by total descending
        assertEquals(Category.FOOD, result[0].category) // 5000 -> 50%
        assertEquals(5000L, result[0].total)
        assertEquals(0.5f, result[0].percent, 0.001f)
        
        assertEquals(Category.BILLS, result[1].category) // 4000 -> 40%
        assertEquals(4000L, result[1].total)
        assertEquals(0.4f, result[1].percent, 0.001f)
        
        assertEquals(Category.TRANSPORT, result[2].category) // 1000 -> 10%
        assertEquals(1000L, result[2].total)
        assertEquals(0.1f, result[2].percent, 0.001f)
        
        val totalPercent = result.sumOf { it.percent.toDouble() }.toFloat()
        assertEquals(1.0f, totalPercent, 0.001f)
    }

    @Test
    fun `invoke with tie in total sorts by enum ordinal`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, Category.BILLS, 5000),
            createTransaction(2, TransactionType.EXPENSE, Category.FOOD, 5000)
        )
        
        val result = useCase(transactions, YearMonth.of(2026, 1))
        
        assertEquals(2, result.size)
        // FOOD is declared before BILLS in Category enum
        assertEquals(Category.FOOD, result[0].category)
        assertEquals(Category.BILLS, result[1].category)
    }

    @Test
    fun `invoke only includes transactions in the specified month`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, Category.FOOD, 5000, LocalDate.of(2026, 1, 31)),
            createTransaction(2, TransactionType.EXPENSE, Category.FOOD, 10000, LocalDate.of(2026, 2, 1))
        )
        
        val result = useCase(transactions, YearMonth.of(2026, 1))
        
        assertEquals(1, result.size)
        assertEquals(5000L, result[0].total)
    }
}
