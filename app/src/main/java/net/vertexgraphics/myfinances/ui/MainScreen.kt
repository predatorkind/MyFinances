package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import net.vertexgraphics.myfinances.R
import net.vertexgraphics.myfinances.BillEntity
import net.vertexgraphics.myfinances.Income
import net.vertexgraphics.myfinances.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddBill: () -> Unit,
    onEditBill: (Int) -> Unit,
    onViewLog: () -> Unit,
    navController: NavController
) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val balance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val availableFunds by viewModel.availableFunds.collectAsStateWithLifecycle()
    val overdraft by viewModel.overdraft.collectAsStateWithLifecycle()
    val income by viewModel.income.collectAsStateWithLifecycle()

    var showAddFundsDialog by remember { mutableStateOf(false) }
    var showSubFundsDialog by remember { mutableStateOf(false) }
    var showSetFundsDialog by remember { mutableStateOf(false) }

    if (showAddFundsDialog) {
        AdjustFundsDialog(
            operation = AdjustFundsOperation.Add,
            onDismiss = { showAddFundsDialog = false },
            onConfirm = { amount, tag -> viewModel.addFunds(amount, tag!!) }
        )
    }

    if (showSubFundsDialog) {
        AdjustFundsDialog(
            operation = AdjustFundsOperation.Deduct,
            onDismiss = { showSubFundsDialog = false },
            onConfirm = { amount, tag -> viewModel.subFunds(amount, tag!!) }
        )
    }

    if (showSetFundsDialog) {
        AdjustFundsDialog(
            operation = AdjustFundsOperation.Set(balance),
            onDismiss = { showSetFundsDialog = false },
            onConfirm = { amount, _ -> viewModel.setFunds(amount) }
        )
    }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            TopAppBar(
                title = { Text("MyFinances") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(
                        onClick = { showAddFundsDialog = true },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(painterResource(id = R.drawable.add_funds), contentDescription = "Add Funds", tint = Color.Unspecified)
                    }
                    IconButton(
                        onClick = { showSubFundsDialog = true },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(painterResource(id = R.drawable.sub_funds), contentDescription = "Deduct Funds", tint = Color.Unspecified)
                    }
                    IconButton(
                        onClick = { showSetFundsDialog = true },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(painterResource(id = R.drawable.set_funds), contentDescription = "Set Funds", tint = Color.Unspecified)
                    }
                    IconButton(
                        onClick = { expanded = true },
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add New Bill") },
                            onClick = {
                                onAddBill()
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Log") },
                            onClick = {
                                onViewLog()
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Setup Income") },
                            onClick = {
                                navController.navigate("income")
                                expanded = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSubFundsDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = FloatingActionButtonDefaults.shape
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sub_funds), 
                    contentDescription = "Deduct Funds", 
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCard(
                balance = balance,
                available = availableFunds,
                overdraft = overdraft,
                totalBillsProvider = { viewModel.getTotalMonthlyBills(bills, income) },
                leftBillsProvider = { viewModel.getBillsLeftThisMonth(bills, income) }
            )
            
            BillList(
                bills = bills,
                income = income,
                onBillClick = { onEditBill(it.id) },
                onPayClick = { viewModel.payBill(it) },
                daysThreshold = 3
            )
        }
    }
}

@Composable
fun SummaryCard(
    balance: Float, 
    available: Float, 
    overdraft: Float,
    totalBillsProvider: () -> Float,
    leftBillsProvider: () -> Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SummaryItem("Available Until Cutoff", available)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryItem("Available Until Next Pay", overdraft)
                }
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SummaryItem("Total Monthly Bills", totalBillsProvider())
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryItem("Bills Left This Month", leftBillsProvider())
                }

            }
            

        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

    }
}

@Composable
fun SummaryItem(label: String, amount: Float) {
    Column(horizontalAlignment = Alignment.Start) {
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
    income: Income?,
    onBillClick: (BillEntity) -> Unit,
    onPayClick: (BillEntity) -> Unit,
    daysThreshold: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bills) { bill ->
            BillItem(bill, income, onBillClick, onPayClick, daysThreshold)
        }
    }
}

@Composable
fun BillItem(
    bill: BillEntity,
    income: Income?,
    onClick: (BillEntity) -> Unit,
    onPay: (BillEntity) -> Unit,
    daysThreshold: Int = 3
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dueDateStr = sdf.format(Date(bill.dueDate))
    
    val now = Calendar.getInstance()
    val dueDateCal = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
    val daysUntilDue = ((dueDateCal.timeInMillis - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
    
    // Check if paid in current cycle: lastPaid must be after cycle start
    val isPaid = income != null && bill.lastPaid >= income.cycleStartDate
    val canPay = daysUntilDue <= daysThreshold && !isPaid
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(bill) },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        bill.name, 
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Text("Due: $dueDateStr", style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = String.format("%.02f", bill.amount), 
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 0.dp, end = 30.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                
                Button(
                    onClick = { onPay(bill) },
                    enabled = canPay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text(if (isPaid) "Paid" else "Pay")
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryCardPreview() {
    MaterialTheme {
        SummaryCard(
            balance = 1250.50f,
            available = 800.00f,
            overdraft = -50.00f,
            totalBillsProvider = { 0f },
            leftBillsProvider = { 0f }
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
            income = null,
            onClick = {},
            onPay = {}
        )
    }
}
