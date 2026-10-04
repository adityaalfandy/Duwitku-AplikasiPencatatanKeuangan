package com.pemmob.duwitku.ui.feature.rates

import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.ExchangeRate
import com.pemmob.duwitku.domain.model.RateException
import com.pemmob.duwitku.domain.model.RatesResult
import com.pemmob.duwitku.domain.repository.RateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RatesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private class FakeRateRepository(
        private val resultToReturn: Result<RatesResult>
    ) : RateRepository {
        var callCount = 0
            private set

        override suspend fun getRates(): Result<RatesResult> {
            callCount++
            return resultToReturn
        }
    }

    @Test
    fun loadRates_success_updatesUiStateToSuccess() = runTest {
        val date = LocalDate.of(2026, 10, 5)
        val rates = listOf(
            ExchangeRate("USD", 15000.0),
            ExchangeRate("AUD", 10000.0)
        )
        val fakeRepo = FakeRateRepository(Result.success(RatesResult(date, rates)))
        
        val viewModel = RatesViewModel(fakeRepo)
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        
        assertTrue(viewModel.uiState.value is RatesUiState.Loading)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is RatesUiState.Success)
        
        val successState = state as RatesUiState.Success
        assertEquals("100000", successState.amountInput)
        assertEquals(2, successState.items.size)
        
        val usdItem = successState.items.find { it.currencyCode == "USD" }
        assertEquals(R.string.currency_usd, usdItem?.nameResId)
        assertEquals("1 USD = Rp 15.000", usdItem?.rateText)
        assertEquals("6.67", usdItem?.convertedAmount) // 100000 / 15000 = 6.666...
        
        assertEquals(1, fakeRepo.callCount)
        job.cancel()
    }

    @Test
    fun loadRates_error_updatesUiStateToError() = runTest {
        val fakeRepo = FakeRateRepository(Result.failure(RateException.NoInternetException()))
        val viewModel = RatesViewModel(fakeRepo)
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is RatesUiState.Error)
        assertEquals(R.string.error_no_internet, (state as RatesUiState.Error).messageResId)
        job.cancel()
    }

    @Test
    fun onAmountChange_updatesAmountAndRecalculates_withoutCallingRepo() = runTest {
        val date = LocalDate.of(2026, 10, 5)
        val rates = listOf(ExchangeRate("USD", 15000.0))
        val fakeRepo = FakeRateRepository(Result.success(RatesResult(date, rates)))
        
        val viewModel = RatesViewModel(fakeRepo)
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        
        advanceUntilIdle()
        
        assertEquals(1, fakeRepo.callCount)
        
        viewModel.onAmountChange("30000")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as RatesUiState.Success
        assertEquals("30000", state.amountInput)
        
        val usdItem = state.items.first()
        assertEquals("2", usdItem.convertedAmount) // 30000 / 15000 = 2
        
        // Repo should not be called again
        assertEquals(1, fakeRepo.callCount)
        job.cancel()
    }
}
