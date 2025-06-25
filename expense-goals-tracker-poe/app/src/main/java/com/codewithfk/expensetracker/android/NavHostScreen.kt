package com.codewithfk.expensetracker.android

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.feature.add_expense.AddExpense
import com.codewithfk.expensetracker.android.feature.home.HomeScreen
import com.codewithfk.expensetracker.android.feature.stats.StatsScreen
import com.codewithfk.expensetracker.android.feature.transactionlist.TransactionListScreen
import com.codewithfk.expensetracker.android.ui.theme.Zinc

/**
 * The main Composable function that sets up the navigation structure of the Expense Tracker app.
 * It uses a [Scaffold] to provide a basic layout with a [BottomAppBar] and manages the visibility
 * of the bottom bar based on the current navigation route.
 */
@Composable
fun NavHostScreen() {
    // Remember a NavController to manage the navigation state
    val navController = rememberNavController()

    // State to control the visibility of the bottom navigation bar
    var bottomBarVisibility by remember {
        mutableStateOf(true)
    }

    Scaffold(
        // Define the bottom bar of the Scaffold
        bottomBar = {
            // Animate the visibility of the bottom bar
            AnimatedVisibility(visible = bottomBarVisibility) {
                NavigationBottomBar(
                    navController = navController,
                    // Define the items to be displayed in the bottom navigation bar
                    items = listOf(
                        NavItem(route = "/home", icon = R.drawable.ic_home),
                        NavItem(route = "/stats", icon = R.drawable.ic_stats)
                    )
                )
            }
        }
    ) { paddingValues ->
        // Define the navigation graph using NavHost
        NavHost(
            navController = navController,
            startDestination = "/home", // Set the starting destination of the navigation graph
            modifier = Modifier.padding(paddingValues) // Apply padding from the Scaffold
        ) {
            // Composable for the Home screen
            composable(route = "/home") {
                bottomBarVisibility = true // Show bottom bar on Home screen
                HomeScreen(navController)
            }

            // Composable for adding income
            composable(route = "/add_income") {
                bottomBarVisibility = false // Hide bottom bar when adding income
                AddExpense(navController, isIncome = true)
            }

            // Composable for adding expense
            composable(route = "/add_exp") {
                bottomBarVisibility = false // Hide bottom bar when adding expense
                AddExpense(navController, isIncome = false)
            }

            // Composable for the Statistics screen
            composable(route = "/stats") {
                bottomBarVisibility = true // Show bottom bar on Stats screen
                StatsScreen(navController)
            }

            // Composable for displaying all transactions
            composable(route = "/all_transactions") {
                // Determine whether to show the bottom bar based on your UX design for this screen.
                // For a list of all transactions, it's common to keep the bottom bar for easy navigation back.
                bottomBarVisibility = true
                TransactionListScreen(navController)
            }
        }
    }
}

/**
 * Data class representing an item in the navigation bar.
 *
 * @param route The navigation route associated with the item.
 * @param icon The resource ID of the icon for the item.
 */
data class NavItem(
    val route: String,
    val icon: Int
)

/**
 * Composable function for the custom bottom navigation bar.
 *
 * @param navController The [NavController] to handle navigation actions when items are clicked.
 * @param items A list of [NavItem] objects to display in the bottom bar.
 */
@Composable
fun NavigationBottomBar(
    navController: NavController,
    items: List<NavItem>
) {
    // Get the current back stack entry to determine the currently selected route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route, // Determine if the current item is selected
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to avoid building up a large back stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true // Save the state of the popped destinations
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    // Display the icon for the navigation item
                    Icon(painter = painterResource(id = item.icon), contentDescription = null)
                },
                alwaysShowLabel = false, // Do not always show the label (only show when selected)
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Zinc, // Color for selected text
                    selectedIconColor = Zinc, // Color for selected icon
                    unselectedTextColor = Color.Gray, // Color for unselected text
                    unselectedIconColor = Color.Gray // Color for unselected icon
                )
            )
        }
    }
}