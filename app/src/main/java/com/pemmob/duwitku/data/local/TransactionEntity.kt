package com.pemmob.duwitku.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("dateEpochDay")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amount: Long,
    val category: String,
    @ColumnInfo(name = "dateEpochDay")
    val dateEpochDay: Long,
    val note: String = "",
    val createdAt: Long
)
