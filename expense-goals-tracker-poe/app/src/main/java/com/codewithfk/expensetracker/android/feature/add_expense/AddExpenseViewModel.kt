package com.codewithfk.expensetracker.android.feature.add_expense

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.AddExpenseNavigationEvent
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.NavigationEvent
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Add Expense/Income screen.
 * This ViewModel handles the business logic for adding new expense or income records
 * and manages navigation events related to this screen.
 *
 * @property dao The [ExpenseDao] injected by Hilt, used for database operations.
 */
@HiltViewModel
class AddExpenseViewModel @Inject constructor(val dao: ExpenseDao) : BaseViewModel() {

    /**
     * Inserts a new [ExpenseEntity] into the database.
     * This is a suspend function, meaning it can be paused and resumed later,
     * which is suitable for database operations.
     *
     * @param expenseEntity The [ExpenseEntity] object to be inserted.
     * @return `true` if the insertion was successful, `false` otherwise.
     */
    suspend fun addExpense(expenseEntity: ExpenseEntity): Boolean {
        return try {
            dao.insertExpense(expenseEntity) // Attempt to insert the expense
            true // Return true on success
        } catch (ex: Throwable) {
            // Log the exception if needed for debugging
            false // Return false if an error occurs during insertion
        }
    }

    /**
     * Handles various UI events originating from the Add Expense/Income screen.
     *
     * @param event The [UiEvent] triggered by the UI.
     */
    override fun onEvent(event: UiEvent) {
        when (event) {
            // Handles the event when the "Add Expense/Income" button is clicked.
            is AddExpenseUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    // Switch to the IO dispatcher for database operations to avoid blocking the main thread.
                    withContext(Dispatchers.IO) {
                        val result = addExpense(event.expenseEntity) // Call the suspend function to add the expense
                        if (result) {
                            // If the expense was added successfully, emit a navigation event to go back.
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            // Handles the event when the back button is pressed.
            is AddExpenseUiEvent.OnBackPressed -> {
                viewModelScope.launch {
                    // Emit a navigation event to go back to the previous screen.
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            // Handles the event when the menu icon (three dots) is clicked.
            is AddExpenseUiEvent.OnMenuClicked -> {
                viewModelScope.launch {
                    // Emit a specific navigation event to indicate that the menu should be opened.
                    _navigationEvent.emit(AddExpenseNavigationEvent.MenuOpenedClicked)
                }
            }
        }
    }
}

/**
 * Sealed class representing the UI events that can occur on the Add Expense/Income screen.
 * These events are sent from the UI to the [AddExpenseViewModel] for processing.
 */
sealed class AddExpenseUiEvent : UiEvent() {
    /**
     * Represents the event when the user clicks to add an expense or income.
     *
     * @param expenseEntity The [ExpenseEntity] object containing the details of the new transaction.
     */
    data class OnAddExpenseClicked(val expenseEntity: ExpenseEntity) : AddExpenseUiEvent()

    /**
     * Represents the event when the user clicks the back button.
     */
    object OnBackPressed : AddExpenseUiEvent()

    /**
     * Represents the event when the user clicks the menu icon.
     */
    object OnMenuClicked : AddExpenseUiEvent()
}