package com.pemmob.duwitku.data.repository

import com.pemmob.duwitku.data.local.TransactionDao
import com.pemmob.duwitku.data.local.TransactionEntity
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryImplTest {

    /**
     * FakeTransactionDao yang memakai MutableStateFlow sebagai storage in-memory.
     */
    private class FakeTransactionDao : TransactionDao {
        private val entities = MutableStateFlow<List<TransactionEntity>>(emptyList())
        private var nextId = 1L

        override fun observeAll(): Flow<List<TransactionEntity>> {
            return entities.map { list ->
                list.sortedWith(compareByDescending<TransactionEntity> { it.dateEpochDay }
                    .thenByDescending { it.createdAt })
            }
        }

        override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> {
            return observeAll().map { it.take(limit) }
        }

        override fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>> {
            return observeAll().map { list ->
                list.filter { it.dateEpochDay in start..end }
            }
        }

        override fun observeById(id: Long): Flow<TransactionEntity?> {
            return entities.map { list -> list.find { it.id == id } }
        }

        override suspend fun getById(id: Long): TransactionEntity? {
            return entities.value.find { it.id == id }
        }

        override fun observeBalance(): Flow<Long> {
            return entities.map { list ->
                list.sumOf { entity ->
                    if (entity.type == "INCOME") entity.amount else -entity.amount
                }
            }
        }

        override suspend fun upsert(entity: TransactionEntity) {
            val current = entities.value.toMutableList()
            val actualEntity = if (entity.id == 0L) {
                entity.copy(id = nextId++)
            } else {
                entity
            }
            val index = current.indexOfFirst { it.id == actualEntity.id }
            if (index >= 0) {
                current[index] = actualEntity
            } else {
                current.add(actualEntity)
            }
            entities.value = current
        }

        override suspend fun deleteById(id: Long) {
            entities.value = entities.value.filter { it.id != id }
        }
    }

    private fun createRepo(): Pair<TransactionRepositoryImpl, FakeTransactionDao> {
        val dao = FakeTransactionDao()
        return TransactionRepositoryImpl(dao) to dao
    }

    @Test
    fun `save new transaction assigns createdAt`() = runTest {
        val (repo, _) = createRepo()
        val transaction = Transaction(
            id = 0,
            type = TransactionType.EXPENSE,
            amount = 10000,
            category = Category.FOOD,
            date = LocalDate.of(2026, 1, 15),
            note = "Makan"
        )

        repo.save(transaction)

        val results = repo.observeAll().first()
        assertEquals(1, results.size)
        assertEquals(10000L, results[0].amount)
    }

    @Test
    fun `save existing transaction preserves original createdAt`() = runTest {
        val (repo, dao) = createRepo()

        // Insert entity langsung lewat dao agar bisa kontrol createdAt
        val entity = TransactionEntity(
            id = 0,
            type = "EXPENSE",
            amount = 5000,
            category = "FOOD",
            dateEpochDay = LocalDate.of(2026, 1, 1).toEpochDay(),
            note = "Original",
            createdAt = 12345L
        )
        dao.upsert(entity) // Gets id = 1

        // Sekarang edit lewat repository
        val edited = Transaction(
            id = 1,
            type = TransactionType.EXPENSE,
            amount = 8000,
            category = Category.FOOD,
            date = LocalDate.of(2026, 1, 1),
            note = "Edited"
        )
        repo.save(edited)

        // Verifikasi createdAt tetap 12345
        val savedEntity = dao.getById(1)!!
        assertEquals(12345L, savedEntity.createdAt)
        assertEquals(8000L, savedEntity.amount)
        assertEquals("Edited", savedEntity.note)
    }

    @Test
    fun `delete removes transaction`() = runTest {
        val (repo, _) = createRepo()
        val transaction = Transaction(
            id = 0, type = TransactionType.INCOME, amount = 50000,
            category = Category.SALARY, date = LocalDate.of(2026, 1, 1), note = ""
        )

        repo.save(transaction)
        assertEquals(1, repo.observeAll().first().size)

        repo.delete(1) // id assigned by FakeDao
        assertEquals(0, repo.observeAll().first().size)
    }

    @Test
    fun `observeByMonth returns only transactions in the specified month`() = runTest {
        val (repo, _) = createRepo()

        // 31 Jan 2026 — in January
        repo.save(Transaction(0, TransactionType.EXPENSE, 1000, Category.FOOD, LocalDate.of(2026, 1, 31), "Jan"))
        // 1 Feb 2026 — in February
        repo.save(Transaction(0, TransactionType.EXPENSE, 2000, Category.FOOD, LocalDate.of(2026, 2, 1), "Feb"))
        // 28 Feb 2026 — in February
        repo.save(Transaction(0, TransactionType.EXPENSE, 3000, Category.FOOD, LocalDate.of(2026, 2, 28), "Feb end"))

        val janResults = repo.observeByMonth(YearMonth.of(2026, 1)).first()
        assertEquals(1, janResults.size)
        assertEquals("Jan", janResults[0].note)

        val febResults = repo.observeByMonth(YearMonth.of(2026, 2)).first()
        assertEquals(2, febResults.size)
    }

    @Test
    fun `observeByMonth handles leap year february correctly`() = runTest {
        val (repo, _) = createRepo()

        repo.save(Transaction(0, TransactionType.EXPENSE, 1000, Category.FOOD, LocalDate.of(2024, 2, 29), "Leap"))
        repo.save(Transaction(0, TransactionType.EXPENSE, 2000, Category.FOOD, LocalDate.of(2024, 3, 1), "March"))

        val febResults = repo.observeByMonth(YearMonth.of(2024, 2)).first()
        assertEquals(1, febResults.size)
        assertEquals("Leap", febResults[0].note)
    }

    @Test
    fun `observeTotalBalance returns 0 when empty`() = runTest {
        val (repo, _) = createRepo()
        assertEquals(0L, repo.observeTotalBalance().first())
    }

    @Test
    fun `observeTotalBalance returns income minus expense`() = runTest {
        val (repo, _) = createRepo()

        repo.save(Transaction(0, TransactionType.INCOME, 100000, Category.SALARY, LocalDate.of(2026, 1, 1), ""))
        repo.save(Transaction(0, TransactionType.EXPENSE, 30000, Category.FOOD, LocalDate.of(2026, 1, 2), ""))
        repo.save(Transaction(0, TransactionType.EXPENSE, 20000, Category.TRANSPORT, LocalDate.of(2026, 1, 3), ""))

        assertEquals(50000L, repo.observeTotalBalance().first())
    }

    @Test
    fun `observeAll returns transactions ordered by date descending`() = runTest {
        val (repo, _) = createRepo()

        repo.save(Transaction(0, TransactionType.EXPENSE, 1000, Category.FOOD, LocalDate.of(2026, 1, 1), "Older"))
        repo.save(Transaction(0, TransactionType.EXPENSE, 2000, Category.FOOD, LocalDate.of(2026, 1, 3), "Newer"))
        repo.save(Transaction(0, TransactionType.EXPENSE, 3000, Category.FOOD, LocalDate.of(2026, 1, 2), "Middle"))

        val results = repo.observeAll().first()
        assertEquals(3, results.size)
        assertEquals("Newer", results[0].note)
        assertEquals("Middle", results[1].note)
        assertEquals("Older", results[2].note)
    }

    @Test
    fun `observeRecent limits results`() = runTest {
        val (repo, _) = createRepo()

        repeat(5) { i ->
            repo.save(Transaction(0, TransactionType.EXPENSE, 1000, Category.FOOD, LocalDate.of(2026, 1, i + 1), "Item $i"))
        }

        val results = repo.observeRecent(3).first()
        assertEquals(3, results.size)
    }

    @Test
    fun `observeById returns null for nonexistent id`() = runTest {
        val (repo, _) = createRepo()
        assertNull(repo.observeById(999).first())
    }

    @Test
    fun `entities with unknown type are filtered out from observeAll`() = runTest {
        val (repo, dao) = createRepo()

        // Insert entity dengan type valid
        dao.upsert(TransactionEntity(0, "EXPENSE", 1000, "FOOD", 0L, "", 1000L))
        // Insert entity dengan type invalid
        dao.upsert(TransactionEntity(0, "INVALID", 2000, "FOOD", 0L, "", 2000L))

        val results = repo.observeAll().first()
        assertEquals(1, results.size)
        assertEquals(1000L, results[0].amount)
    }
}
