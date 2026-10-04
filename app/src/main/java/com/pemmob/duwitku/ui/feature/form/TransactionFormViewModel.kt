package com.pemmob.duwitku.ui.feature.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.ValidateTransactionInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    private val validateInput: ValidateTransactionInputUseCase,
    private val clock: Clock
) : ViewModel() {

    private val transactionId: Long?
    private val initialType: TransactionType
    
    private val _uiState: MutableStateFlow<TransactionFormUiState>

    val uiState: StateFlow<TransactionFormUiState>

    init {
        // Ambil argumen secara internal untuk isolasi pengujian (karena SavedStateHandle.toRoute bermasalah di JVM test biasa)
        val argId: Long? = savedStateHandle.get<Long>("transactionId")
        val argType: String? = savedStateHandle.get<String>("type")
        
        transactionId = argId
        initialType = parseInitialType(argType)

        _uiState = MutableStateFlow(
            TransactionFormUiState(
                type = initialType,
                date = LocalDate.now(clock),
                isEditMode = transactionId != null
            )
        )
        uiState = _uiState.asStateFlow()

        if (transactionId != null) {
            loadTransaction(transactionId)
        }
    }

    private fun loadTransaction(id: Long) {
        _uiState.update { it.copy(isLoading = true, loadError = false) }
        viewModelScope.launch {
            try {
                val transaction = repository.observeById(id).first()
                if (transaction != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            type = transaction.type,
                            amountInput = transaction.amount.toString(),
                            category = transaction.category,
                            date = transaction.date,
                            note = transaction.note
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, loadError = true) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, loadError = true) }
            }
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { state ->
            val newCategory = if (state.category?.type == type) state.category else null
            state.copy(
                type = type,
                category = newCategory,
                categoryError = null
            )
        }
    }

    fun onAmountChange(amount: String) {
        // Saring hanya digit
        var filtered = amount.filter { it.isDigit() }
        // Hapus nol di depan
        filtered = filtered.trimStart('0')
        // Maks 12 digit
        if (filtered.length > 12) {
            filtered = filtered.take(12)
        }
        
        _uiState.update { 
            it.copy(
                amountInput = filtered,
                amountError = null
            )
        }
    }

    fun onCategorySelect(category: Category) {
        _uiState.update { 
            it.copy(
                category = category,
                categoryError = null
            )
        }
    }

    fun onDateSelect(date: LocalDate) {
        _uiState.update { 
            it.copy(
                date = date,
                dateError = null
            )
        }
    }

    fun onNoteChange(note: String) {
        val trimmedNote = if (note.length > 100) note.take(100) else note
        _uiState.update { 
            it.copy(
                note = trimmedNote,
                noteError = null
            )
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSaving || state.isSaved) return

        val validation = validateInput(
            amountInput = state.amountInput,
            category = state.category,
            type = state.type,
            date = state.date,
            note = state.note,
            today = LocalDate.now(clock)
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    amountError = validation.amountError,
                    categoryError = validation.categoryError,
                    dateError = validation.dateError,
                    noteError = validation.noteError
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = false) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = transactionId ?: 0L,
                    type = state.type,
                    amount = state.amountInput.toLong(),
                    category = state.category!!,
                    date = state.date,
                    note = state.note.trim()
                )
                repository.save(transaction)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = true) }
            }
        }
    }

    fun onSaveErrorShown() {
        _uiState.update { it.copy(saveError = false) }
    }

    companion object {
        fun parseInitialType(raw: String?): TransactionType {
            if (raw == null) return TransactionType.EXPENSE
            return try {
                TransactionType.valueOf(raw)
            } catch (e: IllegalArgumentException) {
                TransactionType.EXPENSE
            }
        }
    }
}
