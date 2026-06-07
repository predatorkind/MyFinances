package net.vertexgraphics.myfinances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _billState = MutableStateFlow<BillEntity?>(null)
    val billState: StateFlow<BillEntity?> = _billState.asStateFlow()

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadBill(id: Int) {
        viewModelScope.launch {
            if (id == -1) {
                val mainAcc = repository.getMainAccount()
                val mainAccId = mainAcc?.id ?: 0
                val initialWeekly = true
                val initialDayOfMonth = 1
                val initialDayOfWeek = "Monday"
                _billState.value = BillEntity(
                    name = "",
                    amount = 0f,
                    weekly = initialWeekly,
                    dayOfMonth = initialDayOfMonth,
                    dayOfWeek = initialDayOfWeek,
                    lastPaid = 0,
                    dueDate = getDueDate(initialWeekly, initialDayOfMonth, initialDayOfWeek),
                    accountId = mainAccId
                )
            } else {
                _billState.value = repository.getBillById(id)
            }
        }
    }

    fun updateName(name: String) {
        _billState.value = _billState.value?.copy(name = name)
    }

    fun updateAmount(amount: Float) {
        _billState.value = _billState.value?.copy(amount = amount)
    }

    fun updateFrequency(weekly: Boolean) {
        _billState.value = _billState.value?.let { current ->
            val updated = current.copy(weekly = weekly)
            updated.copy(dueDate = getDueDate(updated.weekly, updated.dayOfMonth, updated.dayOfWeek))
        }
    }

    fun updateDayOfMonth(day: Int) {
        _billState.value = _billState.value?.let { current ->
            val updated = current.copy(dayOfMonth = day)
            updated.copy(dueDate = getDueDate(updated.weekly, updated.dayOfMonth, updated.dayOfWeek))
        }
    }

    fun updateDayOfWeek(day: String) {
        _billState.value = _billState.value?.let { current ->
            val updated = current.copy(dayOfWeek = day)
            updated.copy(dueDate = getDueDate(updated.weekly, updated.dayOfMonth, updated.dayOfWeek))
        }
    }

    fun updateAccountId(accountId: Int) {
        _billState.value = _billState.value?.copy(accountId = accountId)
    }

    fun saveBill() {
        val bill = _billState.value ?: return
        val dueDate = getDueDate(bill.weekly, bill.dayOfMonth, bill.dayOfWeek)
        val finalBill = bill.copy(dueDate = dueDate)
        
        viewModelScope.launch {
            if (finalBill.id == 0) {
                repository.insertBill(finalBill)
            } else {
                repository.updateBill(finalBill)
            }
        }
    }

    fun deleteBill() {
        val bill = _billState.value ?: return
        viewModelScope.launch {
            if (bill.id != 0) {
                repository.deleteBill(bill)
            }
        }
    }

    private fun getDueDate(weekly: Boolean, dayNumber: Int, day: String): Long {
        val dueDateCalendar = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentCalendar = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (weekly) {
            val dayOfWeek = when (day) {
                "Sunday" -> Calendar.SUNDAY
                "Monday" -> Calendar.MONDAY
                "Tuesday" -> Calendar.TUESDAY
                "Wednesday" -> Calendar.WEDNESDAY
                "Thursday" -> Calendar.THURSDAY
                "Friday" -> Calendar.FRIDAY
                else -> Calendar.SATURDAY
            }
            dueDateCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            if (dueDateCalendar.before(currentCalendar)) {
                dueDateCalendar.add(Calendar.DAY_OF_WEEK, 7)
            }
        } else {
            val maxDay = dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            dueDateCalendar.set(Calendar.DAY_OF_MONTH, minOf(dayNumber, maxDay))
            if (dueDateCalendar.before(currentCalendar)) {
                dueDateCalendar.add(Calendar.MONTH, 1)
                val newMaxDay = dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                dueDateCalendar.set(Calendar.DAY_OF_MONTH, minOf(dayNumber, newMaxDay))
            }
        }
        return dueDateCalendar.timeInMillis
    }
}
