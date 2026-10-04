package com.pemmob.duwitku.data.mapper

import com.pemmob.duwitku.data.local.TransactionEntity
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import java.time.LocalDate

/**
 * Entity -> domain Transaction.
 * Mengembalikan null jika type tidak dikenali (baris dilewati di repository via mapNotNull).
 * Category yang tidak dikenali atau tidak sesuai type di-fallback ke OTHER_EXPENSE/OTHER_INCOME.
 */
fun TransactionEntity.toDomain(): Transaction? {
    val domainType = TransactionType.entries.firstOrNull { it.name == type }
        ?: return null

    val domainCategory = Category.entries.firstOrNull { it.name == category }
        ?.let { cat ->
            // Pastikan category sesuai type; bila tidak, fallback
            if (cat.type == domainType) cat
            else fallbackCategory(domainType)
        }
        ?: fallbackCategory(domainType)

    return Transaction(
        id = id,
        type = domainType,
        amount = amount,
        category = domainCategory,
        date = LocalDate.ofEpochDay(dateEpochDay),
        note = note
    )
}

/**
 * Domain Transaction -> Entity.
 * createdAt harus disediakan oleh pemanggil (repository menentukan nilainya).
 */
fun Transaction.toEntity(createdAt: Long): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        amount = amount,
        category = category.name,
        dateEpochDay = date.toEpochDay(),
        note = note,
        createdAt = createdAt
    )
}

private fun fallbackCategory(type: TransactionType): Category {
    return when (type) {
        TransactionType.EXPENSE -> Category.OTHER_EXPENSE
        TransactionType.INCOME -> Category.OTHER_INCOME
    }
}
