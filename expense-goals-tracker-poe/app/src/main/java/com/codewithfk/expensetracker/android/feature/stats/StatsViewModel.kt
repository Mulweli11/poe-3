package com.codewithfk.expensetracker.android.feature.stats

import androidx.lifecycle.ViewModel
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseSummary
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Statistics screen.
 * This ViewModel is responsible for providing data related to expense statistics,
 * such as expense trends over time and top expenses.
 *
 * @property dao The [ExpenseDao] injected by Hilt, used for database operations.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(val dao: ExpenseDao) : BaseViewModel() {

    /**
     * A Flow that emits a list of [ExpenseSummary] objects,
     * representing the total expenses grouped by date. This is used for the line chart.
     */
    val entries = dao.getAllExpenseByDate()

    /**
     * A Flow that emits a list of [ExpenseSummary] objects,
     * representing the top expenses (e.g., highest spending categories).
     */
    val topEntries = dao.getTopExpenses()

    /**
     * Converts a list of [ExpenseSummary] objects into a list of [Entry] objects
     * suitable for use with the MPAndroidChart library's LineChart.
     *
     * The X-axis values of the [Entry] objects represent the date in milliseconds (as a float),
     * and the Y-axis values represent the total amount for that date (as a float).
     *
     * @param entries The list of [ExpenseSummary] to be converted.
     * @return A list of [Entry] objects formatted for the chart.
     */
    fun getEntriesForChart(entries: List<ExpenseSummary>): List<Entry> {
        val list = mutableListOf<Entry>()
        for (entry in entries) {
            // Convert the date string from ExpenseSummary to milliseconds and then to Float for the X-axis.
            val formattedDate = Utils.getMillisFromDate(entry.date)
            // Add a new Entry with the date (as float) and total amount (as float).
            list.add(Entry(formattedDate.toFloat(), entry.total_amount.toFloat()))
        }
        return list
    }

    /**
     * Overrides the [onEvent] method from [BaseViewModel].
     * Currently, there are no specific UI events handled directly by this ViewModel,
     * as its primary role is to provide data.
     *
     * @param event The [UiEvent] to be processed (not currently used).
     */
    override fun onEvent(event: UiEvent) {
        // No UI events are processed directly in this ViewModel for now.
        // If specific actions (e.g., filtering stats by month) were needed,
        // they would be handled here.
    }
}