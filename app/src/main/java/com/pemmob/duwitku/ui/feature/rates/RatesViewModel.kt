package com.pemmob.duwitku.ui.feature.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.ExchangeRate
import com.pemmob.duwitku.domain.model.RateException
import com.pemmob.duwitku.domain.repository.RateRepository
import com.pemmob.duwitku.util.CurrencyFormatter
import com.pemmob.duwitku.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RatesViewModel @Inject constructor(
    private val repository: RateRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _errorResId = MutableStateFlow<Int?>(null)
    
    private val _lastDate = MutableStateFlow<LocalDate?>(null)
    private val _rates = MutableStateFlow<List<ExchangeRate>>(emptyList())
    
    private val _amountInput = MutableStateFlow("100000")

    val uiState: StateFlow<RatesUiState> = combine(
        _isLoading, _errorResId, _lastDate, _rates, _amountInput
    ) { loading, errorResId, date, rates, amountInput ->
        when {
            loading -> RatesUiState.Loading
            errorResId != null -> RatesUiState.Error(errorResId)
            date != null && rates.isNotEmpty() -> {
                val inputVal = amountInput.toLongOrNull() ?: 0L
                val items = rates.map { rate ->
                    val nameResId = getCurrencyNameRes(rate.currencyCode)
                    val converted = inputVal / rate.rateToIdr
                    val rateText = "1 ${rate.currencyCode} = ${CurrencyFormatter.formatRp(rate.rateToIdr.toLong())}"
                    
                    // Custom formatting for conversion result: max 2 decimals, except JPY usually doesn't have decimals.
                    // But to keep it simple, we use a basic String format.
                    val convertedStr = java.lang.String.format(java.util.Locale.US, "%.2f", converted)
                        .replace(".00", "") // clean up integers
                        
                    RateItem(
                        currencyCode = rate.currencyCode,
                        nameResId = nameResId,
                        rateText = rateText,
                        convertedAmount = convertedStr
                    )
                }
                RatesUiState.Success(
                    dateString = DateFormatter.formatFull(date),
                    amountInput = amountInput,
                    items = items
                )
            }
            else -> RatesUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RatesUiState.Loading
    )

    init {
        loadRates()
    }

    fun loadRates() {
        _isLoading.value = true
        _errorResId.value = null
        
        viewModelScope.launch {
            val result = repository.getRates()
            result.onSuccess { ratesResult ->
                _lastDate.value = ratesResult.date
                _rates.value = ratesResult.rates
                _isLoading.value = false
            }.onFailure { e ->
                _errorResId.value = mapErrorToResId(e)
                _isLoading.value = false
            }
        }
    }

    fun onAmountChange(newAmount: String) {
        val digitsOnly = newAmount.filter { it.isDigit() }
        val noLeadingZeros = if (digitsOnly.length > 1 && digitsOnly.startsWith("0")) {
            digitsOnly.dropWhile { it == '0' }.ifEmpty { "0" }
        } else {
            digitsOnly
        }
        val finalAmount = if (noLeadingZeros.length > 12) noLeadingZeros.take(12) else noLeadingZeros
        _amountInput.value = finalAmount
    }
    
    private fun mapErrorToResId(e: Throwable): Int {
        return when (e) {
            is RateException.NoInternetException -> R.string.error_no_internet
            is RateException.TimeoutException -> R.string.error_timeout
            is RateException.ServerException -> R.string.error_server
            is RateException.InvalidDataException -> R.string.error_invalid_data
            else -> R.string.error_generic
        }
    }
    
    private fun getCurrencyNameRes(code: String): Int {
        return when (code.uppercase()) {
            "USD" -> R.string.currency_usd
            "EUR" -> R.string.currency_eur
            "SGD" -> R.string.currency_sgd
            "JPY" -> R.string.currency_jpy
            "MYR" -> R.string.currency_myr
            "AUD" -> R.string.currency_aud
            else -> R.string.error_generic
        }
    }
}
