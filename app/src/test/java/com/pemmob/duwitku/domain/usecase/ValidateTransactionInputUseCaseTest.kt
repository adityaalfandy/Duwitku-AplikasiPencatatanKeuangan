package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidateTransactionInputUseCaseTest {

    private val useCase = ValidateTransactionInputUseCase()
    private val today = LocalDate.of(2026, 1, 15)

    @Test
    fun `invoke with all valid inputs returns valid result`() {
        val result = useCase(
            amountInput = "10000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = LocalDate.of(2026, 1, 15),
            note = "Valid note",
            today = today
        )
        
        assertTrue(result.isValid)
    }

    @Test
    fun `invoke with empty amount returns Empty error`() {
        val result = useCase(
            amountInput = "   ",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(AmountError.Empty, result.amountError)
    }

    @Test
    fun `invoke with zero amount returns NotPositive error`() {
        val result = useCase(
            amountInput = "0",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(AmountError.NotPositive, result.amountError)
    }

    @Test
    fun `invoke with exactly 999_999_999_999 amount returns valid result`() {
        val result = useCase(
            amountInput = "999999999999",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertTrue(result.isValid)
    }

    @Test
    fun `invoke with 1_000_000_000_000 amount returns TooLarge error`() {
        val result = useCase(
            amountInput = "1000000000000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(AmountError.TooLarge, result.amountError)
    }

    @Test
    fun `invoke with 25 digit amount returns TooLarge error and does not crash`() {
        val result = useCase(
            amountInput = "9999999999999999999999999",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(AmountError.TooLarge, result.amountError)
    }

    @Test
    fun `invoke with null category returns Missing error`() {
        val result = useCase(
            amountInput = "1000",
            category = null,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(CategoryError.Missing, result.categoryError)
    }

    @Test
    fun `invoke with category not matching type returns WrongType error`() {
        // FOOD is EXPENSE, but type is INCOME
        val result = useCase(
            amountInput = "1000",
            category = Category.FOOD,
            type = TransactionType.INCOME,
            date = today,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(CategoryError.WrongType, result.categoryError)
    }

    @Test
    fun `invoke with future date returns InFuture error`() {
        val tomorrow = today.plusDays(1)
        val result = useCase(
            amountInput = "1000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = tomorrow,
            note = "",
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(DateError.InFuture, result.dateError)
    }

    @Test
    fun `invoke with today date is valid`() {
        val result = useCase(
            amountInput = "1000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = "",
            today = today
        )
        
        assertTrue(result.isValid)
    }

    @Test
    fun `invoke with exactly 100 character note is valid`() {
        val note = "a".repeat(100)
        val result = useCase(
            amountInput = "1000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = note,
            today = today
        )
        
        assertTrue(result.isValid)
    }

    @Test
    fun `invoke with 101 character note returns TooLong error`() {
        val note = "a".repeat(101)
        val result = useCase(
            amountInput = "1000",
            category = Category.FOOD,
            type = TransactionType.EXPENSE,
            date = today,
            note = note,
            today = today
        )
        
        assertFalse(result.isValid)
        assertEquals(NoteError.TooLong, result.noteError)
    }
}
