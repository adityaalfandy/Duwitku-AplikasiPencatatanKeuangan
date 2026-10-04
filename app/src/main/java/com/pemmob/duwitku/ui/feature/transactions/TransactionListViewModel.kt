package com.pemmob.duwitku.ui.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.FilterTransactionsUseCase
import com.pemmob.duwitku.domain.usecase.GroupTransactionsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val groupTransactionsByDateUseCase: GroupTransactionsByDateUseCase
) : ViewModel() {

    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    private val _query = MutableStateFlow("")
    private val _retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<TransactionListUiState> = _retryTrigger.flatMapLatest {
        combine(
            repository.observeAll(),
            _selectedType,
            _query
        ) { transactions, selectedType, query ->
        val hasAnyTransaction = transactions.isNotEmpty()
        val filtered = filterTransactionsUseCase(transactions, selectedType, query)
        val grouped = groupTransactionsByDateUseCase(filtered)
        
        TransactionListUiState.Success(
            groups = grouped,
            selectedType = selectedType,
            query = query,
            hasAnyTransaction = hasAnyTransaction
        ) as TransactionListUiState
        }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(TransactionListUiState.Error)
        }
    }
        .onStart { emit(TransactionListUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState.Loading
        )

    fun onTypeFilterChange(type: TransactionType?) {
        _selectedType.update { type }
    }

    fun onQueryChange(newQuery: String) {
        _query.update { newQuery }
    }

    fun onRetry() {
        _retryTrigger.update { it + 1 }
    }
}
