package com.pemmob.duwitku.ui.feature.rates

import androidx.annotation.StringRes

sealed interface RatesUiState {
    data object Loading : RatesUiState
    
    data class Success(
        val dateString: String,
        val amountInput: String,
        val items: List<RateItem>
    ) : RatesUiState
    
    data class Error(
        @StringRes val messageResId: Int
    ) : RatesUiState
}

data class RateItem(
    val currencyCode: String,
    @StringRes val nameResId: Int,
    val rateText: String, // "1 USD = Rp 15.000"
    val convertedAmount: String
)
