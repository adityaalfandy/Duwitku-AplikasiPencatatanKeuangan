package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetTotalBalanceUseCaseTest {

    private val useCase = GetTotalBalanceUseCase()

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        amount: Long
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        category = if (type == TransactionType.INCOME) Category.SALARY else Category.FOOD,
        date = LocalDate.of(2026, 1, 1),
        note = ""
    )

    @Test
    fun `invoke with empty list returns zero`() {
        assertEquals(0L, useCase(emptyList()))
    }

    @Test
    fun `invoke with only income returns positive total`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, 5000),
            createTransaction(2, TransactionType.INCOME, 10000)
        )
        
        assertEquals(15000L, useCase(transactions))
    }

    @Test
    fun `invoke with only expense returns negative total`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.EXPENSE, 5000),
            createTransaction(2, TransactionType.EXPENSE, 10000)
        )
        
        assertEquals(-15000L, useCase(transactions))
    }

    @Test
    fun `invoke with mixed transactions returns correct balance`() {
        val transactions = listOf(
            createTransaction(1, TransactionType.INCOME, 15000),
            createTransaction(2, TransactionType.EXPENSE, 5000),
            createTransaction(3, TransactionType.EXPENSE, 12000)
        )
        
        assertEquals(-2000L, useCase(transactions))
    }
}
