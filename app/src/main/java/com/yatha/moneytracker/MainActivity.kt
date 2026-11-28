package com.yatha.moneytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yatha.moneytracker.data.local.MoneyTrackerDatabase
import com.yatha.moneytracker.data.repository.ExpenseRepository
import com.yatha.moneytracker.ui.expense.ExpenseRoute
import com.yatha.moneytracker.ui.expense.ExpenseViewModel
import com.yatha.moneytracker.ui.theme.MoneyTrackerTheme

class MainActivity : ComponentActivity() {

    // Manual dependency wiring: database (process-wide singleton) -> repository -> ViewModel.
    // The ViewModel survives configuration changes such as screen rotation.
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.factory(
            ExpenseRepository(MoneyTrackerDatabase.getInstance(applicationContext).expenseDao()),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyTrackerTheme {
                ExpenseRoute(viewModel = expenseViewModel)
            }
        }
    }
}
