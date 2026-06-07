package net.vertexgraphics.myfinances

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.migrationDataStore by preferencesDataStore(name = "migration_status")

@Singleton
class DataMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val repository: FinanceRepository
) {
    private val MIGRATION_DONE = booleanPreferencesKey("migration_done")

    suspend fun migrateIfNeeded() {
        val isDone = context.migrationDataStore.data.map { it[MIGRATION_DONE] ?: false }.first()
        if (!isDone) {
            val userPrefs = UserPrefs.getInstance(context)
            
            // Migrate Balance
            preferenceManager.updateBalance(userPrefs.currentBalance)
            
            // Migrate Income
            userPrefs.income?.let {
                preferenceManager.saveIncome(it)
            }
            
            // Migrate Logs
            val legacyLogs = userPrefs.readLog("")
            legacyLogs.forEach { logText ->
                if (logText.isNotBlank()) {
                    // Try to parse date or just insert as is
                    repository.insertLog(logText.trim())
                }
            }

            context.migrationDataStore.edit { it[MIGRATION_DONE] = true }
        }

        // Programmatically migrate from DataStore to Room database tables
        migrateToRoomDatabase()
    }

    private suspend fun migrateToRoomDatabase() {
        val mainAcc = repository.getMainAccount()
        if (mainAcc == null) {
            // Read balance from preferenceManager
            val balance = preferenceManager.balanceFlow.first()
            val accountId = repository.insertAccount(
                AccountEntity(
                    name = "Main Account",
                    balance = balance,
                    isMain = true
                )
            ).toInt()

            // Read legacy income
            val legacyIncome = preferenceManager.incomeFlow.first()
            if (legacyIncome != null) {
                repository.insertIncome(
                    IncomeEntity(
                        name = "Primary Income",
                        amount = legacyIncome.amount,
                        weeklyFlag = legacyIncome.weeklyFlag,
                        dayOfMonth = legacyIncome.dayOfMonth,
                        dayOfWeek = legacyIncome.dayOfWeek,
                        lastPay = legacyIncome.lastPay,
                        nextPay = legacyIncome.nextPay,
                        cutOffDate = legacyIncome.cutOffDate,
                        cycleStartDate = legacyIncome.cycleStartDate,
                        accountId = accountId
                    )
                )
            }

            // Update all existing bills to point to this main account
            val bills = repository.allBills.first()
            bills.forEach { bill ->
                if (bill.accountId == 0) {
                    repository.updateBill(bill.copy(accountId = accountId))
                }
            }
        }
    }
}
