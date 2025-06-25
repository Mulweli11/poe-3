@file:OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material3 APIs like DatePicker

package com.codewithfk.expensetracker.android.feature.add_expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.base.AddExpenseNavigationEvent
import com.codewithfk.expensetracker.android.base.NavigationEvent
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.ui.theme.InterFontFamily
import com.codewithfk.expensetracker.android.ui.theme.LightGrey
import com.codewithfk.expensetracker.android.ui.theme.Typography
import com.codewithfk.expensetracker.android.widget.ExpenseTextView

/**
 * Composable function for adding a new expense or income.
 * This screen allows users to input the name, amount, and date of a transaction.
 *
 * @param navController The NavController for navigation actions.
 * @param isIncome A boolean flag indicating whether the screen is for adding income (true) or expense (false).
 * @param viewModel The ViewModel for this screen, injected by Hilt.
 */
@Composable
fun AddExpense(
    navController: NavController,
    isIncome: Boolean,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    // State to control the visibility of the dropdown menu
    val menuExpanded = remember { mutableStateOf(false) }

    // LaunchedEffect to collect navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack() // Pop back stack on navigate back event
                AddExpenseNavigationEvent.MenuOpenedClicked -> {
                    menuExpanded.value = true // Open the menu when the event is triggered
                }
                else -> {} // Handle other navigation events if necessary
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            // Create references for UI elements within the ConstraintLayout
            val (nameRow, card, topBar) = createRefs()

            // Top bar image
            Image(painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

            // Header row with back button, title, and menu icon
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                // Back button
                Image(painter = painterResource(id = R.drawable.ic_back), contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable {
                            viewModel.onEvent(AddExpenseUiEvent.OnBackPressed) // Trigger back press event
                        })
                // Screen title (Add Income or Add Expense)
                ExpenseTextView(
                    text = "Add ${if (isIncome) "Income" else "Expense"}",
                    style = Typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
                // Three dots menu icon
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Image(
                        painter = painterResource(id = R.drawable.dots_menu),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                viewModel.onEvent(AddExpenseUiEvent.OnMenuClicked) // Trigger menu click event
                            }
                    )
                    // Dropdown menu for profile and settings
                    DropdownMenu(
                        expanded = menuExpanded.value,
                        onDismissRequest = { menuExpanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { ExpenseTextView(text = "Profile") },
                            onClick = {
                                menuExpanded.value = false
                                // TODO: Implement navigation to profile screen
                                // navController.navigate("profile_route")
                            }
                        )
                        DropdownMenuItem(
                            text = { ExpenseTextView(text = "Settings") },
                            onClick = {
                                menuExpanded.value = false
                                // TODO: Implement navigation to settings screen
                                // navController.navigate("settings_route")
                            }
                        )
                    }
                }

            }
            // Data input form for adding expense/income
            DataForm(modifier = Modifier.constrainAs(card) {
                top.linkTo(nameRow.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, onAddExpenseClick = {
                viewModel.onEvent(AddExpenseUiEvent.OnAddExpenseClicked(it)) // Pass the new expense/income to the ViewModel
            }, isIncome)
        }
    }
}

/**
 * Composable function for the data input form to add an expense or income.
 * Includes fields for name, amount, and date.
 *
 * @param modifier The Modifier to be applied to the layout.
 * @param onAddExpenseClick Lambda to be invoked when the add expense/income button is clicked.
 * @param isIncome A boolean flag indicating whether the form is for adding income (true) or expense (false).
 */
@Composable
fun DataForm(
    modifier: Modifier,
    onAddExpenseClick: (model: ExpenseEntity) -> Unit,
    isIncome: Boolean
) {
    // State for input fields
    val name = remember {
        mutableStateOf("")
    }
    val amount = remember {
        mutableStateOf("")
    }
    val date = remember {
        mutableLongStateOf(0L) // Store date as a long timestamp
    }
    val dateDialogVisibility = remember {
        mutableStateOf(false) // State to control date picker dialog visibility
    }
    val type = remember {
        mutableStateOf(if (isIncome) "Income" else "Expense") // Set transaction type based on isIncome flag
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp) // Apply shadow for a card-like effect
            .clip(
                RoundedCornerShape(16.dp) // Clip with rounded corners
            )
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling for the form
    ) {
        TitleComponent(title = "name")
        // Dropdown for selecting expense/income category
        ExpenseDropDown(
            if (isIncome) listOf(
                "Paypal", "Salary", "Freelance", "Investments", "Bonus", "Rental Income", "Other Income"
            ) else listOf(
                "Grocery", "Netflix", "Rent", "Paypal", "Starbucks", "Shopping", "Transport",
                "Utilities", "Dining Out", "Entertainment", "Healthcare", "Insurance",
                "Subscriptions", "Education", "Debt Payments", "Gifts & Donations", "Travel",
                "Other Expenses"
            ),
            onItemSelected = {
                name.value = it // Update the name when an item is selected from the dropdown
            })
        Spacer(modifier = Modifier.size(24.dp))

        TitleComponent("amount")
        // OutlinedTextField for entering the amount
        OutlinedTextField(
            value = amount.value,
            onValueChange = { newValue ->
                amount.value = newValue.filter { it.isDigit() || it == '.' } // Allow only digits and a decimal point
            },
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = { text ->
                // Custom visual transformation to prepend "R " (South African Rand)
                val out = "R " + text.text
                val currencyOffsetTranslator = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        return offset + 2 // Adjust offset by 2 for "R "
                    }

                    override fun transformedToOriginal(offset: Int): Int {
                        return if (offset > 1) offset - 2 else 0 // Adjust back for original text
                    }
                }
                TransformedText(AnnotatedString(out), currencyOffsetTranslator)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Numeric keyboard
            placeholder = { ExpenseTextView(text = "Enter amount") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
            )
        )
        Spacer(modifier = Modifier.size(24.dp))

        TitleComponent("date")
        // OutlinedTextField for selecting the date (opens DatePickerDialog)
        OutlinedTextField(value = if (date.longValue == 0L) "" else Utils.formatDateToHumanReadableForm(
            date.longValue // Format the timestamp to a human-readable date string
        ),
            onValueChange = {}, // Read-only as date is selected via dialog
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dateDialogVisibility.value = true }, // Show date picker dialog on click
            enabled = false, // Disable direct input
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Black,
                disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
            ),
            placeholder = { ExpenseTextView(text = "Select date") })
        Spacer(modifier = Modifier.size(24.dp))

        // Button to add the expense/income
        Button(
            onClick = {
                // Create an ExpenseEntity object from the input values
                val model = ExpenseEntity(
                    id = null, // ID will be generated by the database
                    title = name.value,
                    amount = amount.value.toDoubleOrNull() ?: 0.0, // Convert amount to Double, default to 0.0 if invalid
                    date = Utils.formatDateToHumanReadableForm(date.longValue), // Store formatted date
                    type = type.value
                )
                onAddExpenseClick(model) // Invoke the callback with the new ExpenseEntity
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            ExpenseTextView(
                text = "Add ${if (isIncome) "Income" else "Expense"}",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
    // DatePickerDialog visibility controlled by dateDialogVisibility state
    if (dateDialogVisibility.value) {
        ExpenseDatePickerDialog(onDateSelected = {
            date.longValue = it // Update the selected date timestamp
            dateDialogVisibility.value = false // Dismiss the dialog
        }, onDismiss = {
            dateDialogVisibility.value = false // Dismiss the dialog on dismiss request
        })
    }
}

/**
 * Composable function for a custom DatePickerDialog.
 *
 * @param onDateSelected Lambda to be invoked when a date is selected.
 * @param onDismiss Lambda to be invoked when the dialog is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDatePickerDialog(
    onDateSelected: (date: Long) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState() // State for the DatePicker
    val selectedDate = datePickerState.selectedDateMillis ?: 0L // Get the selected date in milliseconds

    DatePickerDialog(
        onDismissRequest = { onDismiss() }, // Handle dismiss request
        confirmButton = {
            TextButton(onClick = { onDateSelected(selectedDate) }) { // Confirm button
                ExpenseTextView(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { // Dismiss button
                ExpenseTextView(text = "Cancel")
            }
        }) {
        DatePicker(state = datePickerState) // The actual DatePicker Composable
    }
}

/**
 * Reusable Composable for displaying a title for input sections.
 *
 * @param title The text to display as the title.
 */
@Composable
fun TitleComponent(title: String) {
    ExpenseTextView(
        text = title.uppercase(), // Display title in uppercase
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = LightGrey
    )
    Spacer(modifier = Modifier.size(10.dp)) // Spacer for vertical separation
}

/**
 * Composable function for a custom dropdown menu for selecting expense/income categories.
 *
 * @param listOfItems The list of strings to display in the dropdown.
 * @param onItemSelected Lambda to be invoked when an item is selected from the dropdown.
 */
@Composable
fun ExpenseDropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit) {
    val expanded = remember {
        mutableStateOf(false) // State to control dropdown expansion
    }
    val selectedItem = remember {
        mutableStateOf(listOfItems[0]) // Initially select the first item
    }

    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {}, // Read-only, selection is via dropdown menu
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Designates this as the anchor for the dropdown menu
            textStyle = TextStyle(fontFamily = InterFontFamily, color = Color.Black),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) // Trailing icon for dropdown
            },
            shape = RoundedCornerShape(8.dp), // Rounded corners for the text field
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            )
        )
        // Actual dropdown menu
        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            listOfItems.forEach {
                DropdownMenuItem(text = { ExpenseTextView(text = it) }, onClick = {
                    selectedItem.value = it // Update selected item
                    onItemSelected(selectedItem.value) // Invoke callback
                    expanded.value = false // Close the dropdown
                })
            }
        }
    }
}

/**
 * Preview function for the AddExpense Composable when adding income.
 */
@Preview(showBackground = true)
@Composable
fun PreviewAddExpense() {
    AddExpense(rememberNavController(), true)
}