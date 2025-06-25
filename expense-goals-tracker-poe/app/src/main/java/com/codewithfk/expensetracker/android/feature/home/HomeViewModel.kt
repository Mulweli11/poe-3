package com.codewithfk.expensetracker.android.feature.home

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.HomeNavigationEvent
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Import withContext
import javax.inject.Inject

/**
 * ViewModel for the Home screen, responsible for providing data to the UI
 * and handling UI-related events. It interacts with the ExpenseDao to retrieve
 * expense data and calculates financial summaries.
 *
 * @property dao The Data Access Object for expenses, injected by Hilt.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(val dao: ExpenseDao) : BaseViewModel() {

    /**
     * Exposes a Flow of all expenses from the database.
     * This will automatically update the UI when the data changes.
     */
    val expenses = dao.getAllExpense()

    // --- Start of added code for "reset on entry" ---
    /**
     * Initializes the ViewModel. When the HomeViewModel is created (e.g., when Home Screen is entered),
     * it will clear all existing expense data from the database.
     *
     * IMPORTANT: This behavior will delete all user data every time the app's home screen
     * is accessed or recreated. This is typically undesirable for a production app
     * and is more suited for testing or a specific demo scenario.
     */
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.deleteAllExpenses() // Call the new method to delete all data
            }
        }
    }
    // --- End of added code for "reset on entry" ---

    /**
     * Handles various UI events triggered from the Home screen.
     *
     * @param event The [UiEvent] to process.
     */
    override fun onEvent(event: UiEvent) {
        when (event) {
            // Handles the event when the "Add Expense" button is clicked.
            is HomeUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    // Emits a navigation event to navigate to the add expense screen.
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddExpense)
                }
            }

            // Handles the event when the "Add Income" button is clicked.
            is HomeUiEvent.OnAddIncomeClicked -> {
                viewModelScope.launch {
                    // Emits a navigation event to navigate to the add income screen.
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddIncome)
                }
            }

            // Handles the event when the "See All" transactions button is clicked.
            is HomeUiEvent.OnSeeAllClicked -> {
                viewModelScope.launch {
                    // Emits a navigation event to navigate to the all transactions screen.
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToSeeAll)
                }
            }
        }
    }

    /**
     * Calculates the total balance from a list of [ExpenseEntity] objects.
     * Income increases the balance, while expenses decrease it.
     *
     * @param list The list of [ExpenseEntity] to calculate the balance from.
     * @return A formatted string representing the total balance.
     */
    fun getBalance(list: List<ExpenseEntity>): String {
        var balance = 0.0
        for (expense in list) {
            if (expense.type == "Income") {
                balance += expense.amount
            } else {
                balance -= expense.amount
            }
        }
        return Utils.formatCurrency(balance)
    }

    /**
     * Calculates the total expense from a list of [ExpenseEntity] objects.
     * Only "Expense" type transactions are included in the total.
     *
     * @param list The list of [ExpenseEntity] to calculate the total expense from.
     * @return A formatted string representing the total expense.
     */
    fun getTotalExpense(list: List<ExpenseEntity>): String {
        var total = 0.0
        for (expense in list) {
            if (expense.type != "Income") { // Assuming anything not "Income" is an "Expense"
                total += expense.amount
            }
        }
        return Utils.formatCurrency(total)
    }

    /**
     * Calculates the total income from a list of [ExpenseEntity] objects.
     * Only "Income" type transactions are included in the total.
     *
     * @param list The list of [ExpenseEntity] to calculate the total income from.
     * @return A formatted string representing the total income.
     */
    fun getTotalIncome(list: List<ExpenseEntity>): String {
        var totalIncome = 0.0
        for (expense in list) {
            if (expense.type == "Income") {
                totalIncome += expense.amount
            }
        }
        return Utils.formatCurrency(totalIncome)
    }
}

/**
 * Sealed class representing the UI events that can occur on the Home screen.
 * These events are sent from the UI to the ViewModel for processing.
 */
sealed class HomeUiEvent : UiEvent() {
    /**
     * Represents the event when the user clicks the "Add Expense" button.
     */
    data object OnAddExpenseClicked : HomeUiEvent()

    /**
     * Represents the event when the user clicks the "Add Income" button.
     */
    data object OnAddIncomeClicked : HomeUiEvent()

    /**
     * Represents the event when the user clicks the "See All" transactions button.
     */
    data object OnSeeAllClicked : HomeUiEvent()
}