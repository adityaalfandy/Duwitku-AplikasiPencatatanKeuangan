package com.pemmob.duwitku.di

import com.pemmob.duwitku.data.repository.RateRepositoryImpl
import com.pemmob.duwitku.data.repository.TransactionRepositoryImpl
import com.pemmob.duwitku.domain.repository.RateRepository
import com.pemmob.duwitku.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRateRepository(
        impl: RateRepositoryImpl
    ): RateRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}

