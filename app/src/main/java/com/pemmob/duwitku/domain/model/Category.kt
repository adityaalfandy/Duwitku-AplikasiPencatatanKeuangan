package com.pemmob.duwitku.domain.model

enum class Category(val type: TransactionType) {
    // Pengeluaran
    FOOD(TransactionType.EXPENSE),
    TRANSPORT(TransactionType.EXPENSE),
    SHOPPING(TransactionType.EXPENSE),
    BILLS(TransactionType.EXPENSE),
    ENTERTAINMENT(TransactionType.EXPENSE),
    HEALTH(TransactionType.EXPENSE),
    EDUCATION(TransactionType.EXPENSE),
    OTHER_EXPENSE(TransactionType.EXPENSE),
    
    // Pemasukan
    ALLOWANCE(TransactionType.INCOME),
    SALARY(TransactionType.INCOME),
    BONUS(TransactionType.INCOME),
    OTHER_INCOME(TransactionType.INCOME)
}
