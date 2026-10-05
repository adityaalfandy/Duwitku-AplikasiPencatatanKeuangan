package com.pemmob.duwitku.ui.feature.stats

import androidx.lifecycle.SavedStateHandle
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.GetCategoryTotalsUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    // Fixed clock for testing: 2026-10-15T12:00:00Z -> YearMonth: 2026-10
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-10-15T12:00:00Z"),
        ZoneId.of("UTC")
    )

    private val getCategoryTotalsUseCase = GetCategoryTotalsUseCase()

    private class FakeTransactionRepository : TransactionRepository {
        val flowData = MutableStateFlow<List<Transaction>>(emptyList())
        var shouldThrowError = false

        override suspend fun save(transaction: Transaction) {}
        override suspend fun delete(id: Long) {}
        
        override fun observeAll(): Flow<List<Transaction>> = flowData
        
        override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> = flow {
            if (shouldThrowError) {
                throw RuntimeException("Database error")
            }
            // Simple filtering for test
            flowData.collect { list ->
                emit(list.filter { YearMonth.from(it.date) == month })
            }
        }
        
        override fun observeById(id: Long): Flow<Transaction?> = flow { emit(null) }
        
        override fun observeRecent(limit: Int): Flow<List<Transaction>> = flow { emit(emptyList()) }
        
        override fun observeTotalBalance(): Flow<Long> = flow { emit(0L) }
    }

    private lateinit var repository: FakeTransactionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTransactionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): StatsViewModel {
        return StatsViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository,
            getCategoryTotalsUseCase = getCategoryTotalsUseCase,
            clock = fixedClock
        )
    }

    @Test
    fun `initial state is Success with empty items when no expenses in current month`() = runTest {
        val viewModel = createViewModel()
        
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(YearMonth.of(2026, 10), state.month)
        assertEquals(0L, state.totalExpense)
        assertTrue(state.items.isEmpty())
        assertFalse(state.canGoNext)
    }

    @Test
    fun `calculate total and items correctly, ignoring incomes`() = runTest {
        repository.flowData.value = listOf(
            Transaction(1, TransactionType.EXPENSE, 100_000, Category.FOOD, LocalDate.of(2026, 10, 1), ""),
            Transaction(2, TransactionType.EXPENSE, 50_000, Category.FOOD, LocalDate.of(2026, 10, 5), ""),
            Transaction(3, TransactionType.EXPENSE, 200_000, Category.TRANSPORT, LocalDate.of(2026, 10, 10), ""),
            Transaction(4, TransactionType.INCOME, 500_000, Category.SALARY, LocalDate.of(2026, 10, 2), "") // Should be ignored
        )
        
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(350_000L, state.totalExpense)
        assertEquals(2, state.items.size)
        // Transport is 200k, Food is 150k -> Transport should be first
        assertEquals(Category.TRANSPORT, state.items[0].category)
        assertEquals(200_000L, state.items[0].total)
        assertEquals(Category.FOOD, state.items[1].category)
        assertEquals(150_000L, state.items[1].total)
    }

    @Test
    fun `onPreviousMonth updates month and canGoNext`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        viewModel.onPreviousMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(YearMonth.of(2026, 9), state.month)
        assertTrue(state.canGoNext)
    }

    @Test
    fun `onNextMonth does not exceed current month`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        // Currently at 2026-10
        viewModel.onNextMonth() // Should not go to 2026-11
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(YearMonth.of(2026, 10), state.month)
        assertFalse(state.canGoNext)
    }

    @Test
    fun `navigate year boundary (Jan to Dec previous year)`() = runTest {
        // Set initial state to Jan 2026
        val savedStateHandle = SavedStateHandle(mapOf("selected_month" to "2026-01"))
        val viewModel = createViewModel(savedStateHandle)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        viewModel.onPreviousMonth()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(YearMonth.of(2025, 12), state.month)
    }

    @Test
    fun `repository data changes updates state`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        var state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(0L, state.totalExpense)
        
        repository.flowData.value = listOf(
            Transaction(1, TransactionType.EXPENSE, 100_000, Category.FOOD, LocalDate.of(2026, 10, 1), "")
        )
        advanceUntilIdle()
        
        state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(100_000L, state.totalExpense)
    }

    @Test
    fun `flow error returns Error state then onRetry recovers`() = runTest {
        repository.shouldThrowError = true
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is StatsUiState.Error)
        
        // Fix error and retry
        repository.shouldThrowError = false
        viewModel.onRetry()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is StatsUiState.Success)
    }
}
