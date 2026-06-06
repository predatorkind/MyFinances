package net.vertexgraphics.myfinances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BillEntity::class, LogEntity::class], version = 2, exportSchema = false)
abstract class MyFinancesDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: MyFinancesDatabase? = null

        fun getDatabase(context: Context): MyFinancesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MyFinancesDatabase::class.java,
                    "my_finances_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
