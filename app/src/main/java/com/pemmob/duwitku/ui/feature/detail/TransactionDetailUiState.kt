package com.pemmob.duwitku.ui.feature.detail

import com.pemmob.duwitku.domain.model.Transaction

sealed interface TransactionDetailState {
    data object Loading : TransactionDetailState
    data class Success(val transaction: Transaction) : TransactionDetailState
    data object NotFound : TransactionDetailState
    data object Error : TransactionDetailState
}

data class TransactionDetailUiState(
    val detailState: TransactionDetailState = TransactionDetailState.Loading,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val deleteError: Boolean = false
)
