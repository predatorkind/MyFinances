package net.vertexgraphics.myfinances

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.GregorianCalendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val billDao: BillDao,
    private val logDao: LogDao,
    private val accountDao: AccountDao,
    private val incomeDao: IncomeDao
) {
    // Bills
    val allBills: Flow<List<BillEntity>> = billDao.getAll()

    suspend fun insertBill(bill: BillEntity) {
        billDao.insert(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.update(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.delete(bill)
    }

    suspend fun deleteBillById(id: Int) {
        billDao.deleteById(id)
    }

    suspend fun getBillById(id: Int): BillEntity? {
        return billDao.getById(id)
    }

    // Accounts
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAll()
    val mainAccountFlow: Flow<AccountEntity?> = accountDao.getMainAccountFlow()

    suspend fun insertAccount(account: AccountEntity): Long {
        return accountDao.insert(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.update(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.delete(account)
    }

    suspend fun getAccountById(id: Int): AccountEntity? {
        return accountDao.getById(id)
    }

    suspend fun getMainAccount(): AccountEntity? {
        return accountDao.getMainAccount()
    }

    suspend fun setMainAccount(accountId: Int) {
        accountDao.unsetMainAccounts()
        val account = accountDao.getById(accountId) ?: return
        accountDao.update(account.copy(isMain = true))
    }

    // Incomes
    val allIncomes: Flow<List<IncomeEntity>> = incomeDao.getAll()

    suspend fun insertIncome(income: IncomeEntity): Long {
        return incomeDao.insert(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.update(income)
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.delete(income)
    }

    suspend fun getIncomeById(id: Int): IncomeEntity? {
        return incomeDao.getById(id)
    }

    // Logs
    val allLogs: Flow<List<LogEntity>> = logDao.getAllLogs()

    suspend fun insertLog(message: String) {
        logDao.insertLog(LogEntity(timestamp = System.currentTimeMillis(), message = message))
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    suspend fun checkAndProcessIncomes() {
        val incomes = incomeDao.getAll().first()
        val now = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (income in incomes) {
            if (income.nextPay == 0L) continue

            val nextPayCal = GregorianCalendar().apply {
                timeInMillis = income.nextPay
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var updatedNextPay = income.nextPay
            var updatedLastPay = income.lastPay
            var balanceAdded = 0f
            var cyclesProcessed = 0

            while (!nextPayCal.after(now)) {
                balanceAdded += income.amount
                cyclesProcessed++
                updatedLastPay = nextPayCal.timeInMillis

                if (income.weeklyFlag) {
                    nextPayCal.add(Calendar.DAY_OF_MONTH, 7)
                } else {
                    nextPayCal.add(Calendar.MONTH, 1)
                    val maxDay = nextPayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    nextPayCal.set(Calendar.DAY_OF_MONTH, minOf(income.dayOfMonth, maxDay))

                    val dayOfWeek = nextPayCal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek == Calendar.SATURDAY) {
                        nextPayCal.add(Calendar.DAY_OF_MONTH, -1)
                    } else if (dayOfWeek == Calendar.SUNDAY) {
                        nextPayCal.add(Calendar.DAY_OF_MONTH, -2)
                    }
                }
                updatedNextPay = nextPayCal.timeInMillis
            }

            if (cyclesProcessed > 0) {
                val account = accountDao.getById(income.accountId)
                if (account != null) {
                    val newBalance = account.balance + balanceAdded
                    accountDao.update(account.copy(balance = newBalance))

                    val finalCutoff = calculateCutOffDate(updatedNextPay)
                    val finalCycleStart = calculateCycleStart(income.weeklyFlag, income.dayOfMonth, updatedNextPay)

                    val updatedIncome = income.copy(
                        lastPay = updatedLastPay,
                        nextPay = updatedNextPay,
                        cutOffDate = finalCutoff,
                        cycleStartDate = finalCycleStart
                    )
                    incomeDao.update(updatedIncome)

                    insertLog("Received income '${income.name}' of ${income.amount} to account '${account.name}'" + if (cyclesProcessed > 1) " (x$cyclesProcessed)" else "")
                }
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

    private fun calculateCutOffDate(nextPay: Long): Long {
        if (nextPay == 0L) return 0L
        val cal = GregorianCalendar().apply {
            timeInMillis = nextPay
            add(Calendar.DAY_OF_MONTH, -1)
        }
        return cal.timeInMillis
    }
}
