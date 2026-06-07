package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.vertexgraphics.myfinances.IncomeViewModel
import net.vertexgraphics.myfinances.BuildConfig
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import net.vertexgraphics.myfinances.ui.theme.FocusedControlColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    viewModel: IncomeViewModel,
    onBack: () -> Unit
) {
    val income by viewModel.incomeState.collectAsStateWithLifecycle()
    val debugResetOnStartup by viewModel.debugResetOnStartup.collectAsStateWithLifecycle()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income Settings") },
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
                            value = income.amount.toString(),
                            onValueChange = { it.toFloatOrNull()?.let { amt -> viewModel.updateAmount(amt) } },
                            label = { Text("Income Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusedControlColor
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weekly Pay", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = income.weeklyFlag, 
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

                        if (income.weeklyFlag) {
                            DayOfWeekSelectorIncome(
                                selectedDayIndex = income.dayOfWeek,
                                onDaySelected = { viewModel.updateDayOfWeek(it) }
                            )
                        } else {
                            DayOfMonthSelectorIncome(
                                selectedDay = income.dayOfMonth,
                                onDaySelected = { viewModel.updateDayOfMonth(it) }
                            )
                        }

                        OutlinedTextField(
                            value = if (income.lastPay != 0L) sdf.format(Date(income.lastPay)) else "N/A",
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

                        DatePickerItem(
                            label = "Cutoff Date",
                            timestamp = income.cutOffDate,
                            onDateSelected = { viewModel.updateCutOff(it) }
                        )

                        DatePickerItem(
                            label = "Next Pay Date",
                            timestamp = income.nextPay,
                            onDateSelected = { viewModel.updateNextPay(it) }
                        )

                        DatePickerItem(
                            label = "Cycle Start Date",
                            timestamp = income.cycleStartDate,
                            onDateSelected = { viewModel.updateCycleStart(it) }
                        )

                        if (BuildConfig.DEBUG) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = "Debug Options",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reset with Sample Data on Startup")
                                Switch(
                                    checked = debugResetOnStartup,
                                    onCheckedChange = { viewModel.updateDebugResetOnStartup(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                        checkedThumbColor = Color.Black,
                                        uncheckedThumbColor = Color.Black,
                                        checkedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayOfWeekSelectorIncome(selectedDayIndex: Int, onDaySelected: (Int) -> Unit) {
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
fun DayOfMonthSelectorIncome(selectedDay: Int, onDaySelected: (Int) -> Unit) {
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
fun DatePickerItem(label: String, timestamp: Long, onDateSelected: (Long) -> Unit) {
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
