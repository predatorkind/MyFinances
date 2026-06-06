package net.vertexgraphics.myfinances

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_log")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val message: String
)
