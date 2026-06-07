package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

sealed class AdjustFundsOperation {
    object Add : AdjustFundsOperation()
    object Deduct : AdjustFundsOperation()
    data class Set(val currentBalance: Float) : AdjustFundsOperation()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustFundsDialog(
    operation: AdjustFundsOperation,
    onDismiss: () -> Unit,
    onConfirm: (Float, String?) -> Unit // Tag is nullable for Set operation
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val title = when (operation) {
        AdjustFundsOperation.Add -> "Add Funds"
        AdjustFundsOperation.Deduct -> "Deduct Funds"
        is AdjustFundsOperation.Set -> "Set Funds"
    }

    val showCategory = operation !is AdjustFundsOperation.Set
    val categories = when (operation) {
        AdjustFundsOperation.Add -> listOf("Income", "Overtime", "Loan", "Winnings", "Transfer", "Other")
        AdjustFundsOperation.Deduct -> listOf("Food", "Shopping", "Installment", "Savings", "Transfer", "Cash", "Other")
        is AdjustFundsOperation.Set -> emptyList()
    }

    if (showCategory && selectedCategory.isEmpty()) {
        selectedCategory = categories.first()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showCategory) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Category")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountText.toFloatOrNull()?.let {
                        val tag = if (showCategory) selectedCategory else null
                        onConfirm(it, tag)
                        onDismiss()
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
