package com.pemmob.duwitku.ui.feature.transactions

import com.pemmob.duwitku.domain.model.DateGroup
import com.pemmob.duwitku.domain.model.TransactionType

sealed interface TransactionListUiState {
    data object Loading : TransactionListUiState
    
    data class Success(
        val groups: List<DateGroup>,
        val selectedType: TransactionType?,
        val query: String,
        val hasAnyTransaction: Boolean
    ) : TransactionListUiState
    
    data object Error : TransactionListUiState
}
