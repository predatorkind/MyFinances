package net.vertexgraphics.myfinances.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import net.vertexgraphics.myfinances.R
import net.vertexgraphics.myfinances.BillEntity
import net.vertexgraphics.myfinances.IncomeEntity
import net.vertexgraphics.myfinances.AccountEntity
import net.vertexgraphics.myfinances.MainViewModel
import net.vertexgraphics.myfinances.BuildConfig
import net.vertexgraphics.myfinances.ui.theme.AppTextColor
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    selectedTab: String,
    onAddBill: () -> Unit,
    onEditBill: (Int) -> Unit,
    onAddIncome: () -> Unit,
    onEditIncome: (Int) -> Unit,
    onViewLog: () -> Unit,
    navController: NavController
) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val mainAccount by viewModel.mainAccount.collectAsStateWithLifecycle()
    val availableFunds by viewModel.availableFunds.collectAsStateWithLifecycle()
    val overdraft by viewModel.overdraft.collectAsStateWithLifecycle()
    val primaryIncome by viewModel.primaryIncome.collectAsStateWithLifecycle()
    val allIncomes by viewModel.allIncomes.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    val balance = mainAccount?.balance ?: 0f

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
                        if (BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text("Reset Sample Data") },
                                onClick = {
                                    viewModel.resetDebugData()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == "incomes") {
                FloatingActionButton(
                    onClick = onAddIncome,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = FloatingActionButtonDefaults.shape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Income Source",
                        tint = AppTextColor
                    )
                }
            } else {
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
                totalBillsProvider = { viewModel.getTotalMonthlyBills(bills, primaryIncome) },
                leftBillsProvider = { viewModel.getBillsLeftThisMonth(bills, primaryIncome) }
            )
            
            if (selectedTab == "incomes") {
                IncomeList(
                    incomes = allIncomes,
                    accounts = allAccounts,
                    onIncomeClick = { onEditIncome(it.id) },
                    onDeleteClick = { viewModel.deleteIncome(it) }
                )
            } else {
                BillList(
                    bills = bills,
                    incomes = allIncomes,
                    accounts = allAccounts,
                    onBillClick = { onEditBill(it.id) },
                    onPayClick = { viewModel.payBill(it) },
                    daysThreshold = 3
                )
            }
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
    incomes: List<IncomeEntity>,
    accounts: List<AccountEntity>,
    onBillClick: (BillEntity) -> Unit,
    onPayClick: (BillEntity) -> Unit,
    daysThreshold: Int
) {
    val accountMap = remember(accounts) { accounts.associate { it.id to it.name } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bills) { bill ->
            val billIncome = incomes.filter { it.accountId == bill.accountId }.minByOrNull { it.nextPay }
            val cycleStartDate = billIncome?.cycleStartDate ?: 0L
            val accountName = accountMap[bill.accountId] ?: "Unknown Account"
            BillItem(
                bill = bill,
                cycleStartDate = cycleStartDate,
                accountName = accountName,
                onClick = onBillClick,
                onPay = onPayClick,
                daysThreshold = daysThreshold
            )
        }
    }
}

@Composable
fun BillItem(
    bill: BillEntity,
    cycleStartDate: Long,
    accountName: String,
    onClick: (BillEntity) -> Unit,
    onPay: (BillEntity) -> Unit,
    daysThreshold: Int = 3
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dueDateStr = sdf.format(Date(bill.dueDate))
    
    val now = Calendar.getInstance()
    val dueDateCal = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
    val daysUntilDue = ((dueDateCal.timeInMillis - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
    
    val isPaid = cycleStartDate != 0L && bill.lastPaid >= cycleStartDate
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
                        textDecoration = if (isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        color = AppTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Due: $dueDateStr", style = MaterialTheme.typography.bodySmall, color = AppTextColor.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[$accountName]",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = String.format("%.02f", bill.amount), 
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 0.dp, end = 30.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    color = AppTextColor
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

@Composable
fun IncomeList(
    incomes: List<IncomeEntity>,
    accounts: List<AccountEntity>,
    onIncomeClick: (IncomeEntity) -> Unit,
    onDeleteClick: (IncomeEntity) -> Unit
) {
    val accountMap = remember(accounts) { accounts.associate { it.id to it.name } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(incomes) { income ->
            val accountName = accountMap[income.accountId] ?: "Unknown Account"
            IncomeItem(
                income = income,
                accountName = accountName,
                onClick = onIncomeClick,
                onDelete = onDeleteClick
            )
        }
    }
}

@Composable
fun IncomeItem(
    income: IncomeEntity,
    accountName: String,
    onClick: (IncomeEntity) -> Unit,
    onDelete: (IncomeEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val nextPayStr = sdf.format(Date(income.nextPay))
    val frequencyText = if (income.weeklyFlag) {
        val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayName = days.getOrNull(income.dayOfWeek - 1) ?: "Weekly"
        "Weekly ($dayName)"
    } else {
        "Monthly (Day ${income.dayOfMonth})"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(income) },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = income.name.ifBlank { "Income Source" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = frequencyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Next: $nextPayStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[$accountName]",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = String.format("%.02f", income.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTextColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    IconButton(onClick = { onDelete(income) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Income",
                            tint = AppTextColor
                        )
                    }
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
            cycleStartDate = 0L,
            accountName = "Main Account",
            onClick = {},
            onPay = {}
        )
    }
}
