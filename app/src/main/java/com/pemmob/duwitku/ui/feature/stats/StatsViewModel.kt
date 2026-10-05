package com.pemmob.duwitku.ui.feature.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.GetCategoryTotalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

private const val SELECTED_MONTH_KEY = "selected_month"

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    private val getCategoryTotalsUseCase: GetCategoryTotalsUseCase,
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = YearMonth.now(clock)

    private val _selectedMonth = MutableStateFlow(
        savedStateHandle.get<String>(SELECTED_MONTH_KEY)?.let { YearMonth.parse(it) } ?: currentMonth
    )

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = kotlinx.coroutines.flow.combine(_selectedMonth, retryTrigger) { month, _ -> month }
        .flatMapLatest { month ->
            repository.observeByMonth(month).map { transactions ->
                val items = getCategoryTotalsUseCase(transactions, month)
                val totalExpense = items.sumOf { it.total }
                StatsUiState.Success(
                    month = month,
                    totalExpense = totalExpense,
                    items = items,
                    canGoNext = month < YearMonth.now(clock)
                ) as StatsUiState
            }.catch { e ->
                if (e is CancellationException) throw e
                emit(StatsUiState.Error)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState.Loading
        )

    fun onPreviousMonth() {
        val newMonth = _selectedMonth.value.minusMonths(1)
        _selectedMonth.value = newMonth
        savedStateHandle[SELECTED_MONTH_KEY] = newMonth.toString()
    }

    fun onNextMonth() {
        val newMonth = _selectedMonth.value.plusMonths(1)
        if (newMonth <= YearMonth.now(clock)) {
            _selectedMonth.value = newMonth
            savedStateHandle[SELECTED_MONTH_KEY] = newMonth.toString()
        }
    }

    fun onRetry() {
        retryTrigger.value++
    }
}
