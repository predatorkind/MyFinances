package net.vertexgraphics.myfinances.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.vertexgraphics.myfinances.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyFinancesDatabase {
        return MyFinancesDatabase.getDatabase(context)
    }

    @Provides
    fun provideBillDao(database: MyFinancesDatabase): BillDao {
        return database.billDao()
    }

    @Provides
    fun provideLogDao(database: MyFinancesDatabase): LogDao {
        return database.logDao()
    }

    @Provides
    fun provideAccountDao(database: MyFinancesDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideIncomeDao(database: MyFinancesDatabase): IncomeDao {
        return database.incomeDao()
    }

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
}
