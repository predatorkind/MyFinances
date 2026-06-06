package net.vertexgraphics.myfinances

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Update
    suspend fun update(bill: BillEntity)

    @Query("SELECT * FROM t_bill ORDER BY dueDate ASC")
    fun getAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM t_bill WHERE id = :id")
    suspend fun getById(id: Int): BillEntity?

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("DELETE FROM t_bill WHERE id = :id")
    suspend fun deleteById(id: Int)
}
