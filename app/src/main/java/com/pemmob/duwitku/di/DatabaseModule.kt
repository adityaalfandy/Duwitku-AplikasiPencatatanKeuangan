package com.pemmob.duwitku.di

import android.content.Context
import androidx.room.Room
import com.pemmob.duwitku.data.local.DuwitkuDatabase
import com.pemmob.duwitku.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DuwitkuDatabase {
        return Room.databaseBuilder(
            context,
            DuwitkuDatabase::class.java,
            "duwitku.db"
        ).build()
    }

    @Provides
    fun provideTransactionDao(database: DuwitkuDatabase): TransactionDao {
        return database.transactionDao()
    }
}
