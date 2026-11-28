package com.yatha.moneytracker.ui.expense

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yatha.moneytracker.R
import com.yatha.moneytracker.data.local.MoneyTrackerDatabase
import com.yatha.moneytracker.data.repository.ExpenseRepository
import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountError
import com.yatha.moneytracker.domain.time.CalendarDate
import com.yatha.moneytracker.ui.theme.MoneyTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class ExpenseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val today = CalendarDate.of(System.currentTimeMillis(), TimeZone.getDefault())
    private var database: MoneyTrackerDatabase? = null

    @After
    fun closeDatabase() {
        database?.close()
    }

    private fun text(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    /** Shows the stateless screen with fixed state. */
    private fun showScreen(
        form: ExpenseFormState = ExpenseFormState(date = today),
        list: ExpenseListState = ExpenseListState.Loaded(emptyList()),
        onSave: () -> Unit = {},
    ) {
        composeRule.setContent {
            MoneyTrackerTheme {
                ExpenseScreen(
                    form = form,
                    list = list,
                    onAmountChange = {},
                    onCategorySelect = {},
                    onDescriptionChange = {},
                    onDateSelect = {},
                    onSave = onSave,
                    onMessageShown = {},
                )
            }
        }
    }

    /** Shows the real screen backed by a real ViewModel and an in-memory Room database. */
    private fun showAppWithInMemoryDatabase(): ExpenseViewModel {
        val db = Room.inMemoryDatabaseBuilder(context, MoneyTrackerDatabase::class.java).build()
        database = db
        val viewModel = ExpenseViewModel(ExpenseRepository(db.expenseDao()))
        composeRule.setContent { MoneyTrackerTheme { ExpenseRoute(viewModel) } }
        return viewModel
    }

    private fun waitForText(value: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // --- Stateless screen ---

    @Test
    fun emptyList_showsTheEmptyState() {
        showScreen(list = ExpenseListState.Loaded(emptyList()))

        composeRule.onNodeWithTag(ExpenseScreenTags.LIST)
            .performScrollToNode(hasText(text(R.string.transactions_empty_title)))
        composeRule.onNodeWithText(text(R.string.transactions_empty_title)).assertIsDisplayed()
    }

    @Test
    fun savedExpense_showsAmountCategoryDescriptionAndDate() {
        val transactionDate = today.toLocalNoonMillis(TimeZone.getDefault())
        val expense = Expense(
            id = 7,
            amount = 1_23_456_78L,
            category = ExpenseCategory.HEALTH,
            description = "Pharmacy",
            transactionDate = transactionDate,
            createdAt = transactionDate,
        )
        showScreen(list = ExpenseListState.Loaded(listOf(expense)))

        composeRule.onNodeWithTag(ExpenseScreenTags.LIST).performScrollToNode(hasText("₹1,23,456.78"))
        composeRule.onNodeWithText("₹1,23,456.78").assertIsDisplayed()
        composeRule.onNodeWithText("Pharmacy").assertIsDisplayed()
        composeRule.onNodeWithText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(transactionDate)))
            .assertIsDisplayed()
        // "Health" appears twice: as a category chip in the form and on the transaction row.
        assertEquals(2, composeRule.onAllNodesWithText(text(R.string.category_health)).fetchSemanticsNodes().size)
    }

    @Test
    fun validationErrors_areShown() {
        showScreen(
            form = ExpenseFormState(
                amountText = "abc",
                date = today,
                errors = ExpenseFormErrors(
                    amount = AmountError.MALFORMED,
                    categoryMissing = true,
                    dateInFuture = true,
                    descriptionTooLong = true,
                ),
            ),
        )

        composeRule.onNodeWithText(text(R.string.error_amount_malformed)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.error_category_missing)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.error_description_too_long, MAX_DESCRIPTION_LENGTH)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.error_date_in_future)).assertIsDisplayed()
    }

    @Test
    fun whileSaving_inputsAndSaveAreDisabled_andProgressIsShown() {
        var saveClicks = 0
        showScreen(
            form = ExpenseFormState(amountText = "10", category = ExpenseCategory.FOOD, date = today, isSaving = true),
            onSave = { saveClicks++ },
        )

        composeRule.onNodeWithText(text(R.string.saving_expense)).assertIsDisplayed()
        composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).assertIsNotEnabled()
        composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).assertIsNotEnabled()
        composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).performClick()
        assertEquals(0, saveClicks)
    }

    // --- Real ViewModel + in-memory database ---

    @Test
    fun savingAnEmptyForm_showsValidationMessages() {
        showAppWithInMemoryDatabase()

        composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).performScrollTo().performClick()

        composeRule.onNodeWithText(text(R.string.error_amount_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.error_category_missing)).assertIsDisplayed()
    }

    @Test
    fun invalidAmounts_showAnError_andCreateNoDatabaseRecord() {
        val viewModel = showAppWithInMemoryDatabase()
        composeRule.waitUntil(timeoutMillis = 5_000) { viewModel.listState.value is ExpenseListState.Loaded }
        composeRule.onNodeWithTag(ExpenseScreenTags.category(ExpenseCategory.FOOD)).performScrollTo().performClick()

        mapOf(
            "-5" to R.string.error_amount_negative,
            "abc" to R.string.error_amount_malformed,
            "1.005" to R.string.error_amount_too_many_decimals,
            "0" to R.string.error_amount_zero,
        ).forEach { (input, errorRes) ->
            composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performScrollTo().performTextClearance()
            composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performTextInput(input)
            composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).performScrollTo().performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performScrollTo()
            composeRule.onNodeWithText(text(errorRes)).assertIsDisplayed()
        }

        // Above the maximum (and beyond Long.MAX_VALUE paise): the message includes the limit.
        composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performScrollTo().performTextClearance()
        composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performTextInput("92233720368547758.08")
        composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(AmountError.TOO_LARGE, viewModel.formState.value.errors.amount)

        // Nothing reached the database.
        val storedRows = runBlocking { requireNotNull(database).expenseDao().observeAll().first() }
        assertEquals(emptyList<Any>(), storedRows)
        assertEquals(ExpenseListState.Loaded(emptyList()), viewModel.listState.value)
    }

    @Test
    fun enteringAndSavingAnExpense_addsItToTheList_andClearsTheForm() {
        val viewModel = showAppWithInMemoryDatabase()
        composeRule.waitUntil(timeoutMillis = 5_000) { viewModel.listState.value is ExpenseListState.Loaded }

        // Scroll before each interaction: once the soft keyboard opens, the list's viewport
        // shrinks (imePadding) and lower controls can be outside the visible area.
        composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT).performScrollTo().performTextInput("125.50")
        composeRule.onNodeWithTag(ExpenseScreenTags.category(ExpenseCategory.FOOD)).performScrollTo().performClick()
        composeRule.onNodeWithTag(ExpenseScreenTags.DESCRIPTION).performScrollTo().performTextInput("Lunch")
        composeRule.onNodeWithTag(ExpenseScreenTags.SAVE).performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("form errors after Save", ExpenseFormErrors(), viewModel.formState.value.errors)

        // The list updates reactively from Room.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            (viewModel.listState.value as? ExpenseListState.Loaded)?.expenses?.size == 1
        }
        composeRule.onNodeWithTag(ExpenseScreenTags.LIST).performScrollToNode(hasText("₹125.50"))
        composeRule.onNodeWithText("₹125.50").assertIsDisplayed()
        composeRule.onNodeWithText("Lunch").assertIsDisplayed()

        // Feedback is shown and the form is cleared for the next entry.
        waitForText(text(R.string.message_saved))
        composeRule.onNodeWithTag(ExpenseScreenTags.AMOUNT)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
        composeRule.onNodeWithTag(ExpenseScreenTags.DESCRIPTION)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
    }
}
