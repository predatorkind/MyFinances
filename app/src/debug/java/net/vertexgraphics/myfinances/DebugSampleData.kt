package net.vertexgraphics.myfinances

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object DebugSampleData {
    suspend fun populateIfNeeded(
        database: MyFinancesDatabase,
        preferenceManager: PreferenceManager
    ) {
        val resetEnabled = preferenceManager.debugResetOnStartupFlow.first()
        val alreadyPopulated = preferenceManager.debugSampleDataPopulatedFlow.first()

        if (resetEnabled || !alreadyPopulated) {
            withContext(Dispatchers.IO) {
                // Clear current data using Room database's clearAllTables
                database.clearAllTables()

                // Populate legacy default balance & income preferences
                preferenceManager.updateBalance(1310.78f)
                preferenceManager.saveIncome(
                    Income(
                        amount = 2754.68f,
                        weeklyFlag = false,
                        dayOfMonth = 26,
                        dayOfWeek = 1,
                        lastPay = 1779992447248L,
                        nextPay = 1782447796681L,
                        cutOffDate = 1782361406690L
                    )
                )

                // Populate sample bills
                val billDao = database.billDao()
                val sampleBills = listOf(
                    BillEntity(name = "Mortgage", amount = 582.03f, weekly = false, dayOfMonth = 1, dayOfWeek = "N/A", lastPaid = 1780287481996L, dueDate = 1782860400000L),
                    BillEntity(name = "Virgin Media", amount = 33.55f, weekly = false, dayOfMonth = 28, dayOfWeek = "N/A", lastPaid = 1779943885199L, dueDate = 1782601200000L),
                    BillEntity(name = "TV License", amount = 15.45f, weekly = false, dayOfMonth = 8, dayOfWeek = "N/A", lastPaid = 1778304573827L, dueDate = 1780873200000L),
                    BillEntity(name = "United Utilities", amount = 67.93f, weekly = false, dayOfMonth = 7, dayOfWeek = "N/A", lastPaid = 1778164138439L, dueDate = 1780786800000L),
                    BillEntity(name = "O2", amount = 24.29f, weekly = false, dayOfMonth = 1, dayOfWeek = "N/A", lastPaid = 1780462008876L, dueDate = 1782860400000L),
                    BillEntity(name = "PythonAnywhere", amount = 5.33f, weekly = false, dayOfMonth = 7, dayOfWeek = "N/A", lastPaid = 1778164150005L, dueDate = 1780700400000L),
                    BillEntity(name = "Council Tax", amount = 162.00f, weekly = false, dayOfMonth = 28, dayOfWeek = "N/A", lastPaid = 1779942499711L, dueDate = 1782601200000L),
                    BillEntity(name = "Octopus Energy", amount = 123.98f, weekly = false, dayOfMonth = 1, dayOfWeek = "N/A", lastPaid = 1780287489696L, dueDate = 1782860400000L),
                    BillEntity(name = "Metlife", amount = 10.00f, weekly = false, dayOfMonth = 23, dayOfWeek = "N/A", lastPaid = 1779857680386L, dueDate = 1782169200000L),
                    BillEntity(name = "Netflix", amount = 12.99f, weekly = false, dayOfMonth = 23, dayOfWeek = "N/A", lastPaid = 1779468026454L, dueDate = 1782169200000L),
                    BillEntity(name = "Beagle Street", amount = 12.41f, weekly = false, dayOfMonth = 22, dayOfWeek = "N/A", lastPaid = 1779468016927L, dueDate = 1782082800000L),
                    BillEntity(name = "Admiral", amount = 17.03f, weekly = false, dayOfMonth = 22, dayOfWeek = "N/A", lastPaid = 1779468017537L, dueDate = 1781996400000L),
                    BillEntity(name = "Polish transfer", amount = 0.00f, weekly = false, dayOfMonth = 28, dayOfWeek = "N/A", lastPaid = 1779942344045L, dueDate = 1782601200000L)
                )
                for (bill in sampleBills) {
                    billDao.insert(bill)
                }

                // Populate sample logs
                val logDao = database.logDao()
                val sampleLogs = listOf(
                    LogEntity(timestamp = 1780650360000L, message = "Deducted: 91.85 - Food"),
                    LogEntity(timestamp = 1780637700000L, message = "Added: 395.95 - Other"),
                    LogEntity(timestamp = 1780455960000L, message = "Paid: 24.29 - O2"),
                    LogEntity(timestamp = 1780469220000L, message = "Paid: 123.98 - Octopus Energy"),
                    LogEntity(timestamp = 1780469160000L, message = "Paid: 624.19 - Mortgage"),
                    LogEntity(timestamp = 1780291020000L, message = "Added: 108.20 - Other")
                )
                for (log in sampleLogs) {
                    logDao.insertLog(log)
                }

                // Mark as populated
                preferenceManager.updateDebugSampleDataPopulated(true)
            }
        }
    }
}
