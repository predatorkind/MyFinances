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
        if (isDone) return

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
}
