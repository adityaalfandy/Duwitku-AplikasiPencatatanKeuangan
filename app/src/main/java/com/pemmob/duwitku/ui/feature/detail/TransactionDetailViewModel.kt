package com.pemmob.duwitku.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository
) : ViewModel() {

    // Mengambil parameter rute langsung dari SavedStateHandle agar lebih mudah ditest
    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: 0L

    private val _isDeleting = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)
    private val _deleteError = MutableStateFlow(false)

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        repository.observeById(transactionId),
        _isDeleting,
        _isDeleted,
        _deleteError
    ) { transaction, isDeleting, isDeleted, deleteError ->
        val detailState = when {
            isDeleting || isDeleted -> {
                // JEBAKAN: Cegah NotFound saat sedang/sudah dihapus.
                // Saat repository memancarkan null akibat dihapus, cegah transisi ke NotFound.
                if (transaction != null) {
                    TransactionDetailState.Success(transaction)
                } else {
                    TransactionDetailState.Loading
                }
            }
            transaction == null -> TransactionDetailState.NotFound
            else -> TransactionDetailState.Success(transaction)
        }

        TransactionDetailUiState(
            detailState = detailState,
            isDeleting = isDeleting,
            isDeleted = isDeleted,
            deleteError = deleteError
        )
    }.catch {
        emit(
            TransactionDetailUiState(
                detailState = TransactionDetailState.Error
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionDetailUiState()
    )

    fun onDeleteConfirmed() {
        if (_isDeleting.value || _isDeleted.value) return
        
        _isDeleting.value = true
        _deleteError.value = false
        
        viewModelScope.launch {
            try {
                repository.delete(transactionId)
                _isDeleted.value = true
                _isDeleting.value = false
            } catch (e: CancellationException) {
                _isDeleting.value = false
                throw e
            } catch (e: Exception) {
                _isDeleting.value = false
                _deleteError.value = true
            }
        }
    }

    fun onDeleteErrorShown() {
        _deleteError.value = false
    }
}
