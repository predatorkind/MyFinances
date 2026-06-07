package net.vertexgraphics.myfinances

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(private val context: Context) {

    private val dataStore = context.dataStore

    object PreferencesKeys {
        val CURRENT_BALANCE = floatPreferencesKey("current_balance")
        val LAST_BALANCE = floatPreferencesKey("last_balance")
        val INCOME_AMOUNT = floatPreferencesKey("income_amount")
        val INCOME_WEEKLY = booleanPreferencesKey("income_weekly")
        val INCOME_DAY_OF_MONTH = intPreferencesKey("income_day_of_month")
        val INCOME_DAY_OF_WEEK = intPreferencesKey("income_day_of_week")
        val LAST_PAY = longPreferencesKey("last_pay")
        val NEXT_PAY = longPreferencesKey("next_pay")
        val CUTOFF_DATE = longPreferencesKey("cutoff_date")
        val CYCLE_START_DATE = longPreferencesKey("cycle_start_date")
        val PAY_BUTTON_DAYS_THRESHOLD = intPreferencesKey("pay_button_days_threshold")
        val DEBUG_RESET_ON_STARTUP = booleanPreferencesKey("debug_reset_on_startup")
        val DEBUG_SAMPLE_DATA_POPULATED = booleanPreferencesKey("debug_sample_data_populated")
    }

    val balanceFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_BALANCE] ?: 0f
    }

    val incomeFlow: Flow<Income?> = dataStore.data.map { preferences ->
        val amount = preferences[PreferencesKeys.INCOME_AMOUNT] ?: 0f
        if (amount == 0f) return@map null
        
        Income(
            amount = amount,
            weeklyFlag = preferences[PreferencesKeys.INCOME_WEEKLY] ?: false,
            dayOfMonth = preferences[PreferencesKeys.INCOME_DAY_OF_MONTH] ?: 1,
            dayOfWeek = preferences[PreferencesKeys.INCOME_DAY_OF_WEEK] ?: 1,
            lastPay = preferences[PreferencesKeys.LAST_PAY] ?: 0L,
            nextPay = preferences[PreferencesKeys.NEXT_PAY] ?: 0L,
            cutOffDate = preferences[PreferencesKeys.CUTOFF_DATE] ?: 0L,
            cycleStartDate = preferences[PreferencesKeys.CYCLE_START_DATE] ?: 0L
        )
    }

    suspend fun updateBalance(amount: Float) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CURRENT_BALANCE] ?: 0f
            preferences[PreferencesKeys.LAST_BALANCE] = current
            preferences[PreferencesKeys.CURRENT_BALANCE] = amount
        }
    }

    suspend fun saveIncome(income: Income) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCOME_AMOUNT] = income.amount
            preferences[PreferencesKeys.INCOME_WEEKLY] = income.weeklyFlag
            preferences[PreferencesKeys.INCOME_DAY_OF_MONTH] = income.dayOfMonth
            preferences[PreferencesKeys.INCOME_DAY_OF_WEEK] = income.dayOfWeek
            preferences[PreferencesKeys.LAST_PAY] = income.lastPay
            preferences[PreferencesKeys.NEXT_PAY] = income.nextPay
            preferences[PreferencesKeys.CUTOFF_DATE] = income.cutOffDate
            preferences[PreferencesKeys.CYCLE_START_DATE] = income.cycleStartDate
        }
    }

    val debugResetOnStartupFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEBUG_RESET_ON_STARTUP] ?: false
    }

    val payButtonDaysThresholdFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PAY_BUTTON_DAYS_THRESHOLD] ?: 3
    }

    val debugSampleDataPopulatedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEBUG_SAMPLE_DATA_POPULATED] ?: false
    }

    suspend fun updateDebugResetOnStartup(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_RESET_ON_STARTUP] = enabled
        }
    }

    suspend fun updatePayButtonDaysThreshold(days: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAY_BUTTON_DAYS_THRESHOLD] = days
        }
    }

    suspend fun updateDebugSampleDataPopulated(populated: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_SAMPLE_DATA_POPULATED] = populated
        }
    }
}
