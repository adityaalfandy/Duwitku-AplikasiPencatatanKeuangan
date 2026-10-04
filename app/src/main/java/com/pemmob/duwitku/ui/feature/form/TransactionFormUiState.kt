package com.pemmob.duwitku.ui.feature.form

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.usecase.AmountError
import com.pemmob.duwitku.domain.usecase.CategoryError
import com.pemmob.duwitku.domain.usecase.DateError
import com.pemmob.duwitku.domain.usecase.NoteError
import java.time.LocalDate

data class TransactionFormUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val category: Category? = null,
    val date: LocalDate, // Set initially from Clock in ViewModel
    val note: String = "",
    
    val amountError: AmountError? = null,
    val categoryError: CategoryError? = null,
    val dateError: DateError? = null,
    val noteError: NoteError? = null,
    
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val loadError: Boolean = false,
    val saveError: Boolean = false
)
