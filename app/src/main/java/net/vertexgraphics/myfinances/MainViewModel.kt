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
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val allBills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBalance: StateFlow<Float> = preferenceManager.balanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val income: StateFlow<Income?> = preferenceManager.incomeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLogs: StateFlow<List<LogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _availableFunds = MutableStateFlow(0f)
    val availableFunds: StateFlow<Float> = _availableFunds.asStateFlow()

    private val _overdraft = MutableStateFlow(0f)
    val overdraft: StateFlow<Float> = _overdraft.asStateFlow()

    init {
        viewModelScope.launch {
            combine(allBills, currentBalance, income) { bills, balance, income ->
                calculateFunds(bills, balance, income)
            }.collect()
        }
    }

    private fun calculateFunds(bills: List<BillEntity>, balance: Float, income: Income?) {
        if (income == null) return

        _availableFunds.value = balance - calculateTotalBills(bills, income, income.cutOffDate, false)
        _overdraft.value = balance - calculateTotalBills(bills, income, income.nextPay, false)
    }

    private fun calculateTotalBills(bills: List<BillEntity>, income: Income, limitTimestamp: Long, onlyFuture: Boolean): Float {
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

    fun getTotalMonthlyBills(bills: List<BillEntity>, income: Income?): Float {
        if (income == null) return 0f
        
        var total = 0f
        val startPeriod = Calendar.getInstance().apply { timeInMillis = income.cycleStartDate }
        val endPeriod = startPeriod.clone() as Calendar
        endPeriod.add(Calendar.MONTH, 1)

        for (bill in bills) {
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

    fun getBillsLeftThisMonth(bills: List<BillEntity>, income: Income?): Float {
        if (income == null) return 0f
        
        var total = 0f
        val startPeriod = Calendar.getInstance().apply { timeInMillis = income.cycleStartDate }
        val endPeriod = startPeriod.clone() as Calendar
        endPeriod.add(Calendar.MONTH, 1)

        for (bill in bills) {
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
            val newBalance = currentBalance.value + amount
            preferenceManager.updateBalance(newBalance)
            repository.insertLog("Added $amount - $tag")
        }
    }

    fun subFunds(amount: Float, tag: String) {
        viewModelScope.launch {
            val newBalance = currentBalance.value - amount
            preferenceManager.updateBalance(newBalance)
            repository.insertLog("Deducted $amount - $tag")
        }
    }

    fun setFunds(amount: Float) {
        viewModelScope.launch {
            preferenceManager.updateBalance(amount)
            repository.insertLog("Balance Set to: $amount")
        }
    }

    fun payBill(bill: BillEntity) {
        viewModelScope.launch {
            val newBalance = currentBalance.value - bill.amount
            preferenceManager.updateBalance(newBalance)
            
            val nextDueDate = GregorianCalendar().apply {
                timeInMillis = bill.dueDate
                if (bill.weekly) add(Calendar.DAY_OF_MONTH, 7)
                else add(Calendar.MONTH, 1)
            }.timeInMillis
            
            repository.updateBill(bill.copy(lastPaid = System.currentTimeMillis(), dueDate = nextDueDate))
            repository.insertLog("Paid ${bill.amount} - ${bill.name}")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
