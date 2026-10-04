package com.pemmob.duwitku.domain.usecase

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.TransactionType
import java.time.LocalDate
import javax.inject.Inject

sealed interface AmountError {
    data object Empty : AmountError
    data object NotPositive : AmountError
    data object TooLarge : AmountError
}

sealed interface CategoryError {
    data object Missing : CategoryError
    data object WrongType : CategoryError
}

sealed interface DateError {
    data object InFuture : DateError
}

sealed interface NoteError {
    data object TooLong : NoteError
}

data class ValidationResult(
    val amountError: AmountError? = null,
    val categoryError: CategoryError? = null,
    val dateError: DateError? = null,
    val noteError: NoteError? = null
) {
    val isValid: Boolean
        get() = amountError == null && categoryError == null && dateError == null && noteError == null
}

class ValidateTransactionInputUseCase @Inject constructor() {

    operator fun invoke(
        amountInput: String,
        category: Category?,
        type: TransactionType,
        date: LocalDate,
        today: LocalDate
    ): ValidationResult {
        return invoke(amountInput, category, type, date, "", today)
    }

    operator fun invoke(
        amountInput: String,
        category: Category?,
        type: TransactionType,
        date: LocalDate,
        note: String,
        today: LocalDate
    ): ValidationResult {
        val amountError = validateAmount(amountInput)
        val categoryError = validateCategory(category, type)
        val dateError = validateDate(date, today)
        val noteError = validateNote(note)

        return ValidationResult(
            amountError = amountError,
            categoryError = categoryError,
            dateError = dateError,
            noteError = noteError
        )
    }

    private fun validateAmount(amountInput: String): AmountError? {
        val trimmed = amountInput.trim()
        if (trimmed.isEmpty()) return AmountError.Empty
        
        val value = trimmed.toLongOrNull()
        if (value == null) return AmountError.TooLarge // either too large or not a number, but UI restricts to digits
        if (value <= 0) return AmountError.NotPositive
        if (value > 999_999_999_999L) return AmountError.TooLarge
        
        return null
    }

    private fun validateCategory(category: Category?, type: TransactionType): CategoryError? {
        if (category == null) return CategoryError.Missing
        if (category.type != type) return CategoryError.WrongType
        return null
    }

    private fun validateDate(date: LocalDate, today: LocalDate): DateError? {
        if (date.isAfter(today)) return DateError.InFuture
        return null
    }

    private fun validateNote(note: String): NoteError? {
        if (note.length > 100) return NoteError.TooLong
        return null
    }
}
