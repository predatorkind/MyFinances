package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.vertexgraphics.myfinances.AccountEntity
import net.vertexgraphics.myfinances.IncomeEditViewModel
import net.vertexgraphics.myfinances.ui.theme.FocusedControlColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeEditScreen(
    viewModel: IncomeEditViewModel,
    incomeId: Int,
    onBack: () -> Unit
) {
    LaunchedEffect(incomeId) {
        viewModel.loadIncome(incomeId)
    }

    val income by viewModel.incomeState.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (incomeId == -1) "New Income Source" else "Edit Income Source") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        income?.let { inc ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
                ) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = inc.name,
                                onValueChange = { viewModel.updateName(it) },
                                label = { Text("Income Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusedControlColor
                                )
                            )

                            OutlinedTextField(
                                value = inc.amount.toString(),
                                onValueChange = { it.toFloatOrNull()?.let { amt -> viewModel.updateAmount(amt) } },
                                label = { Text("Income Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusedControlColor
                                )
                            )

                            AccountSelector(
                                selectedAccountId = inc.accountId,
                                accounts = accounts,
                                onAccountSelected = { viewModel.updateAccountId(it) }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Weekly Pay", style = MaterialTheme.typography.bodyLarge)
                                Switch(
                                    checked = inc.weeklyFlag, 
                                    onCheckedChange = { viewModel.updateFrequency(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                        checkedThumbColor = Color.Black,
                                        uncheckedThumbColor = Color.Black,
                                        checkedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }

                            if (inc.weeklyFlag) {
                                DayOfWeekSelectorIncomeEdit(
                                    selectedDayIndex = inc.dayOfWeek,
                                    onDaySelected = { viewModel.updateDayOfWeek(it) }
                                )
                            } else {
                                DayOfMonthSelectorIncomeEdit(
                                    selectedDay = inc.dayOfMonth,
                                    onDaySelected = { viewModel.updateDayOfMonth(it) }
                                )
                            }

                            OutlinedTextField(
                                value = if (inc.lastPay != 0L) sdf.format(Date(inc.lastPay)) else "N/A",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Last Pay Date") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            DatePickerItemIncomeEdit(
                                label = "Cutoff Date",
                                timestamp = inc.cutOffDate,
                                onDateSelected = { viewModel.updateCutOff(it) }
                            )

                            DatePickerItemIncomeEdit(
                                label = "Next Pay Date",
                                timestamp = inc.nextPay,
                                onDateSelected = { viewModel.updateNextPay(it) }
                            )

                            DatePickerItemIncomeEdit(
                                label = "Cycle Start Date",
                                timestamp = inc.cycleStartDate,
                                onDateSelected = { viewModel.updateCycleStart(it) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.save()
                                    onBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ),
                                border = BorderStroke(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Text("Save")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayOfWeekSelectorIncomeEdit(selectedDayIndex: Int, onDaySelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = days.getOrNull(selectedDayIndex - 1) ?: "Select Day",
            onValueChange = {},
            readOnly = true,
            label = { Text("Day of Week") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Select Day")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusedControlColor
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            days.forEachIndexed { index, day ->
                DropdownMenuItem(
                    text = { Text(day) },
                    onClick = {
                        onDaySelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayOfMonthSelectorIncomeEdit(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val days = (1..31).toList()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedDay.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Day of Month") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Select Day")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusedControlColor
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.toString()) },
                    onClick = {
                        onDaySelected(day)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerItemIncomeEdit(label: String, timestamp: Long, onDateSelected: (Long) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = key(timestamp) {
        rememberDatePickerState(
            initialSelectedDateMillis = if (timestamp != 0L) timestamp else System.currentTimeMillis()
        )
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateStr = if (timestamp != 0L) sdf.format(Date(timestamp)) else "Select Date"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = dateStr,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    selectedAccountId: Int,
    accounts: List<AccountEntity>,
    onAccountSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedAccountId }
    val displayText = selectedAccount?.name ?: "Select Account"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Account") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, "Select Account")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusedControlColor
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
