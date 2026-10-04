package com.pemmob.duwitku.data.mapper

import com.pemmob.duwitku.data.local.TransactionEntity
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TransactionMapperTest {

    @Test
    fun `toDomain maps valid entity correctly`() {
        val entity = TransactionEntity(
            id = 1,
            type = "EXPENSE",
            amount = 25000,
            category = "FOOD",
            dateEpochDay = LocalDate.of(2026, 1, 15).toEpochDay(),
            note = "Makan siang",
            createdAt = 1000L
        )

        val result = entity.toDomain()!!

        assertEquals(1L, result.id)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals(25000L, result.amount)
        assertEquals(Category.FOOD, result.category)
        assertEquals(LocalDate.of(2026, 1, 15), result.date)
        assertEquals("Makan siang", result.note)
    }

    @Test
    fun `toDomain returns null for unknown type`() {
        val entity = TransactionEntity(
            id = 2,
            type = "UNKNOWN_TYPE",
            amount = 5000,
            category = "FOOD",
            dateEpochDay = 0L,
            note = "",
            createdAt = 1000L
        )

        assertNull(entity.toDomain())
    }

    @Test
    fun `toDomain falls back to OTHER_EXPENSE for unknown expense category`() {
        val entity = TransactionEntity(
            id = 3,
            type = "EXPENSE",
            amount = 5000,
            category = "NONEXISTENT",
            dateEpochDay = 0L,
            note = "",
            createdAt = 1000L
        )

        val result = entity.toDomain()!!
        assertEquals(Category.OTHER_EXPENSE, result.category)
    }

    @Test
    fun `toDomain falls back to OTHER_INCOME for unknown income category`() {
        val entity = TransactionEntity(
            id = 4,
            type = "INCOME",
            amount = 5000,
            category = "NONEXISTENT",
            dateEpochDay = 0L,
            note = "",
            createdAt = 1000L
        )

        val result = entity.toDomain()!!
        assertEquals(Category.OTHER_INCOME, result.category)
    }

    @Test
    fun `toDomain falls back when category does not match type`() {
        // FOOD is EXPENSE category, but type is INCOME
        val entity = TransactionEntity(
            id = 5,
            type = "INCOME",
            amount = 5000,
            category = "FOOD",
            dateEpochDay = 0L,
            note = "",
            createdAt = 1000L
        )

        val result = entity.toDomain()!!
        assertEquals(Category.OTHER_INCOME, result.category)
    }

    @Test
    fun `toEntity maps domain transaction correctly`() {
        val transaction = Transaction(
            id = 10,
            type = TransactionType.INCOME,
            amount = 100000,
            category = Category.SALARY,
            date = LocalDate.of(2026, 3, 1),
            note = "Gaji"
        )

        val entity = transaction.toEntity(createdAt = 5000L)

        assertEquals(10L, entity.id)
        assertEquals("INCOME", entity.type)
        assertEquals(100000L, entity.amount)
        assertEquals("SALARY", entity.category)
        assertEquals(LocalDate.of(2026, 3, 1).toEpochDay(), entity.dateEpochDay)
        assertEquals("Gaji", entity.note)
        assertEquals(5000L, entity.createdAt)
    }

    @Test
    fun `roundtrip entity to domain and back preserves data`() {
        val original = TransactionEntity(
            id = 7,
            type = "EXPENSE",
            amount = 15000,
            category = "TRANSPORT",
            dateEpochDay = LocalDate.of(2024, 2, 29).toEpochDay(), // tahun kabisat
            note = "Grab",
            createdAt = 9999L
        )

        val domain = original.toDomain()!!
        val backToEntity = domain.toEntity(createdAt = 9999L)

        assertEquals(original.id, backToEntity.id)
        assertEquals(original.type, backToEntity.type)
        assertEquals(original.amount, backToEntity.amount)
        assertEquals(original.category, backToEntity.category)
        assertEquals(original.dateEpochDay, backToEntity.dateEpochDay)
        assertEquals(original.note, backToEntity.note)
        assertEquals(original.createdAt, backToEntity.createdAt)
    }

    @Test
    fun `toDomain handles epoch day boundary dates`() {
        // Epoch day 0 = 1970-01-01
        val entity = TransactionEntity(
            id = 8,
            type = "EXPENSE",
            amount = 1000,
            category = "FOOD",
            dateEpochDay = 0L,
            note = "",
            createdAt = 1000L
        )
        val result = entity.toDomain()!!
        assertEquals(LocalDate.of(1970, 1, 1), result.date)
    }
}
