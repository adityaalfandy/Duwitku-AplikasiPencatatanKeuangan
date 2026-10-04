package com.pemmob.duwitku.di

import com.pemmob.duwitku.data.repository.RateRepositoryImpl
import com.pemmob.duwitku.domain.repository.RateRepository
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
}
