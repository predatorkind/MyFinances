package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.vertexgraphics.myfinances.BillEntity
import net.vertexgraphics.myfinances.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun SummaryCardPreview() {
    MaterialTheme {
        SummaryCard(
            balance = 1250.50f, 
            available = 800.00f, 
            overdraft = -50.00f,
            onAddFunds = {},
            onSubFunds = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BillItemPreview() {
    MaterialTheme {
        BillItem(
            bill = BillEntity(
                id = 1,
                name = "Electricity",
                amount = 120.0f,
                weekly = false,
                dayOfMonth = 15,
                dayOfWeek = "Monday",
                lastPaid = 0,
                dueDate = System.currentTimeMillis()
            ),
            onClick = {},
            onPay = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddBill: () -> Unit,
    onEditBill: (Int) -> Unit,
    onViewLog: () -> Unit
) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val balance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val availableFunds by viewModel.availableFunds.collectAsStateWithLifecycle()
    val overdraft by viewModel.overdraft.collectAsStateWithLifecycle()

    var showAddFundsDialog by remember { mutableStateOf(false) }
    var showSubFundsDialog by remember { mutableStateOf(false) }

    if (showAddFundsDialog) {
        AddSubFundsDialog(
            isAdding = true,
            onDismiss = { showAddFundsDialog = false },
            onConfirm = { amount, tag -> viewModel.addFunds(amount, tag) }
        )
    }

    if (showSubFundsDialog) {
        AddSubFundsDialog(
            isAdding = false,
            onDismiss = { showSubFundsDialog = false },
            onConfirm = { amount, tag -> viewModel.subFunds(amount, tag) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MyFinances") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onViewLog) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "View Log")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBill) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCard(
                balance, 
                availableFunds, 
                overdraft,
                onAddFunds = { showAddFundsDialog = true },
                onSubFunds = { showSubFundsDialog = true }
            )
            
            BillList(
                bills = bills,
                onBillClick = { onEditBill(it.id) },
                onPayClick = { viewModel.payBill(it) }
            )
        }
    }
}

@Composable
fun SummaryCard(
    balance: Float, 
    available: Float, 
    overdraft: Float,
    onAddFunds: () -> Unit,
    onSubFunds: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Current Balance", style = MaterialTheme.typography.labelMedium)
            Text(
                String.format("%.02f", balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem("Available", available)
                SummaryItem("Overdraft", overdraft)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onAddFunds,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
                Button(
                    onClick = onSubFunds,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Deduct")
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(
            String.format("%.02f", amount),
            color = if (amount < 0) Color.Red else Color.Unspecified,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BillList(
    bills: List<BillEntity>,
    onBillClick: (BillEntity) -> Unit,
    onPayClick: (BillEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bills) { bill ->
            BillItem(bill, onBillClick, onPayClick)
        }
    }
}

@Composable
fun BillItem(
    bill: BillEntity,
    onClick: (BillEntity) -> Unit,
    onPay: (BillEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dueDateStr = sdf.format(Date(bill.dueDate))
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(bill) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(bill.name, style = MaterialTheme.typography.titleMedium)
                Text("Due: $dueDateStr", style = MaterialTheme.typography.bodySmall)
                Text(String.format("%.02f", bill.amount), style = MaterialTheme.typography.bodyMedium)
            }
            
            Button(onClick = { onPay(bill) }) {
                Text("Pay")
            }
        }
    }
}
