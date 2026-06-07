package net.vertexgraphics.myfinances

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Float,

    @ColumnInfo(name = "weekly_flag")
    val weeklyFlag: Boolean,

    @ColumnInfo(name = "day_of_month")
    val dayOfMonth: Int,

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,

    @ColumnInfo(name = "last_pay")
    val lastPay: Long,

    @ColumnInfo(name = "next_pay")
    val nextPay: Long,

    @ColumnInfo(name = "cut_off_date")
    val cutOffDate: Long,

    @ColumnInfo(name = "cycle_start_date")
    val cycleStartDate: Long = 0L,

    @ColumnInfo(name = "account_id")
    val accountId: Int
)
