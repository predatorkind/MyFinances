package net.vertexgraphics.myfinances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BillEntity::class, LogEntity::class, AccountEntity::class, IncomeEntity::class], version = 3, exportSchema = false)
abstract class MyFinancesDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun logDao(): LogDao
    abstract fun accountDao(): AccountDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: MyFinancesDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `t_account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `is_main` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `t_income` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `weekly_flag` INTEGER NOT NULL, `day_of_month` INTEGER NOT NULL, `day_of_week` INTEGER NOT NULL, `last_pay` INTEGER NOT NULL, `next_pay` INTEGER NOT NULL, `cut_off_date` INTEGER NOT NULL, `cycle_start_date` INTEGER NOT NULL, `account_id` INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): MyFinancesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MyFinancesDatabase::class.java,
                    "my_finances_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
