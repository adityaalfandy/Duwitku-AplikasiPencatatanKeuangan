package com.pemmob.duwitku.ui.feature.transactions

import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.DateGroup
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.FilterTransactionsUseCase
import com.pemmob.duwitku.domain.usecase.GroupTransactionsByDateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    private lateinit var viewModel: TransactionListViewModel
    private lateinit var fakeRepository: FakeTransactionRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTransactionRepository()
        viewModel = TransactionListViewModel(
            repository = fakeRepository,
            filterTransactionsUseCase = FilterTransactionsUseCase(),
            groupTransactionsByDateUseCase = GroupTransactionsByDateUseCase()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Success`() = testScope.runTest {
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        
        // Let flow emit
        advanceUntilIdle()
        
        // Loading is initial value for stateIn, then Success (since fakeRepository has empty list)
        assertTrue(states.first() is TransactionListUiState.Loading)
        assertTrue(states.last() is TransactionListUiState.Success)
        
        job.cancel()
    }

    @Test
    fun `filter by type works correctly`() = testScope.runTest {
        val tx1 = createTransaction(1L, TransactionType.INCOME)
        val tx2 = createTransaction(2L, TransactionType.EXPENSE)
        fakeRepository.emit(listOf(tx1, tx2))
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        // Initial state has both
        var successState = states.last() as TransactionListUiState.Success
        assertEquals(2, successState.groups.sumOf { it.items.size })
        
        // Filter income
        viewModel.onTypeFilterChange(TransactionType.INCOME)
        advanceUntilIdle()
        
        successState = states.last() as TransactionListUiState.Success
        assertEquals(TransactionType.INCOME, successState.selectedType)
        assertEquals(1, successState.groups.sumOf { it.items.size })
        assertEquals(tx1.id, successState.groups.first().items.first().id)
        
        job.cancel()
    }

    @Test
    fun `search by query is case insensitive`() = testScope.runTest {
        val tx1 = createTransaction(1L, note = "Makan Siang")
        val tx2 = createTransaction(2L, note = "Beli Buku")
        fakeRepository.emit(listOf(tx1, tx2))
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        viewModel.onQueryChange("makan")
        advanceUntilIdle()
        
        val successState = states.last() as TransactionListUiState.Success
        assertEquals("makan", successState.query)
        assertEquals(1, successState.groups.sumOf { it.items.size })
        assertEquals(tx1.id, successState.groups.first().items.first().id)
        
        job.cancel()
    }

    @Test
    fun `combined filter and search`() = testScope.runTest {
        val tx1 = createTransaction(1L, TransactionType.EXPENSE, note = "Makan Siang")
        val tx2 = createTransaction(2L, TransactionType.INCOME, note = "Uang Makan")
        val tx3 = createTransaction(3L, TransactionType.EXPENSE, note = "Nonton")
        fakeRepository.emit(listOf(tx1, tx2, tx3))
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        viewModel.onTypeFilterChange(TransactionType.EXPENSE)
        viewModel.onQueryChange("makan")
        advanceUntilIdle()
        
        val successState = states.last() as TransactionListUiState.Success
        assertEquals(1, successState.groups.sumOf { it.items.size })
        assertEquals(tx1.id, successState.groups.first().items.first().id)
        
        job.cancel()
    }

    @Test
    fun `hasAnyTransaction is true when filter returns empty but repo has data`() = testScope.runTest {
        fakeRepository.emit(listOf(createTransaction(1L)))
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        viewModel.onQueryChange("does not exist")
        advanceUntilIdle()
        
        val successState = states.last() as TransactionListUiState.Success
        assertTrue(successState.hasAnyTransaction)
        assertTrue(successState.groups.isEmpty())
        
        job.cancel()
    }

    @Test
    fun `data changes in repo updates state`() = testScope.runTest {
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        assertTrue((states.last() as TransactionListUiState.Success).groups.isEmpty())
        
        fakeRepository.emit(listOf(createTransaction(1L)))
        advanceUntilIdle()
        
        assertEquals(1, (states.last() as TransactionListUiState.Success).groups.sumOf { it.items.size })
        
        job.cancel()
    }

    @Test
    fun `error from repo triggers Error state and onRetry recovers`() = testScope.runTest {
        fakeRepository.shouldThrow = true
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        assertTrue(states.last() is TransactionListUiState.Error)
        
        fakeRepository.shouldThrow = false
        viewModel.onRetry()
        advanceUntilIdle()
        
        assertTrue(states.last() is TransactionListUiState.Success)
        
        job.cancel()
    }

    @Test
    fun `date groups are ordered newest first`() = testScope.runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        
        val tx1 = createTransaction(1L, date = today)
        val tx2 = createTransaction(2L, date = yesterday)
        val tx3 = createTransaction(3L, date = tomorrow)
        
        fakeRepository.emit(listOf(tx1, tx2, tx3))
        
        val states = mutableListOf<TransactionListUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        
        val successState = states.last() as TransactionListUiState.Success
        assertEquals(3, successState.groups.size)
        assertEquals(tomorrow, successState.groups[0].date)
        assertEquals(today, successState.groups[1].date)
        assertEquals(yesterday, successState.groups[2].date)
        
        job.cancel()
    }

    private fun createTransaction(
        id: Long,
        type: TransactionType = TransactionType.EXPENSE,
        date: LocalDate = LocalDate.now(),
        note: String = ""
    ) = Transaction(
        id = id,
        type = type,
        amount = 1000L,
        category = Category.FOOD,
        date = date,
        note = note
    )
}

class FakeTransactionRepository : TransactionRepository {
    private val flow = MutableStateFlow<List<Transaction>>(emptyList())
    var shouldThrow = false
    
    fun emit(transactions: List<Transaction>) {
        flow.value = transactions
    }

    override fun observeAll(): Flow<List<Transaction>> = kotlinx.coroutines.flow.flow {
        flow.collect {
            if (shouldThrow) throw RuntimeException("Test Exception")
            emit(it)
        }
    }
    
    override fun observeRecent(limit: Int): Flow<List<Transaction>> = kotlinx.coroutines.flow.flow { emit(emptyList()) }
    
    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> = kotlinx.coroutines.flow.flow { emit(emptyList()) }
    
    override fun observeById(id: Long): Flow<Transaction?> = kotlinx.coroutines.flow.flow { emit(null) }
    
    override fun observeTotalBalance(): Flow<Long> = kotlinx.coroutines.flow.flow { emit(0L) }
    
    override suspend fun save(transaction: Transaction) {}
    
    override suspend fun delete(id: Long) {}
}
