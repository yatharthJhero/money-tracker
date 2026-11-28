package com.yatha.moneytracker.ui.expense

import com.yatha.moneytracker.data.local.ExpenseEntity
import com.yatha.moneytracker.data.repository.ExpenseRepository
import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountError
import com.yatha.moneytracker.domain.time.CalendarDate
import com.yatha.moneytracker.testing.FakeExpenseDao
import com.yatha.moneytracker.testing.IST
import com.yatha.moneytracker.testing.MainDispatcherRule
import com.yatha.moneytracker.testing.millisAt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeExpenseDao()

    /** "Now" for the ViewModel's clock: 28 Nov 2026, 10:15 in India. Tests may move it. */
    private var now = millisAt(IST, 2026, 11, 28, 10, 15)
    private val today = CalendarDate(2026, 11, 28)

    private fun createViewModel() = ExpenseViewModel(
        repository = ExpenseRepository(dao),
        clock = { now },
        timeZone = { IST },
    )

    private fun ExpenseViewModel.fillValidForm(
        amount: String = "125.50",
        category: ExpenseCategory = ExpenseCategory.FOOD,
        description: String = "Lunch",
    ) {
        onAmountChange(amount)
        onCategorySelect(category)
        onDescriptionChange(description)
    }

    /** `listState` only loads while someone collects it, like the UI does. */
    private fun TestScope.subscribeToList(viewModel: ExpenseViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.listState.collect {} }
    }

    private fun storedEntity(id: Long, day: Int) = ExpenseEntity(
        id = id,
        amountMinor = 1_000L * id,
        currencyCode = "INR",
        category = "FOOD",
        description = "",
        transactionDate = millisAt(IST, 2026, 11, day, 12),
        createdAt = millisAt(IST, 2026, 11, day, 12),
    )

    // --- Initial state and the transaction list ---

    @Test
    fun initialForm_isEmpty_andDefaultsToToday() {
        assertEquals(ExpenseFormState(date = today), createViewModel().formState.value)
    }

    @Test
    fun listState_isLoadingUntilCollected_thenShowsStoredExpensesNewestFirst() = runTest {
        dao.seed(storedEntity(id = 1, day = 26), storedEntity(id = 2, day = 27), storedEntity(id = 3, day = 25))
        val viewModel = createViewModel()

        assertEquals(ExpenseListState.Loading, viewModel.listState.value)
        subscribeToList(viewModel)

        val loaded = viewModel.listState.value as ExpenseListState.Loaded
        assertEquals(listOf(2L, 1L, 3L), loaded.expenses.map { it.id })
    }

    @Test
    fun listState_isEmpty_whenNothingIsStored() = runTest {
        val viewModel = createViewModel()
        subscribeToList(viewModel)

        assertEquals(ExpenseListState.Loaded(emptyList()), viewModel.listState.value)
    }

    @Test
    fun listState_reportsError_whenLoadingFails() = runTest {
        dao.observeFailure = IllegalStateException("Unknown expense category")
        val viewModel = createViewModel()
        subscribeToList(viewModel)

        assertEquals(ExpenseListState.Error, viewModel.listState.value)
    }

    // --- Validation ---

    @Test
    fun savingAnEmptyForm_showsErrors_andSavesNothing() {
        val viewModel = createViewModel()

        viewModel.saveExpense()

        val form = viewModel.formState.value
        assertEquals(ExpenseFormErrors(amount = AmountError.EMPTY, categoryMissing = true), form.errors)
        assertFalse(form.isSaving)
        assertNull(form.message)
        assertEquals(0, dao.insertCallCount)
    }

    @Test
    fun invalidAmount_isReported_andTheTypedTextIsKept() {
        val viewModel = createViewModel()
        viewModel.fillValidForm(amount = "12.345")

        viewModel.saveExpense()

        assertEquals(AmountError.TOO_MANY_DECIMAL_PLACES, viewModel.formState.value.errors.amount)
        assertEquals("12.345", viewModel.formState.value.amountText)
        assertEquals(0, dao.insertCallCount)
    }

    @Test
    fun everyKindOfInvalidAmount_isRejected_andNoRecordIsCreated() = runTest {
        val viewModel = createViewModel()
        subscribeToList(viewModel)
        viewModel.fillValidForm()

        mapOf(
            // Negative values
            "-1" to AmountError.NEGATIVE,
            "-0.01" to AmountError.NEGATIVE,
            "-125.50" to AmountError.NEGATIVE,
            // Malformed input
            "abc" to AmountError.MALFORMED,
            "1,000" to AmountError.MALFORMED,
            "1e3" to AmountError.MALFORMED,
            "+5" to AmountError.MALFORMED,
            "₹100" to AmountError.MALFORMED,
            "." to AmountError.MALFORMED,
            "1.2.3" to AmountError.MALFORMED,
            // Zero and blank
            "0" to AmountError.ZERO,
            "0.00" to AmountError.ZERO,
            "   " to AmountError.EMPTY,
            // Excessive decimal places
            "1.005" to AmountError.TOO_MANY_DECIMAL_PLACES,
            "0.001" to AmountError.TOO_MANY_DECIMAL_PLACES,
            // Above the maximum, including values that would overflow a Long
            "1000000000" to AmountError.TOO_LARGE,
            "92233720368547758.08" to AmountError.TOO_LARGE,
            "9".repeat(100) to AmountError.TOO_LARGE,
        ).forEach { (text, expectedError) ->
            viewModel.onAmountChange(text)
            viewModel.saveExpense()

            val form = viewModel.formState.value
            assertEquals("error for \"$text\"", expectedError, form.errors.amount)
            assertEquals("typed text kept for \"$text\"", text, form.amountText)
            assertFalse("not saving after \"$text\"", form.isSaving)
            assertNull("no message after \"$text\"", form.message)
        }

        assertEquals("insert calls", 0, dao.insertCallCount)
        assertEquals("stored rows", 0, dao.rowCount)
        assertEquals(ExpenseListState.Loaded(emptyList()), viewModel.listState.value)
    }

    @Test
    fun futureDate_isReported() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onDateSelect(CalendarDate(2026, 11, 29))

        viewModel.saveExpense()

        assertTrue(viewModel.formState.value.errors.dateInFuture)
        assertEquals(0, dao.insertCallCount)
    }

    @Test
    fun tooLongDescription_isReported() {
        val viewModel = createViewModel()
        viewModel.fillValidForm(description = "x".repeat(MAX_DESCRIPTION_LENGTH + 1))

        viewModel.saveExpense()

        assertTrue(viewModel.formState.value.errors.descriptionTooLong)
        assertEquals(0, dao.insertCallCount)
    }

    @Test
    fun editingAField_clearsOnlyThatFieldsError() {
        val viewModel = createViewModel()
        viewModel.saveExpense()

        viewModel.onAmountChange("5")
        assertNull(viewModel.formState.value.errors.amount)
        assertTrue(viewModel.formState.value.errors.categoryMissing)

        viewModel.onCategorySelect(ExpenseCategory.RENT)
        assertFalse(viewModel.formState.value.errors.hasAny)
    }

    // --- Saving ---

    @Test
    fun successfulSave_storesANewExpense_withIdZeroForTheDatabaseToReplace() {
        val viewModel = createViewModel()
        viewModel.fillValidForm(amount = "125.50", category = ExpenseCategory.HEALTH, description = "  Medicine  ")
        viewModel.onDateSelect(CalendarDate(2026, 11, 20))

        viewModel.saveExpense()

        assertEquals(
            ExpenseEntity(
                id = 0L,
                amountMinor = 12_550L,
                currencyCode = "INR",
                category = "HEALTH",
                description = "Medicine",
                transactionDate = millisAt(IST, 2026, 11, 20, 12), // noon on the chosen date
                createdAt = now,
            ),
            dao.insertArguments.single(),
        )
        assertEquals(1, dao.rowCount)
    }

    @Test
    fun successfulSave_clearsTheForm_andReportsSaved() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onDateSelect(CalendarDate(2026, 11, 20))

        viewModel.saveExpense()

        assertEquals(ExpenseFormState(date = today, message = ExpenseFormMessage.SAVED), viewModel.formState.value)
    }

    @Test
    fun successfulSave_appearsInTheTransactionList() = runTest {
        val viewModel = createViewModel()
        subscribeToList(viewModel)
        assertEquals(ExpenseListState.Loaded(emptyList()), viewModel.listState.value)

        viewModel.fillValidForm(amount = "125.50")
        viewModel.saveExpense()

        val expenses = (viewModel.listState.value as ExpenseListState.Loaded).expenses
        assertEquals(1, expenses.size)
        assertEquals(1L, expenses.single().id)
        assertEquals(12_550L, expenses.single().amount)
    }

    @Test
    fun whileSaving_theFormIsBusy_andRepeatedSavesAreIgnored() = runTest {
        val writeFinished = CompletableDeferred<Unit>()
        dao.insertGate = writeFinished
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.saveExpense()
        assertTrue(viewModel.formState.value.isSaving)
        viewModel.saveExpense()
        viewModel.saveExpense()
        assertEquals(1, dao.insertCallCount)

        writeFinished.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
        assertEquals(ExpenseFormMessage.SAVED, viewModel.formState.value.message)
        assertEquals(1, dao.rowCount)
    }

    @Test
    fun failedSave_keepsTheInput_reportsFailure_andAllowsRetry() {
        dao.nextInsertFailure = IllegalStateException("disk full")
        val viewModel = createViewModel()
        viewModel.fillValidForm(amount = "125.50", category = ExpenseCategory.FOOD, description = "Lunch")
        viewModel.onDateSelect(CalendarDate(2026, 11, 20))

        viewModel.saveExpense()

        assertEquals(
            ExpenseFormState(
                amountText = "125.50",
                category = ExpenseCategory.FOOD,
                description = "Lunch",
                date = CalendarDate(2026, 11, 20),
                isSaving = false,
                message = ExpenseFormMessage.SAVE_FAILED,
            ),
            viewModel.formState.value,
        )
        assertEquals(0, dao.rowCount)

        viewModel.onMessageShown()
        viewModel.saveExpense()

        assertEquals(ExpenseFormMessage.SAVED, viewModel.formState.value.message)
        assertEquals(1, dao.rowCount)
    }

    @Test
    fun onMessageShown_clearsTheMessage() {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.saveExpense()

        viewModel.onMessageShown()

        assertNull(viewModel.formState.value.message)
    }

    @Test
    fun afterSavingPastMidnight_theFormDefaultsToTheNewToday() {
        now = millisAt(IST, 2026, 11, 28, 23, 58)
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        now = millisAt(IST, 2026, 11, 29, 0, 5)
        viewModel.saveExpense()

        assertEquals(1, dao.rowCount)
        assertEquals(millisAt(IST, 2026, 11, 28, 12), dao.insertArguments.single().transactionDate)
        assertEquals(CalendarDate(2026, 11, 29), viewModel.formState.value.date)
    }
}
