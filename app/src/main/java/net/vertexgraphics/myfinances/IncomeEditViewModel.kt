package net.vertexgraphics.myfinances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar
import javax.inject.Inject

@HiltViewModel
class IncomeEditViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _incomeState = MutableStateFlow<IncomeEntity?>(null)
    val incomeState: StateFlow<IncomeEntity?> = _incomeState.asStateFlow()

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadIncome(id: Int) {
        viewModelScope.launch {
            if (id == -1) {
                val mainAcc = repository.getMainAccount()
                val mainAccId = mainAcc?.id ?: 0
                val initialAmount = 0f
                val initialWeeklyFlag = false
                val initialDayOfMonth = 1
                val initialDayOfWeek = 1
                val initialNextPay = calculateNextPayMonthly(initialDayOfMonth)
                val initialCutOff = calculateCutOffDate(initialNextPay)
                val initialCycleStart = calculateCycleStart(initialWeeklyFlag, initialDayOfMonth, initialNextPay)
                
                _incomeState.value = IncomeEntity(
                    name = "",
                    amount = initialAmount,
                    weeklyFlag = initialWeeklyFlag,
                    dayOfMonth = initialDayOfMonth,
                    dayOfWeek = initialDayOfWeek,
                    lastPay = 0,
                    nextPay = initialNextPay,
                    cutOffDate = initialCutOff,
                    cycleStartDate = initialCycleStart,
                    accountId = mainAccId
                )
            } else {
                _incomeState.value = repository.getIncomeById(id)
            }
        }
    }

    fun updateName(name: String) {
        _incomeState.value = _incomeState.value?.copy(name = name)
    }

    fun updateAmount(amount: Float) {
        _incomeState.value = _incomeState.value?.copy(amount = amount)
    }

    fun updateFrequency(weekly: Boolean) {
        _incomeState.value = _incomeState.value?.let { current ->
            val newNextPay = if (weekly) {
                calculateNextPayWeekly(current.dayOfWeek)
            } else {
                calculateNextPayMonthly(current.dayOfMonth)
            }
            val newCutOff = calculateCutOffDate(newNextPay)
            val newCycleStart = calculateCycleStart(weekly, current.dayOfMonth, newNextPay)
            current.copy(
                weeklyFlag = weekly,
                nextPay = newNextPay,
                cutOffDate = newCutOff,
                cycleStartDate = newCycleStart
            )
        }
    }

    fun updateDayOfMonth(day: Int) {
        _incomeState.value = _incomeState.value?.let { current ->
            val newNextPay = calculateNextPayMonthly(day)
            val newCutOff = calculateCutOffDate(newNextPay)
            val newCycleStart = calculateCycleStart(false, day, newNextPay)
            current.copy(
                dayOfMonth = day,
                nextPay = newNextPay,
                cutOffDate = newCutOff,
                cycleStartDate = newCycleStart
            )
        }
    }

    fun updateDayOfWeek(day: Int) {
        _incomeState.value = _incomeState.value?.let { current ->
            val newNextPay = calculateNextPayWeekly(day)
            val newCutOff = calculateCutOffDate(newNextPay)
            current.copy(
                dayOfWeek = day,
                nextPay = newNextPay,
                cutOffDate = newCutOff
            )
        }
    }

    fun updateLastPay(time: Long) {
        _incomeState.value = _incomeState.value?.copy(lastPay = time)
    }

    fun updateNextPay(time: Long) {
        _incomeState.value = _incomeState.value?.copy(nextPay = time)
    }

    fun updateCutOff(time: Long) {
        _incomeState.value = _incomeState.value?.copy(cutOffDate = time)
    }

    fun updateCycleStart(time: Long) {
        _incomeState.value = _incomeState.value?.copy(cycleStartDate = time)
    }

    fun updateAccountId(accountId: Int) {
        _incomeState.value = _incomeState.value?.copy(accountId = accountId)
    }

    fun save() {
        val income = _incomeState.value ?: return
        viewModelScope.launch {
            if (income.id == 0) {
                repository.insertIncome(income)
            } else {
                repository.updateIncome(income)
            }
        }
    }

    private fun calculateCycleStart(weekly: Boolean, dayOfMonth: Int, nextPay: Long): Long {
        if (weekly) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        } else {
            val cal = Calendar.getInstance()
            cal.timeInMillis = nextPay
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            return cal.timeInMillis
        }
    }

    private fun calculateNextPayMonthly(dayOfMonth: Int): Long {
        val today = Calendar.getInstance()
        val todayZero = GregorianCalendar(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        
        val targetCal = todayZero.clone() as Calendar
        val currentDay = todayZero.get(Calendar.DAY_OF_MONTH)
        
        if (currentDay >= dayOfMonth) {
            targetCal.add(Calendar.MONTH, 1)
        }
        
        val maxDay = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        targetCal.set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
        
        val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY) {
            targetCal.add(Calendar.DAY_OF_MONTH, -1)
        } else if (dayOfWeek == Calendar.SUNDAY) {
            targetCal.add(Calendar.DAY_OF_MONTH, -2)
        }
        
        return targetCal.timeInMillis
    }

    private fun calculateNextPayWeekly(dayOfWeek: Int): Long {
        val today = Calendar.getInstance()
        val todayZero = GregorianCalendar(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        
        val targetCal = todayZero.clone() as Calendar
        val currentDayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
        
        val diff = dayOfWeek - currentDayOfWeek
        targetCal.add(Calendar.DAY_OF_MONTH, diff)
        
        if (targetCal.before(todayZero) || targetCal.equals(todayZero)) {
            targetCal.add(Calendar.DAY_OF_MONTH, 7)
        }
        
        return targetCal.timeInMillis
    }

    private fun calculateCutOffDate(nextPay: Long): Long {
        if (nextPay == 0L) return 0L
        val cal = GregorianCalendar().apply {
            timeInMillis = nextPay
            add(Calendar.DAY_OF_MONTH, -1)
        }
        return cal.timeInMillis
    }
}
