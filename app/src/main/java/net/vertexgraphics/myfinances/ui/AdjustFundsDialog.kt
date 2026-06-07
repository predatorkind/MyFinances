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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import net.vertexgraphics.myfinances.ui.theme.FocusedControlColor
import net.vertexgraphics.myfinances.ui.theme.AppTextColor

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RectangleShape
                ),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surfaceBright,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusedControlColor
                        )
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
                                    .clickable { expanded = true },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusedControlColor
                                )
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = AppTextColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                amountText.toFloatOrNull()?.let {
                                    val tag = if (showCategory) selectedCategory else null
                                    onConfirm(it, tag)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}
