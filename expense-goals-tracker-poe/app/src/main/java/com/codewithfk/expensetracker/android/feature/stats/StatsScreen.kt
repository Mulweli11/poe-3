package com.codewithfk.expensetracker.android.feature.stats

import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.feature.home.TransactionList
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

/**
 * Composable function for the Statistics screen.
 * Displays a line chart of expenses over time and a list of top spending.
 *
 * @param navController The [NavController] to handle navigation events.
 * @param viewModel The [StatsViewModel] for providing data to the UI, injected by Hilt.
 */
@Composable
fun StatsScreen(navController: NavController, viewModel: StatsViewModel = hiltViewModel()) {
    Scaffold(
        // Top app bar for the Statistics screen
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                // Back button
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier.align(
                        Alignment.CenterStart
                    ).clickable {
                        navController.navigateUp() // Navigate back on click
                    },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline) // Tint the icon
                )
                // Screen title
                ExpenseTextView(
                    text = "Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
                // Three dots menu icon (currently not interactive)
                Image(
                    painter = painterResource(id = R.drawable.dots_menu),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black)
                )
            }
        }) { paddingValues -> // Content of the Scaffold
        // Collect expense data and top expense data from the ViewModel's Flow
        val dataState = viewModel.entries.collectAsState(emptyList())
        val topExpense = viewModel.topEntries.collectAsState(initial = emptyList())

        Column(modifier = Modifier.padding(paddingValues)) {
            // Prepare entries for the line chart
            val entries = viewModel.getEntriesForChart(dataState.value)
            // Display the LineChart Composable
            LineChart(entries = entries)
            Spacer(modifier = Modifier.height(16.dp))
            // Display the list of top spending transactions
            TransactionList(
                modifier = Modifier,
                list = topExpense.value,
                title = "Top Spending",
                onSeeAllClicked = { /* No action on "See All" for now */ }
            )
        }
    }
}

/**
 * Composable function that integrates an Android `LineChart` (from MPAndroidChart library)
 * into a Compose UI.
 *
 * @param entries A list of [Entry] objects representing the data points for the line chart.
 */
@Composable
fun LineChart(entries: List<Entry>) {
    val context = LocalContext.current // Get the current Android context

    // Use AndroidView to embed the traditional Android View (LineChart) into Compose
    AndroidView(
        factory = {
            // Inflate the layout XML for the LineChart
            val view = LayoutInflater.from(context).inflate(R.layout.stats_line_chart, null)
            view
        }, modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Set the height of the chart
    ) { view ->
        // Find the LineChart by its ID from the inflated layout
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        // Create a LineDataSet from the provided entries
        val dataSet = LineDataSet(entries, "Expenses").apply {
            color = android.graphics.Color.parseColor("#FF2F7E79") // Set line color
            valueTextColor = android.graphics.Color.BLACK // Set value text color
            lineWidth = 3f // Set line width
            axisDependency = YAxis.AxisDependency.RIGHT // Associate with right Y-axis
            setDrawFilled(true) // Enable filling below the line
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth the line curves
            valueTextSize = 12f // Set value text size
            valueTextColor = android.graphics.Color.parseColor("#FF2F7E79") // Set value text color
            // Set gradient drawable for filling the area below the line
            val drawable = ContextCompat.getDrawable(context, R.drawable.char_gradient)
            drawable?.let {
                fillDrawable = it
            }
        }

        // Set custom value formatter for X-axis to display dates
        lineChart.xAxis.valueFormatter =
            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    // Format the float value (which represents a timestamp) to a human-readable date
                    return Utils.formatDateForChart(value.toLong())
                }
            }
        // Set the data for the chart
        lineChart.data = com.github.mikephil.charting.data.LineData(dataSet)
        // Disable left and right Y-axes
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
        // Disable grid lines for right and left Y-axes
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        // Disable grid lines and axis line for X-axis
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // Position X-axis at the bottom
        lineChart.invalidate() // Refresh the chart to display changes
    }
}