package com.pemmob.duwitku.data.repository

import com.pemmob.duwitku.data.local.TransactionDao
import com.pemmob.duwitku.data.mapper.toDomain
import com.pemmob.duwitku.data.mapper.toEntity
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> {
        return dao.observeAll().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> {
        return dao.observeRecent(limit).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> {
        val startEpochDay = month.atDay(1).toEpochDay()
        val endEpochDay = month.atEndOfMonth().toEpochDay()
        return dao.observeBetween(startEpochDay, endEpochDay).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun observeById(id: Long): Flow<Transaction?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    override fun observeTotalBalance(): Flow<Long> {
        return dao.observeBalance()
    }

    override suspend fun save(transaction: Transaction) {
        val createdAt: Long = if (transaction.id == 0L) {
            System.currentTimeMillis()
        } else {
            // Pertahankan createdAt asli saat edit
            dao.getById(transaction.id)?.createdAt ?: System.currentTimeMillis()
        }
        dao.upsert(transaction.toEntity(createdAt))
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
