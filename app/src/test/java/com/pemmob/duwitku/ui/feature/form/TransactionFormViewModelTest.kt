package com.pemmob.duwitku.ui.feature.form

import androidx.lifecycle.SavedStateHandle
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.domain.usecase.AmountError
import com.pemmob.duwitku.domain.usecase.CategoryError
import com.pemmob.duwitku.domain.usecase.ValidateTransactionInputUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    // Fixed clock for testing: 2026-01-15T12:00:00Z
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-01-15T12:00:00Z"),
        ZoneId.of("UTC")
    )
    
    private val useCase = ValidateTransactionInputUseCase()

    private class FakeTransactionRepository : TransactionRepository {
        var simulateSaveError = false
        val savedTransactions = mutableListOf<Transaction>()
        var transactionToReturn: Transaction? = null
        
        override fun observeAll(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeRecent(limit: Int): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> = flowOf(emptyList())
        
        override fun observeById(id: Long): Flow<Transaction?> {
            return flowOf(transactionToReturn)
        }
        
        override fun observeTotalBalance(): Flow<Long> = flowOf(0L)
        
        override suspend fun save(transaction: Transaction) {
            if (simulateSaveError) throw Exception("Fake save error")
            savedTransactions.add(transaction)
        }
        
        override suspend fun delete(id: Long) {}
    }

    private lateinit var repository: FakeTransactionRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTransactionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        transactionId: Long? = null,
        type: String? = null
    ): TransactionFormViewModel {
        val handle = SavedStateHandle().apply {
            if (transactionId != null) set("transactionId", transactionId)
            if (type != null) set("type", type)
        }
        return TransactionFormViewModel(handle, repository, useCase, fixedClock)
    }

    @Test
    fun `parseInitialType parses correctly`() {
        assertEquals(TransactionType.INCOME, TransactionFormViewModel.parseInitialType("INCOME"))
        assertEquals(TransactionType.EXPENSE, TransactionFormViewModel.parseInitialType("EXPENSE"))
        // null or invalid defaults to EXPENSE
        assertEquals(TransactionType.EXPENSE, TransactionFormViewModel.parseInitialType(null))
        assertEquals(TransactionType.EXPENSE, TransactionFormViewModel.parseInitialType("INVALID"))
    }

    @Test
    fun `initial state is correct for new transaction`() = runTest {
        val viewModel = createViewModel(type = "INCOME")
        
        val state = viewModel.uiState.value
        assertEquals(TransactionType.INCOME, state.type)
        assertEquals(LocalDate.of(2026, 1, 15), state.date)
        assertFalse(state.isEditMode)
        assertNull(state.category)
    }

    @Test
    fun `changing type resets category if incompatible`() = runTest {
        val viewModel = createViewModel(type = "EXPENSE")
        
        viewModel.onCategorySelect(Category.FOOD) // FOOD is EXPENSE
        assertEquals(Category.FOOD, viewModel.uiState.value.category)
        
        viewModel.onTypeChange(TransactionType.INCOME)
        // FOOD is incompatible with INCOME, should be reset to null
        assertNull(viewModel.uiState.value.category)
    }

    @Test
    fun `validation errors appear on save and disappear on change`() = runTest {
        val viewModel = createViewModel()
        
        // Coba simpan kosong
        viewModel.onSaveClick()
        advanceUntilIdle()
        
        val stateWithError = viewModel.uiState.value
        assertEquals(AmountError.Empty, stateWithError.amountError)
        assertEquals(CategoryError.Missing, stateWithError.categoryError)
        
        // Ubah amount
        viewModel.onAmountChange("1000")
        
        val stateAfterChange = viewModel.uiState.value
        assertNull(stateAfterChange.amountError)
        assertEquals("1000", stateAfterChange.amountInput)
        // Kategori masih error
        assertEquals(CategoryError.Missing, stateAfterChange.categoryError)
    }

    @Test
    fun `successful save updates state and calls repository`() = runTest {
        val viewModel = createViewModel()
        
        viewModel.onAmountChange("50000")
        viewModel.onCategorySelect(Category.FOOD)
        // note is empty, date is today (valid)
        
        viewModel.onSaveClick()
        
        // During save
        val savingState = viewModel.uiState.value
        assertTrue(savingState.isSaving)
        
        advanceUntilIdle()
        
        // After save
        val savedState = viewModel.uiState.value
        assertFalse(savedState.isSaving)
        assertTrue(savedState.isSaved)
        
        assertEquals(1, repository.savedTransactions.size)
        val savedTx = repository.savedTransactions[0]
        assertEquals(50000L, savedTx.amount)
        assertEquals(Category.FOOD, savedTx.category)
        assertEquals(0L, savedTx.id) // 0 because new
    }

    @Test
    fun `failed save updates error state`() = runTest {
        repository.simulateSaveError = true
        val viewModel = createViewModel()
        
        viewModel.onAmountChange("50000")
        viewModel.onCategorySelect(Category.FOOD)
        
        viewModel.onSaveClick()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertTrue(state.saveError)
    }

    @Test
    fun `multiple save clicks only saves once`() = runTest {
        val viewModel = createViewModel()
        
        viewModel.onAmountChange("50000")
        viewModel.onCategorySelect(Category.FOOD)
        
        // Click 3 times consecutively
        viewModel.onSaveClick()
        viewModel.onSaveClick()
        viewModel.onSaveClick()
        
        advanceUntilIdle()
        
        assertEquals(1, repository.savedTransactions.size)
    }

    @Test
    fun `edit mode loads data and saves with original id`() = runTest {
        repository.transactionToReturn = Transaction(
            id = 99L,
            type = TransactionType.INCOME,
            amount = 75000L,
            category = Category.SALARY,
            date = LocalDate.of(2025, 12, 1),
            note = "Bonus"
        )
        
        val viewModel = createViewModel(transactionId = 99L)
        
        advanceUntilIdle()
        
        val loadedState = viewModel.uiState.value
        assertTrue(loadedState.isEditMode)
        assertFalse(loadedState.isLoading)
        assertEquals(TransactionType.INCOME, loadedState.type)
        assertEquals("75000", loadedState.amountInput)
        assertEquals(Category.SALARY, loadedState.category)
        assertEquals("Bonus", loadedState.note)
        
        // Edit amount and save
        viewModel.onAmountChange("80000")
        viewModel.onSaveClick()
        
        advanceUntilIdle()
        
        assertEquals(1, repository.savedTransactions.size)
        val savedTx = repository.savedTransactions[0]
        assertEquals(99L, savedTx.id) // Mempertahankan id 99
        assertEquals(80000L, savedTx.amount)
    }

    @Test
    fun `edit mode with nonexistent id shows load error`() = runTest {
        repository.transactionToReturn = null // Tidak ditemukan
        
        val viewModel = createViewModel(transactionId = 99L)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.loadError)
    }
}
