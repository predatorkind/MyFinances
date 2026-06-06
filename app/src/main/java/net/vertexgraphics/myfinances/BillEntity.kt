package net.vertexgraphics.myfinances

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_bill")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Float,

    @ColumnInfo(name = "weekly")
    val weekly: Boolean,

    @ColumnInfo(name = "dayOfMonth")
    val dayOfMonth: Int,

    @ColumnInfo(name = "dayOfWeek")
    val dayOfWeek: String,

    @ColumnInfo(name = "lastPaid")
    val lastPaid: Long,

    @ColumnInfo(name = "dueDate")
    val dueDate: Long,

    @ColumnInfo(name = "account_id")
    val accountId: Int = 0
)
