package net.vertexgraphics.myfinances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val database: MyFinancesDatabase,
    private val preferenceManager: PreferenceManager,
    private val dataMigrator: DataMigrator
) : ViewModel() {

    val allBills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainAccount: StateFlow<AccountEntity?> = repository.mainAccountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allIncomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<LogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _availableFunds = MutableStateFlow(0f)
    val availableFunds: StateFlow<Float> = _availableFunds.asStateFlow()

    private val _overdraft = MutableStateFlow(0f)
    val overdraft: StateFlow<Float> = _overdraft.asStateFlow()

    // Expose the primary income of the main account for the summary cards
    val primaryIncome: StateFlow<IncomeEntity?> = combine(allIncomes, mainAccount) { incomes, mainAcc ->
        if (mainAcc == null) null
        else incomes.filter { it.accountId == mainAcc.id }.minByOrNull { it.nextPay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            combine(allBills, mainAccount, allIncomes) { bills, mainAcc, incomes ->
                calculateFunds(bills, mainAcc, incomes)
            }.collect()
        }
    }

    private fun calculateFunds(bills: List<BillEntity>, mainAcc: AccountEntity?, incomes: List<IncomeEntity>) {
        if (mainAcc == null) {
            _availableFunds.value = 0f
            _overdraft.value = 0f
            return
        }

        val balance = mainAcc.balance
        val mainAccBills = bills.filter { it.accountId == mainAcc.id }
        val mainAccIncomes = incomes.filter { it.accountId == mainAcc.id }
        val primaryInc = mainAccIncomes.minByOrNull { it.nextPay }

        if (primaryInc == null) {
            _availableFunds.value = balance
            _overdraft.value = balance
            return
        }

        _availableFunds.value = balance - calculateTotalBills(mainAccBills, primaryInc, primaryInc.cutOffDate, false)
        _overdraft.value = balance - calculateTotalBills(mainAccBills, primaryInc, primaryInc.nextPay, false)
    }

    private fun calculateTotalBills(bills: List<BillEntity>, income: IncomeEntity, limitTimestamp: Long, onlyFuture: Boolean): Float {
        var total = 0f
        val now = Calendar.getInstance()
        val limit = Calendar.getInstance().apply { timeInMillis = limitTimestamp }
        
        for (bill in bills) {
            val tempDate = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
            
            while (tempDate.before(limit)) {
                if (!onlyFuture || tempDate.after(now)) {
                    total += bill.amount
                }
                if (bill.weekly) tempDate.add(Calendar.DAY_OF_MONTH, 7)
                else tempDate.add(Calendar.MONTH, 1)
            }
        }
        return total
    }

    fun getTotalMonthlyBills(bills: List<BillEntity>, income: IncomeEntity?): Float {
        if (income == null) return 0f
        
        var total = 0f
        val startPeriod = Calendar.getInstance().apply { timeInMillis = income.cycleStartDate }
        val endPeriod = startPeriod.clone() as Calendar
        endPeriod.add(Calendar.MONTH, 1)

        val mainAccId = income.accountId
        val filteredBills = bills.filter { it.accountId == mainAccId }

        for (bill in filteredBills) {
            val lastPaidCal = Calendar.getInstance().apply { timeInMillis = bill.lastPaid }
            val tempDate = if (bill.lastPaid != 0L && lastPaidCal.after(startPeriod)) {
                lastPaidCal
            } else {
                Calendar.getInstance().apply { timeInMillis = bill.dueDate }
            }

            // Adjust start date for weekly bills if they were paid within the current cycle
            if (bill.weekly && bill.lastPaid != 0L && lastPaidCal.after(startPeriod)) {
                while (tempDate.after(startPeriod)) {
                    tempDate.add(Calendar.DAY_OF_MONTH, -7)
                }
            }

            while (tempDate.before(endPeriod)) {
                if (!tempDate.before(startPeriod)) {
                    total += bill.amount
                }
                if (bill.weekly) tempDate.add(Calendar.DAY_OF_MONTH, 7)
                else tempDate.add(Calendar.MONTH, 1)
            }
        }
        return total
    }

    fun getBillsLeftThisMonth(bills: List<BillEntity>, income: IncomeEntity?): Float {
        if (income == null) return 0f
        
        var total = 0f
        val startPeriod = Calendar.getInstance().apply { timeInMillis = income.cycleStartDate }
        val endPeriod = startPeriod.clone() as Calendar
        endPeriod.add(Calendar.MONTH, 1)

        val mainAccId = income.accountId
        val filteredBills = bills.filter { it.accountId == mainAccId }

        for (bill in filteredBills) {
            val dueDate = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
            val tempDate = dueDate
            
            while (tempDate.before(endPeriod)) {
                total += bill.amount

                if (bill.weekly) tempDate.add(Calendar.DAY_OF_MONTH, 7)
                else tempDate.add(Calendar.MONTH, 1)
            }
        }
        return total
    }

    fun addFunds(amount: Float, tag: String) {
        viewModelScope.launch {
            val mainAcc = mainAccount.value ?: return@launch
            val newBalance = mainAcc.balance + amount
            repository.updateAccount(mainAcc.copy(balance = newBalance))
            repository.insertLog("Added $amount to ${mainAcc.name} - $tag")
        }
    }

    fun subFunds(amount: Float, tag: String) {
        viewModelScope.launch {
            val mainAcc = mainAccount.value ?: return@launch
            val newBalance = mainAcc.balance - amount
            repository.updateAccount(mainAcc.copy(balance = newBalance))
            repository.insertLog("Deducted $amount from ${mainAcc.name} - $tag")
        }
    }

    fun setFunds(amount: Float) {
        viewModelScope.launch {
            val mainAcc = mainAccount.value ?: return@launch
            repository.updateAccount(mainAcc.copy(balance = amount))
            repository.insertLog("Balance of ${mainAcc.name} Set to: $amount")
        }
    }

    fun payBill(bill: BillEntity) {
        viewModelScope.launch {
            val account = repository.getAccountById(bill.accountId) ?: repository.getMainAccount() ?: return@launch
            val newBalance = account.balance - bill.amount
            repository.updateAccount(account.copy(balance = newBalance))
            
            val nextDueDate = GregorianCalendar().apply {
                timeInMillis = bill.dueDate
                if (bill.weekly) add(Calendar.DAY_OF_MONTH, 7)
                else add(Calendar.MONTH, 1)
            }.timeInMillis
            
            repository.updateBill(bill.copy(lastPaid = System.currentTimeMillis(), dueDate = nextDueDate))
            repository.insertLog("Paid ${bill.amount} from ${account.name} - ${bill.name}")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun selectMainAccount(accountId: Int) {
        viewModelScope.launch {
            repository.setMainAccount(accountId)
        }
    }

    fun insertAccount(name: String, balance: Float) {
        viewModelScope.launch {
            repository.insertAccount(
                AccountEntity(
                    name = name,
                    balance = balance,
                    isMain = false
                )
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun resetDebugData() {
        viewModelScope.launch {
            preferenceManager.updateDebugResetOnStartup(true)
            preferenceManager.updateDebugSampleDataPopulated(false)
            DebugSampleData.populateIfNeeded(database, preferenceManager)
            dataMigrator.migrateIfNeeded()
            repository.checkAndProcessIncomes()
            preferenceManager.updateDebugResetOnStartup(false)
        }
    }
}
