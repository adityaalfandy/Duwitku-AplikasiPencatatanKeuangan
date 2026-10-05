package com.pemmob.duwitku.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.repository.TransactionRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeTransactionRepository : TransactionRepository {
        var simulateDeleteError = false
        val transactionFlow = MutableStateFlow<Transaction?>(null)
        
        override fun observeAll(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeRecent(limit: Int): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> = flowOf(emptyList())
        
        override fun observeById(id: Long): Flow<Transaction?> = transactionFlow
        
        override fun observeTotalBalance(): Flow<Long> = flowOf(0L)
        
        override suspend fun save(transaction: Transaction) {}
        
        override suspend fun delete(id: Long) {
            if (simulateDeleteError) throw Exception("Fake delete error")
            transactionFlow.value = null // simulasi terhapus
        }
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

    private fun TestScope.createViewModel(transactionId: Long): TransactionDetailViewModel {
        val handle = SavedStateHandle(mapOf("transactionId" to transactionId))
        val vm = TransactionDetailViewModel(handle, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect()
        }
        return vm
    }

    @Test
    fun `memuat sukses`() = runTest {
        val tx = Transaction(99L, TransactionType.INCOME, 5000L, Category.SALARY, LocalDate.now(), "Test")
        repository.transactionFlow.value = tx
        
        val viewModel = createViewModel(99L)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.detailState is TransactionDetailState.Success)
        assertEquals(tx, (state.detailState as TransactionDetailState.Success).transaction)
    }

    @Test
    fun `id tidak ada memunculkan NotFound`() = runTest {
        repository.transactionFlow.value = null
        val viewModel = createViewModel(99L)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.detailState is TransactionDetailState.NotFound)
    }

    @Test
    fun `hapus sukses isDeleted true dan TIDAK pernah memunculkan NotFound`() = runTest {
        val tx = Transaction(99L, TransactionType.INCOME, 5000L, Category.SALARY, LocalDate.now(), "Test")
        repository.transactionFlow.value = tx
        
        val viewModel = createViewModel(99L)
        advanceUntilIdle() // State menjadi Success
        
        viewModel.onDeleteConfirmed()
        
        // Menunggu operasi selesai
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.isDeleted)
        assertFalse(state.isDeleting)
        // Karena dicegah, detailState BUKAN NotFound
        assertFalse(state.detailState is TransactionDetailState.NotFound)
    }

    @Test
    fun `hapus gagal memunculkan deleteError`() = runTest {
        val tx = Transaction(99L, TransactionType.INCOME, 5000L, Category.SALARY, LocalDate.now(), "Test")
        repository.transactionFlow.value = tx
        repository.simulateDeleteError = true
        
        val viewModel = createViewModel(99L)
        advanceUntilIdle()
        
        viewModel.onDeleteConfirmed()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.deleteError)
        assertFalse(state.isDeleted)
        assertFalse(state.isDeleting)
    }

    @Test
    fun `data berubah state ikut berubah`() = runTest {
        val tx1 = Transaction(99L, TransactionType.INCOME, 5000L, Category.SALARY, LocalDate.now(), "Awal")
        repository.transactionFlow.value = tx1
        val viewModel = createViewModel(99L)
        advanceUntilIdle()
        
        val tx2 = tx1.copy(note = "Berubah")
        repository.transactionFlow.value = tx2
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        val successState = state.detailState as TransactionDetailState.Success
        assertEquals("Berubah", successState.transaction.note)
    }
}
