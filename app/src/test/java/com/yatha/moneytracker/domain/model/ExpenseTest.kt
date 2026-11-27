package com.yatha.moneytracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExpenseTest {

    private fun expenseWithAmount(amount: Long) = Expense(
        id = 1L,
        amount = amount,
        category = ExpenseCategory.FOOD,
        transactionDate = 1_700_000_000_000L,
        createdAt = 1_700_000_000_000L,
    )

    @Test
    fun positiveAmount_isAccepted() {
        val expense = expenseWithAmount(12_550L)

        assertEquals(12_550L, expense.amount)
    }

    @Test
    fun smallestPositiveAmount_isAccepted() {
        val expense = expenseWithAmount(1L)

        assertEquals(1L, expense.amount)
    }

    @Test
    fun zeroAmount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            expenseWithAmount(0L)
        }
    }

    @Test
    fun negativeAmount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            expenseWithAmount(-100L)
        }
    }

    @Test
    fun defaults_areInrAndEmptyDescription() {
        val expense = expenseWithAmount(100L)

        assertEquals("INR", expense.currencyCode)
        assertEquals("", expense.description)
    }

    @Test
    fun copyWithNonPositiveAmount_isRejected() {
        val expense = expenseWithAmount(100L)

        assertThrows(IllegalArgumentException::class.java) {
            expense.copy(amount = 0L)
        }
    }
}
