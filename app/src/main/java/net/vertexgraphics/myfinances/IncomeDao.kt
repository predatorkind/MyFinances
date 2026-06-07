package net.vertexgraphics.myfinances

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)

    @Query("SELECT * FROM t_income ORDER BY name ASC")
    fun getAll(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM t_income WHERE id = :id")
    suspend fun getById(id: Int): IncomeEntity?
}
