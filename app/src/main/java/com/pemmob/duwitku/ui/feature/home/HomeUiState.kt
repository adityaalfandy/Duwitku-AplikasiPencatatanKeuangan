package com.pemmob.duwitku.ui.feature.home

import com.pemmob.duwitku.domain.model.Transaction
import java.time.YearMonth

sealed interface HomeUiState {
    data object Loading : HomeUiState
    
    data class Success(
        val month: YearMonth,
        val balance: Long,
        val monthIncome: Long,
        val monthExpense: Long,
        val recent: List<Transaction>
    ) : HomeUiState
    
    data object Error : HomeUiState
}
