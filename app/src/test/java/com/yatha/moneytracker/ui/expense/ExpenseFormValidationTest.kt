package com.yatha.moneytracker.ui.expense

import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountError
import com.yatha.moneytracker.domain.time.CalendarDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseFormValidationTest {

    private val today = CalendarDate(2026, 11, 28)

    private val validForm = ExpenseFormState(
        amountText = "125.50",
        category = ExpenseCategory.FOOD,
        description = "Lunch",
        date = today,
    )

    private fun errorsOf(form: ExpenseFormState): ExpenseFormErrors =
        (validateExpenseForm(form, today) as ExpenseFormValidation.Invalid).errors

    @Test
    fun validForm_producesParsedValues() {
        assertEquals(
            ExpenseFormValidation.Valid(
                amountPaise = 12_550L,
                category = ExpenseCategory.FOOD,
                description = "Lunch",
                date = today,
            ),
            validateExpenseForm(validForm, today),
        )
    }

    @Test
    fun description_isTrimmed_andMayBeEmpty() {
        val padded = validateExpenseForm(validForm.copy(description = "  Lunch  "), today)
        val blank = validateExpenseForm(validForm.copy(description = "   "), today)

        assertEquals("Lunch", (padded as ExpenseFormValidation.Valid).description)
        assertEquals("", (blank as ExpenseFormValidation.Valid).description)
    }

    @Test
    fun emptyForm_reportsMissingAmountAndCategory_only() {
        assertEquals(
            ExpenseFormErrors(amount = AmountError.EMPTY, categoryMissing = true),
            errorsOf(ExpenseFormState(date = today)),
        )
    }

    @Test
    fun amountErrors_areReportedFromTheParser() {
        mapOf(
            "abc" to AmountError.MALFORMED,
            "0" to AmountError.ZERO,
            "-5" to AmountError.NEGATIVE,
            "1.001" to AmountError.TOO_MANY_DECIMAL_PLACES,
            "1000000000" to AmountError.TOO_LARGE,
        ).forEach { (text, error) ->
            assertEquals("amount \"$text\"", ExpenseFormErrors(amount = error), errorsOf(validForm.copy(amountText = text)))
        }
    }

    @Test
    fun missingCategory_isReported() {
        assertEquals(ExpenseFormErrors(categoryMissing = true), errorsOf(validForm.copy(category = null)))
    }

    @Test
    fun today_andPastDates_areAccepted() {
        listOf(today, CalendarDate(2026, 11, 27), CalendarDate(2020, 1, 1)).forEach { date ->
            assertEquals(
                "date $date",
                date,
                (validateExpenseForm(validForm.copy(date = date), today) as ExpenseFormValidation.Valid).date,
            )
        }
    }

    @Test
    fun futureDates_areRejected() {
        listOf(CalendarDate(2026, 11, 29), CalendarDate(2027, 1, 1)).forEach { date ->
            assertEquals("date $date", ExpenseFormErrors(dateInFuture = true), errorsOf(validForm.copy(date = date)))
        }
    }

    @Test
    fun descriptionLengthLimit_appliesAfterTrimming() {
        val atLimit = "x".repeat(MAX_DESCRIPTION_LENGTH)

        assertEquals(atLimit, (validateExpenseForm(validForm.copy(description = atLimit), today) as ExpenseFormValidation.Valid).description)
        assertEquals(atLimit, (validateExpenseForm(validForm.copy(description = "  $atLimit  "), today) as ExpenseFormValidation.Valid).description)
        assertEquals(
            ExpenseFormErrors(descriptionTooLong = true),
            errorsOf(validForm.copy(description = atLimit + "x")),
        )
    }

    @Test
    fun allProblems_areReportedTogether() {
        val form = ExpenseFormState(
            amountText = "abc",
            category = null,
            description = "x".repeat(MAX_DESCRIPTION_LENGTH + 1),
            date = CalendarDate(2026, 12, 1),
        )

        assertEquals(
            ExpenseFormErrors(
                amount = AmountError.MALFORMED,
                categoryMissing = true,
                dateInFuture = true,
                descriptionTooLong = true,
            ),
            errorsOf(form),
        )
    }
}
