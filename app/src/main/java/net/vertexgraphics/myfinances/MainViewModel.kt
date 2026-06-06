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

    private val _availableFunds = MutableStateFlow(0f)
    val availableFunds: StateFlow<Float> = _availableFunds.asStateFlow()

    private val _overdraft = MutableStateFlow(0f)
    val overdraft: StateFlow<Float> = _overdraft.asStateFlow()

    val allLogs: StateFlow<List<LogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(allBills, currentBalance, preferenceManager.incomeFlow) { bills, balance, income ->
                calculateFunds(bills, balance, income)
            }.collect()
        }
    }

    private fun calculateFunds(bills: List<BillEntity>, balance: Float, income: Income?) {
        if (income == null) return

        var funds = balance
        var over = balance
        
        val cutOffCalendar = GregorianCalendar().apply { timeInMillis = income.cutOffDate }
        val nextPay = GregorianCalendar().apply { timeInMillis = income.nextPay }
        
        for (bill in bills) {
            val compareCalendar = GregorianCalendar().apply { timeInMillis = bill.dueDate }
            
            // Funds available calculation
            while (compareCalendar.before(cutOffCalendar)) {
                funds -= bill.amount
                if (bill.weekly) compareCalendar.add(Calendar.DAY_OF_MONTH, 7)
                else compareCalendar.add(Calendar.MONTH, 1)
            }
            
            // Overdraft calculation
            compareCalendar.timeInMillis = bill.dueDate
            while (compareCalendar.before(nextPay)) {
                over -= bill.amount
                if (bill.weekly) compareCalendar.add(Calendar.DAY_OF_MONTH, 7)
                else compareCalendar.add(Calendar.MONTH, 1)
            }
        }
        
        // Add future income to funds available
        val incomeCompare = GregorianCalendar().apply { timeInMillis = income.nextPay }
        while (incomeCompare.before(cutOffCalendar)) {
            funds += income.amount
            if (income.weeklyFlag) incomeCompare.add(Calendar.DAY_OF_MONTH, 7)
            else incomeCompare.add(Calendar.MONTH, 1)
        }

        _availableFunds.value = funds
        _overdraft.value = over
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

    fun payBill(bill: BillEntity) {
        viewModelScope.launch {
            val newBalance = currentBalance.value - bill.amount
            preferenceManager.updateBalance(newBalance)
            
            val nextDueDate = GregorianCalendar().apply {
                timeInMillis = bill.dueDate
                if (bill.weekly) add(Calendar.WEEK_OF_MONTH, 1)
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
