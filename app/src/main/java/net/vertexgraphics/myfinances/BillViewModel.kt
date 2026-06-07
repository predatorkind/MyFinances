package net.vertexgraphics.myfinances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _billState = MutableStateFlow<BillEntity?>(null)
    val billState: StateFlow<BillEntity?> = _billState.asStateFlow()

    fun loadBill(id: Int) {
        if (id == -1) {
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
                dueDate = getDueDate(initialWeekly, initialDayOfMonth, initialDayOfWeek)
            )
        } else {
            viewModelScope.launch {
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
        val dueDateCalendar = GregorianCalendar()
        val currentCalendar = GregorianCalendar()

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
            dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)
            if (dueDateCalendar.before(currentCalendar)) {
                dueDateCalendar.add(Calendar.MONTH, 1)
            }
        }
        return dueDateCalendar.timeInMillis
    }
}
