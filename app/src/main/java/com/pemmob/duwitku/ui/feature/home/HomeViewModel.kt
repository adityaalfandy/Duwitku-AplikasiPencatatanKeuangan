package com.pemmob.duwitku.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.GetMonthSummaryUseCase
import com.pemmob.duwitku.domain.usecase.GetTotalBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val getTotalBalance: GetTotalBalanceUseCase,
    private val getMonthSummary: GetMonthSummaryUseCase,
    private val clock: Clock
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = retryTrigger.flatMapLatest {
        repository.observeAll().map { transactions ->
            val currentMonth = YearMonth.now(clock)
            val balance = getTotalBalance(transactions)
            val monthSummary = getMonthSummary(transactions, currentMonth)
            
            val recent = transactions.take(5)
            
            HomeUiState.Success(
                month = currentMonth,
                balance = balance,
                monthIncome = monthSummary.totalIncome,
                monthExpense = monthSummary.totalExpense,
                recent = recent
            ) as HomeUiState
        }.catch { e ->
            if (e is CancellationException) throw e
            emit(HomeUiState.Error)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun onRetry() {
        retryTrigger.value += 1
    }
}
