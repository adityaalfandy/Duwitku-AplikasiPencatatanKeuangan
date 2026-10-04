package com.pemmob.duwitku.domain.repository

import com.pemmob.duwitku.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>
    fun observeById(id: Long): Flow<Transaction?>
    fun observeTotalBalance(): Flow<Long>
    suspend fun save(transaction: Transaction)
    suspend fun delete(id: Long)
}
