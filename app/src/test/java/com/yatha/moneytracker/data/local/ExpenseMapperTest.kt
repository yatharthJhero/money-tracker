package com.yatha.moneytracker.data.local

import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExpenseMapperTest {

    // 2026-01-01T00:00:00Z and 2026-01-02T00:00:00Z: deliberately different so a swap is caught.
    private val transactionDate = 1_767_225_600_000L
    private val createdAt = 1_767_312_000_000L

    private val expense = Expense(
        id = 42L,
        amount = 12_550L,
        currencyCode = "USD",
        category = ExpenseCategory.HEALTH,
        description = "Pharmacy",
        transactionDate = transactionDate,
        createdAt = createdAt,
    )

    private val entity = ExpenseEntity(
        id = 42L,
        amountMinor = 12_550L,
        currencyCode = "USD",
        category = "HEALTH",
        description = "Pharmacy",
        transactionDate = transactionDate,
        createdAt = createdAt,
    )

    @Test
    fun toEntity_copiesEveryField() {
        assertEquals(entity, expense.toEntity())
    }

    @Test
    fun toDomain_copiesEveryField() {
        assertEquals(expense, entity.toDomain())
    }

    @Test
    fun roundTrip_isLossless_forEveryCategory() {
        ExpenseCategory.entries.forEach { category ->
            val original = expense.copy(category = category)

            assertEquals(original, original.toEntity().toDomain())
        }
    }

    @Test
    fun category_isStoredByName_notOrdinal() {
        ExpenseCategory.entries.forEach { category ->
            assertEquals(category.name, expense.copy(category = category).toEntity().category)
        }
    }

    @Test
    fun amountBoundaries_arePreservedExactly() {
        // 1 paisa, ₹9,99,99,999.99 and the largest representable amount.
        listOf(1L, 9_999_999_999L, Long.MAX_VALUE).forEach { amount ->
            val original = expense.copy(amount = amount)

            assertEquals(amount, original.toEntity().amountMinor)
            assertEquals(amount, original.toEntity().toDomain().amount)
        }
    }

    @Test
    fun currency_isPreservedAsGiven() {
        listOf("INR", "USD", "EUR").forEach { currency ->
            assertEquals(currency, expense.copy(currencyCode = currency).toEntity().toDomain().currencyCode)
        }
    }

    @Test
    fun emptyDescription_isPreserved() {
        val original = expense.copy(description = "")

        assertEquals("", original.toEntity().toDomain().description)
    }

    @Test
    fun newExpense_keepsZeroId_soTheDatabaseGeneratesOne() {
        assertEquals(0L, expense.copy(id = 0L).toEntity().id)
    }

    @Test
    fun toDomain_unknownCategory_failsLoudly() {
        assertThrows(IllegalStateException::class.java) {
            entity.copy(category = "TRAVEL").toDomain()
        }
    }

    @Test
    fun toDomain_categoryNameIsCaseSensitive() {
        assertThrows(IllegalStateException::class.java) {
            entity.copy(category = "health").toDomain()
        }
    }

    @Test
    fun toDomain_nonPositiveStoredAmount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            entity.copy(amountMinor = 0L).toDomain()
        }
    }
}
