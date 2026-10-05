package com.pemmob.duwitku.ui.feature.home

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.GetMonthSummaryUseCase
import com.pemmob.duwitku.domain.usecase.GetTotalBalanceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    // Fixed clock for testing: 2026-02-15T12:00:00Z
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-02-15T12:00:00Z"),
        ZoneId.of("UTC")
    )

    private val getTotalBalance = GetTotalBalanceUseCase()
    private val getMonthSummary = GetMonthSummaryUseCase()

    private class FakeTransactionRepository : TransactionRepository {
        val flow = MutableStateFlow<List<Transaction>>(emptyList())
        var shouldThrowError = false
        
        override fun observeAll(): Flow<List<Transaction>> = flow {
            flow.collect {
                if (shouldThrowError) {
                    throw Exception("Fake exception")
                }
                emit(it)
            }
        }
        
        override fun observeRecent(limit: Int): Flow<List<Transaction>> = flow {}
        override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> = flow {}
        override fun observeById(id: Long): Flow<Transaction?> = flow {}
        override fun observeTotalBalance(): Flow<Long> = flow {}
        override suspend fun save(transaction: Transaction) {}
        override suspend fun delete(id: Long) {}
    }

    private lateinit var repository: FakeTransactionRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTransactionRepository()
        viewModel = HomeViewModel(
            repository = repository,
            getTotalBalance = getTotalBalance,
            getMonthSummary = getMonthSummary,
            clock = fixedClock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTx(
        id: Long,
        amount: Long,
        type: TransactionType,
        dateStr: String
    ): Transaction {
        return Transaction(
            id = id,
            type = type,
            amount = amount,
            category = Category.OTHER_EXPENSE,
            date = LocalDate.parse(dateStr),
            note = ""
        )
    }

    @Test
    fun `empty list returns Success with zeros`() = runTest {
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        
        advanceUntilIdle()
        val state = viewModel.uiState.value
        
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        
        assertEquals(YearMonth.of(2026, 2), success.month)
        assertEquals(0L, success.balance)
        assertEquals(0L, success.monthIncome)
        assertEquals(0L, success.monthExpense)
        assertTrue(success.recent.isEmpty())
        
        collectJob.cancel()
    }

    @Test
    fun `balance calculates all time, month summary only current month`() = runTest {
        val txOld = createTx(1, 10000, TransactionType.INCOME, "2026-01-31")
        val txFeb = createTx(2, 5000, TransactionType.EXPENSE, "2026-02-01")
        val txFebLeap = createTx(3, 2000, TransactionType.EXPENSE, "2024-02-29") // Different year, so ignored for month summary
        val txFebLeapCurrent = createTx(4, 3000, TransactionType.EXPENSE, "2026-02-28")
        
        repository.flow.value = listOf(txOld, txFeb, txFebLeap, txFebLeapCurrent)
        
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        val success = viewModel.uiState.value as HomeUiState.Success
        
        // Balance all time: 10000 - 5000 - 2000 - 3000 = 0
        assertEquals(0L, success.balance)
        
        // Month summary for 2026-02 (txFeb, txFebLeapCurrent)
        // Income = 0, Expense = 5000 + 3000 = 8000
        assertEquals(0L, success.monthIncome)
        assertEquals(8000L, success.monthExpense)
        
        collectJob.cancel()
    }

    @Test
    fun `recent takes max 5 transactions`() = runTest {
        val list = (1..10L).map { i ->
            createTx(i, 1000 * i, TransactionType.EXPENSE, "2026-02-15")
        }
        repository.flow.value = list
        
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        val success = viewModel.uiState.value as HomeUiState.Success
        assertEquals(5, success.recent.size)
        assertEquals(1L, success.recent[0].id)
        
        collectJob.cancel()
    }

    @Test
    fun `data changes update state`() = runTest {
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        var success = viewModel.uiState.value as HomeUiState.Success
        assertEquals(0L, success.balance)
        
        repository.flow.value = listOf(createTx(1, 10000, TransactionType.INCOME, "2026-02-15"))
        advanceUntilIdle()
        
        success = viewModel.uiState.value as HomeUiState.Success
        assertEquals(10000L, success.balance)
        
        collectJob.cancel()
    }

    @Test
    fun `flow error returns Error state and onRetry recovers`() = runTest {
        repository.shouldThrowError = true
        
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is HomeUiState.Error)
        
        // Recover
        repository.shouldThrowError = false
        viewModel.onRetry()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
        
        collectJob.cancel()
    }

    @Test
    fun `negative balance works without crash`() = runTest {
        val txOld = createTx(1, 10000, TransactionType.EXPENSE, "2026-02-15")
        repository.flow.value = listOf(txOld)
        
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        val success = viewModel.uiState.value as HomeUiState.Success
        assertEquals(-10000L, success.balance)
        
        collectJob.cancel()
    }
}
