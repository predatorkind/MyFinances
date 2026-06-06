package net.vertexgraphics.myfinances

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val billDao: BillDao,
    private val logDao: LogDao
) {
    val allBills: Flow<List<BillEntity>> = billDao.getAll()
    val allLogs: Flow<List<LogEntity>> = logDao.getAllLogs()

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

    suspend fun insertLog(message: String) {
        logDao.insertLog(LogEntity(timestamp = System.currentTimeMillis(), message = message))
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }
}
