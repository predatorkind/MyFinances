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
class IncomeViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _incomeState = MutableStateFlow(Income(0f, false, 1, 1, 0, 0, 0))
    val incomeState: StateFlow<Income> = _incomeState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceManager.incomeFlow.collect { income ->
                income?.let { _incomeState.value = it }
            }
        }
    }

    fun updateAmount(amount: Float) {
        _incomeState.value = _incomeState.value.copy(amount = amount)
    }

    fun updateFrequency(weekly: Boolean) {
        val currentState = _incomeState.value
        val newNextPay = if (weekly) {
            calculateNextPayWeekly(currentState.dayOfWeek)
        } else {
            calculateNextPayMonthly(currentState.dayOfMonth)
        }
        val newCutOff = calculateCutOffDate(newNextPay)
        _incomeState.value = currentState.copy(
            weeklyFlag = weekly,
            nextPay = newNextPay,
            cutOffDate = newCutOff
        )
    }

    fun updateDayOfMonth(day: Int) {
        val currentState = _incomeState.value
        val newNextPay = calculateNextPayMonthly(day)
        val newCutOff = calculateCutOffDate(newNextPay)
        _incomeState.value = currentState.copy(
            dayOfMonth = day,
            nextPay = newNextPay,
            cutOffDate = newCutOff
        )
    }

    fun updateDayOfWeek(day: Int) {
        val currentState = _incomeState.value
        val newNextPay = calculateNextPayWeekly(day)
        val newCutOff = calculateCutOffDate(newNextPay)
        _incomeState.value = currentState.copy(
            dayOfWeek = day,
            nextPay = newNextPay,
            cutOffDate = newCutOff
        )
    }

    fun updateLastPay(time: Long) {
        _incomeState.value = _incomeState.value.copy(lastPay = time)
    }

    fun updateNextPay(time: Long) {
        _incomeState.value = _incomeState.value.copy(nextPay = time)
    }

    fun updateCutOff(time: Long) {
        _incomeState.value = _incomeState.value.copy(cutOffDate = time)
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
        
        // Use add(DAY_OF_MONTH, diff) to avoid the field precedence priority issue 
        // that ignores set(DAY_OF_WEEK) on predefined/cloned instances.
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

    val debugResetOnStartup: StateFlow<Boolean> = preferenceManager.debugResetOnStartupFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateDebugResetOnStartup(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateDebugResetOnStartup(enabled)
        }
    }

    fun save() {
        val income = _incomeState.value
        viewModelScope.launch {
            preferenceManager.saveIncome(income)
        }
    }
}
