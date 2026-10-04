package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FilterTransactionsUseCaseTest {

    private val useCase = FilterTransactionsUseCase()

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        note: String
    ) = Transaction(
        id = id,
        type = type,
        amount = 5000,
        category = Category.FOOD,
        date = LocalDate.of(2026, 1, 1),
        note = note
    )

    @Test
    fun `invoke with null type and empty query returns all`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, "Gaji"),
            createTransaction(2, TransactionType.EXPENSE, "Makan")
        )
        
        val result = useCase(transactions, null, "")
        
        assertEquals(2, result.size)
    }

    @Test
    fun `invoke filters by type only`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, "Gaji"),
            createTransaction(2, TransactionType.EXPENSE, "Makan"),
            createTransaction(3, TransactionType.EXPENSE, "Minum")
        )
        
        val result = useCase(transactions, TransactionType.EXPENSE, "")
        
        assertEquals(2, result.size)
        assertEquals(2L, result[0].id)
        assertEquals(3L, result[1].id)
    }

    @Test
    fun `invoke filters by query ignoring case and spaces`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, "Makan Siang"),
            createTransaction(2, TransactionType.EXPENSE, "makan malam"),
            createTransaction(3, TransactionType.EXPENSE, "Jajan")
        )
        
        val result = useCase(transactions, null, "  makan  ")
        
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }

    @Test
    fun `invoke filters by type and query simultaneously`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, "Uang dari teman"),
            createTransaction(2, TransactionType.EXPENSE, "Uang makan"),
            createTransaction(3, TransactionType.EXPENSE, "Jajan")
        )
        
        val result = useCase(transactions, TransactionType.INCOME, "uang")
        
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }
}
