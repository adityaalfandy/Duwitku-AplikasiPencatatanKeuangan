package com.pemmob.duwitku.ui.feature.stats

import com.pemmob.duwitku.domain.model.CategoryTotal
import java.time.YearMonth

sealed interface StatsUiState {
    data object Loading : StatsUiState
    
    data class Success(
        val month: YearMonth,
        val totalExpense: Long,
        val items: List<CategoryTotal>,
        val canGoNext: Boolean
    ) : StatsUiState
    
    data object Error : StatsUiState
}
