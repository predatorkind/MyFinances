package net.vertexgraphics.myfinances

data class Income(
    var amount: Float,
    var weeklyFlag: Boolean,
    var dayOfMonth: Int,
    var dayOfWeek: Int,
    var lastPay: Long,
    var nextPay: Long,
    var cutOffDate: Long,
    var cycleStartDate: Long = 0L
)
