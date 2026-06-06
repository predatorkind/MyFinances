package net.vertexgraphics.myfinances

import android.content.Context
import android.content.SharedPreferences
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UserPrefs private constructor(private val context: Context) {
    private val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = settings.edit()

    var currentBalance: Float = 0f
    var lastBalance: Float = 0f
    var income: Income? = null

    init {
        loadBalance()
        loadIncome()
    }

    fun adjustBalance(amount: Float) {
        lastBalance = currentBalance
        currentBalance += amount
    }

    fun saveBalance() {
        editor.putFloat(KEY_LASTBALANCE, lastBalance)
        editor.putFloat(KEY_CURRENTBALANCE, currentBalance)
        editor.apply()
    }

    private fun loadBalance() {
        lastBalance = settings.getFloat(KEY_LASTBALANCE, 0f)
        currentBalance = settings.getFloat(KEY_CURRENTBALANCE, 0f)
    }

    fun loadIncome() {
        val amount = settings.getFloat(KEY_INCOMEAMOUNT, 0f)
        val freq = settings.getBoolean(KEY_INCOMEFREQUENCY, false)
        val dom = settings.getInt(KEY_INCOMEDAYOFMONTH, 1)
        val dow = settings.getInt(KEY_INCOMEDAYOFWEEK, 1)
        val lpay = settings.getLong(KEY_LASTPAY, 0)
        val npay = settings.getLong(KEY_NEXTPAY, 0)
        val cDate = settings.getLong(KEY_CUTOFF, 0)

        income = Income(amount, freq, dom, dow, lpay, npay, cDate)
    }

    fun saveIncome() {
        val inc = income ?: return
        editor.putFloat(KEY_INCOMEAMOUNT, inc.amount)
        editor.putBoolean(KEY_INCOMEFREQUENCY, inc.weeklyFlag)
        editor.putInt(KEY_INCOMEDAYOFMONTH, inc.dayOfMonth)
        editor.putInt(KEY_INCOMEDAYOFWEEK, inc.dayOfWeek)
        editor.putLong(KEY_LASTPAY, inc.lastPay)
        editor.putLong(KEY_NEXTPAY, inc.nextPay)
        editor.putLong(KEY_CUTOFF, inc.cutOffDate)
        editor.apply()
    }

    fun getTimeStamp(timeAndDate: Boolean): String {
        val msTime = System.currentTimeMillis()
        val formatter = if (timeAndDate) {
            SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        }
        return formatter.format(Date(msTime))
    }

    fun getDateString(time: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date(time))
    }

    fun writeLog(entry: String) {
        try {
            val filePath = "log.txt"
            val fos = context.openFileOutput(filePath, Context.MODE_APPEND)
            fos.write((entry + "###").toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readLog(filter: String): List<String> {
        return try {
            val fis = context.openFileInput("log.txt")
            val content = fis.bufferedReader().use { it.readText() }
            content.split("###").filter { it.contains(filter, ignoreCase = true) }
        } catch (e: IOException) {
            emptyList()
        }
    }

    fun getDueDate(weekly: Boolean, dayNumber: Int, day: String): Long {
        val dueDateCalendar = GregorianCalendar()
        val currentCalendar = GregorianCalendar()

        if (weekly) {
            val dayOfWeek = when (day) {
                context.getString(R.string.sunday_string) -> Calendar.SUNDAY
                context.getString(R.string.monday_string) -> Calendar.MONDAY
                context.getString(R.string.tuesday_string) -> Calendar.TUESDAY
                context.getString(R.string.wednesday_string) -> Calendar.WEDNESDAY
                context.getString(R.string.thursday_string) -> Calendar.THURSDAY
                context.getString(R.string.friday_string) -> Calendar.FRIDAY
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

    companion object {
        @Volatile
        private var instance: UserPrefs? = null

        private const val PREFS_NAME = "net.vertexgraphics.bills"
        private const val KEY_LASTBALANCE = "key_LASTBALANCE"
        private const val KEY_CURRENTBALANCE = "key_CURRENTBALANCE"
        private const val KEY_INCOMEAMOUNT = "key_INCOMEAMOUNT"
        private const val KEY_INCOMEFREQUENCY = "key_INCOMEFREQUENCY"
        private const val KEY_INCOMEDAYOFWEEK = "key_INCOMEDAYOFWEEK"
        private const val KEY_INCOMEDAYOFMONTH = "key_INCOMEDAYOFMONTH"
        private const val KEY_LASTPAY = "key_LASTPAY"
        private const val KEY_NEXTPAY = "key_NEXTPAY"
        private const val KEY_CUTOFF = "key_CUTOFF"

        fun getInstance(context: Context): UserPrefs {
            return instance ?: synchronized(this) {
                instance ?: UserPrefs(context.applicationContext).also { instance = it }
            }
        }
    }
}
