package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import net.vertexgraphics.myfinances.BillViewModel
import net.vertexgraphics.myfinances.ui.theme.FocusedControlColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEditScreen(
    viewModel: BillViewModel,
    billId: Int,
    onBack: () -> Unit
) {
    LaunchedEffect(billId) {
        viewModel.loadBill(billId)
    }

    val bill by viewModel.billState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (billId == -1) "New Bill" else "Edit Bill") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (billId != -1) {
                        IconButton(onClick = {
                            viewModel.deleteBill()
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        bill?.let { b ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
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
                                value = b.name,
                                onValueChange = { viewModel.updateName(it) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusedControlColor
                                )
                            )

                            OutlinedTextField(
                                value = b.amount.toString(),
                                onValueChange = { 
                                    it.toFloatOrNull()?.let { amount -> viewModel.updateAmount(amount) }
                                },
                                label = { Text("Amount") },
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
                                Text("Weekly", style = MaterialTheme.typography.bodyLarge)
                                Switch(
                                    checked = b.weekly, 
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

                            if (b.weekly) {
                                DayOfWeekSelector(
                                    selectedDay = b.dayOfWeek,
                                    onDaySelected = { viewModel.updateDayOfWeek(it) }
                                )
                            } else {
                                DayOfMonthSelector(
                                    selectedDay = b.dayOfMonth,
                                    onDaySelected = { viewModel.updateDayOfMonth(it) }
                                )
                            }

                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            Text("Next Due Date: ${sdf.format(Date(b.dueDate))}", style = MaterialTheme.typography.bodyMedium)
                            if (b.lastPaid != 0L) {
                                Text("Last Paid: ${sdf.format(Date(b.lastPaid))}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.saveBill()
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
fun DayOfWeekSelector(selectedDay: String, onDaySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedDay,
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
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day) },
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
fun DayOfMonthSelector(selectedDay: Int, onDaySelected: (Int) -> Unit) {
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
