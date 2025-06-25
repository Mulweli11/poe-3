package com.codewithfk.expensetracker.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [ExpenseEntity] data in the database.
 * This interface defines the methods for interacting with the `expense_table`.
 */
@Dao
interface ExpenseDao {

    /**
     * Retrieves all expense entries from the `expense_table`.
     * The result is emitted as a [Flow], meaning any changes to the data in the table
     * will automatically trigger a new emission to collectors.
     *
     * @return A [Flow] emitting a list of all [ExpenseEntity] objects.
     */
    @Query("SELECT * FROM expense_table")
    fun getAllExpense(): Flow<List<ExpenseEntity>>

    /**
     * Retrieves the top 5 expense entries from the `expense_table` that are of type 'Expense',
     * ordered by amount in descending order.
     *
     * @return A [Flow] emitting a list of up to 5 [ExpenseEntity] objects representing top expenses.
     */
    @Query("SELECT * FROM expense_table WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Retrieves a summary of expenses or income grouped by type and date.
     * It sums the amount for each type (defaulting to 'Expense') for each distinct date.
     * The results are ordered by date.
     *
     * @param type The type of transaction to query (e.g., "Expense" or "Income").
     * Defaults to "Expense".
     * @return A [Flow] emitting a list of [ExpenseSummary] objects.
     */
    @Query("SELECT type, date, SUM(amount) AS total_amount FROM expense_table where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<ExpenseSummary>>

    /**
     * Inserts a new [ExpenseEntity] into the database.
     * This is a suspend function, meaning it must be called from a coroutine or another suspend function.
     *
     * @param expenseEntity The [ExpenseEntity] object to be inserted.
     */
    @Insert
    suspend fun insertExpense(expenseEntity: ExpenseEntity)

    /**
     * Deletes an existing [ExpenseEntity] from the database.
     * This is a suspend function.
     *
     * @param expenseEntity The [ExpenseEntity] object to be deleted.
     */
    @Delete
    suspend fun deleteExpense(expenseEntity: ExpenseEntity)

    /**
     * Updates an existing [ExpenseEntity] in the database.
     * This is a suspend function. The `id` of the [ExpenseEntity] is used to identify the record to update.
     *
     * @param expenseEntity The [ExpenseEntity] object to be updated.
     */
    @Update
    suspend fun updateExpense(expenseEntity: ExpenseEntity)

    /**
     * Deletes all expense entries from the `expense_table`.
     * This is a suspend function.
     */
    @Query("DELETE FROM expense_table")
    suspend fun deleteAllExpenses()
}