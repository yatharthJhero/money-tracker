package com.yatha.moneytracker.ui.expense

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yatha.moneytracker.R
import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountError
import com.yatha.moneytracker.domain.money.MoneyFormatter
import com.yatha.moneytracker.domain.money.RupeeAmountParser
import com.yatha.moneytracker.domain.time.CalendarDate
import com.yatha.moneytracker.ui.theme.MoneyTrackerTheme
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

/** Test tags for UI tests. */
object ExpenseScreenTags {
    const val AMOUNT = "amount_input"
    const val DESCRIPTION = "description_input"
    const val DATE = "date_button"
    const val SAVE = "save_button"
    const val LIST = "expense_list"
    fun category(category: ExpenseCategory) = "category_${category.name}"
}

/** Connects [ExpenseViewModel] to the stateless [ExpenseScreen]. */
@Composable
fun ExpenseRoute(viewModel: ExpenseViewModel, modifier: Modifier = Modifier) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val list by viewModel.listState.collectAsStateWithLifecycle()
    ExpenseScreen(
        form = form,
        list = list,
        onAmountChange = viewModel::onAmountChange,
        onCategorySelect = viewModel::onCategorySelect,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDateSelect = viewModel::onDateSelect,
        onSave = viewModel::saveExpense,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

/** Add-expense form above the transaction list. Holds no business logic of its own. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    form: ExpenseFormState,
    list: ExpenseListState,
    onAmountChange: (String) -> Unit,
    onCategorySelect: (ExpenseCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateSelect: (CalendarDate) -> Unit,
    onSave: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val messageText = form.message?.let { stringResource(it.textRes()) }
    LaunchedEffect(form.message) {
        if (messageText != null) {
            // Shown in a separate scope so that clearing the message (which restarts this
            // effect) does not cancel the snackbar that is on screen.
            snackbarScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(messageText)
            }
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .testTag(ExpenseScreenTags.LIST),
            contentPadding = PaddingValues(16.dp),
        ) {
            item(key = "form") {
                ExpenseForm(
                    form = form,
                    onAmountChange = onAmountChange,
                    onCategorySelect = onCategorySelect,
                    onDescriptionChange = onDescriptionChange,
                    onDateSelect = onDateSelect,
                    onSave = onSave,
                )
            }
            item(key = "transactions_title") {
                Text(
                    text = stringResource(R.string.transactions_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
                )
            }
            when (list) {
                ExpenseListState.Loading -> item(key = "loading") {
                    Text(stringResource(R.string.transactions_loading))
                }
                ExpenseListState.Error -> item(key = "error") {
                    Text(
                        text = stringResource(R.string.transactions_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                is ExpenseListState.Loaded -> if (list.expenses.isEmpty()) {
                    item(key = "empty") { EmptyTransactions() }
                } else {
                    items(items = list.expenses, key = { it.id }) { expense -> ExpenseRow(expense) }
                }
            }
        }
    }
}

@Composable
private fun ExpenseForm(
    form: ExpenseFormState,
    onAmountChange: (String) -> Unit,
    onCategorySelect: (ExpenseCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateSelect: (CalendarDate) -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val enabled = !form.isSaving
    val errorColor = MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.add_expense_title), style = MaterialTheme.typography.titleLarge)

        val amountError = form.errors.amount?.let { amountErrorText(it) }
        OutlinedTextField(
            value = form.amountText,
            onValueChange = onAmountChange,
            label = { Text(stringResource(R.string.amount_label)) },
            prefix = { Text(stringResource(R.string.amount_prefix)) },
            placeholder = { Text(stringResource(R.string.amount_placeholder)) },
            supportingText = { Text(amountError ?: stringResource(R.string.amount_helper)) },
            isError = amountError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ExpenseScreenTags.AMOUNT),
        )

        Text(stringResource(R.string.category_label), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseCategory.entries.forEach { category ->
                FilterChip(
                    selected = form.category == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(stringResource(category.labelRes())) },
                    enabled = enabled,
                    modifier = Modifier.testTag(ExpenseScreenTags.category(category)),
                )
            }
        }
        if (form.errors.categoryMissing) {
            Text(
                text = stringResource(R.string.error_category_missing),
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        OutlinedTextField(
            value = form.description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.description_label)) },
            supportingText = {
                Text(
                    if (form.errors.descriptionTooLong) {
                        stringResource(R.string.error_description_too_long, MAX_DESCRIPTION_LENGTH)
                    } else {
                        stringResource(
                            R.string.description_counter,
                            form.description.trim().length,
                            MAX_DESCRIPTION_LENGTH,
                        )
                    },
                )
            },
            isError = form.errors.descriptionTooLong,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ExpenseScreenTags.DESCRIPTION),
        )

        val dateText = remember(form.date) { formatDate(form.date.toLocalNoonMillis(TimeZone.getDefault())) }
        OutlinedButton(
            onClick = { showDatePicker = true },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ExpenseScreenTags.DATE),
        ) {
            Text(stringResource(R.string.date_button, dateText))
        }
        if (form.errors.dateInFuture) {
            Text(
                text = stringResource(R.string.error_date_in_future),
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ExpenseScreenTags.SAVE),
        ) {
            if (form.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.saving_expense))
            } else {
                Text(stringResource(R.string.save_expense))
            }
        }
    }

    if (showDatePicker) {
        TransactionDatePickerDialog(
            initialDate = form.date,
            onConfirm = {
                onDateSelect(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePickerDialog(
    initialDate: CalendarDate,
    onConfirm: (CalendarDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = remember { CalendarDate.of(System.currentTimeMillis(), TimeZone.getDefault()) }
    val selectableDates = remember(today) {
        val todayUtcMillis = today.toUtcMidnightMillis()
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
            override fun isSelectableYear(year: Int) = year <= today.year
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcMidnightMillis(),
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected != null) onConfirm(CalendarDate.fromUtcMidnightMillis(selected)) else onDismiss()
                },
            ) { Text(stringResource(R.string.date_picker_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.date_picker_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    val dateText = remember(expense.transactionDate) { formatDate(expense.transactionDate) }
    Column {
        ListItem(
            overlineContent = { Text(stringResource(expense.category.labelRes())) },
            headlineContent = {
                Text(
                    text = MoneyFormatter.format(expense.amount, expense.currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            supportingContent = if (expense.description.isNotBlank()) {
                { Text(expense.description, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            } else {
                null
            },
            trailingContent = { Text(dateText) },
        )
        HorizontalDivider()
    }
}

@Composable
private fun EmptyTransactions() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        Text(stringResource(R.string.transactions_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.transactions_empty_body), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun amountErrorText(error: AmountError): String = when (error) {
    AmountError.EMPTY -> stringResource(R.string.error_amount_empty)
    AmountError.MALFORMED -> stringResource(R.string.error_amount_malformed)
    AmountError.NEGATIVE -> stringResource(R.string.error_amount_negative)
    AmountError.ZERO -> stringResource(R.string.error_amount_zero)
    AmountError.TOO_MANY_DECIMAL_PLACES -> stringResource(R.string.error_amount_too_many_decimals)
    AmountError.TOO_LARGE -> stringResource(
        R.string.error_amount_too_large,
        MoneyFormatter.format(RupeeAmountParser.MAX_AMOUNT_PAISE, Expense.DEFAULT_CURRENCY_CODE),
    )
}

@StringRes
private fun ExpenseFormMessage.textRes(): Int = when (this) {
    ExpenseFormMessage.SAVED -> R.string.message_saved
    ExpenseFormMessage.SAVE_FAILED -> R.string.message_save_failed
}

/** Display label for a category. Exhaustive, so adding a category forces adding a label. */
@StringRes
fun ExpenseCategory.labelRes(): Int = when (this) {
    ExpenseCategory.FOOD -> R.string.category_food
    ExpenseCategory.GROCERIES -> R.string.category_groceries
    ExpenseCategory.TRANSPORT -> R.string.category_transport
    ExpenseCategory.SHOPPING -> R.string.category_shopping
    ExpenseCategory.EDUCATION -> R.string.category_education
    ExpenseCategory.HEALTH -> R.string.category_health
    ExpenseCategory.ENTERTAINMENT -> R.string.category_entertainment
    ExpenseCategory.UTILITIES -> R.string.category_utilities
    ExpenseCategory.RENT -> R.string.category_rent
    ExpenseCategory.OTHER -> R.string.category_other
}

/** Medium date in the device's locale and time zone, e.g. "28 Nov 2026". */
private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

@Preview(showBackground = true)
@Composable
private fun ExpenseScreenPreview() {
    val today = CalendarDate(2026, 11, 28)
    MoneyTrackerTheme {
        ExpenseScreen(
            form = ExpenseFormState(amountText = "125.50", category = ExpenseCategory.FOOD, date = today),
            list = ExpenseListState.Loaded(
                listOf(
                    Expense(
                        id = 1,
                        amount = 12_550,
                        category = ExpenseCategory.FOOD,
                        description = "Lunch",
                        transactionDate = today.toLocalNoonMillis(TimeZone.getDefault()),
                        createdAt = 0,
                    ),
                ),
            ),
            onAmountChange = {},
            onCategorySelect = {},
            onDescriptionChange = {},
            onDateSelect = {},
            onSave = {},
            onMessageShown = {},
        )
    }
}
