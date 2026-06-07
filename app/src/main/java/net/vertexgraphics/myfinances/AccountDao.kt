package net.vertexgraphics.myfinances

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM t_account ORDER BY name ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM t_account WHERE id = :id")
    suspend fun getById(id: Int): AccountEntity?

    @Query("SELECT * FROM t_account WHERE is_main = 1 LIMIT 1")
    fun getMainAccountFlow(): Flow<AccountEntity?>

    @Query("SELECT * FROM t_account WHERE is_main = 1 LIMIT 1")
    suspend fun getMainAccount(): AccountEntity?

    @Query("UPDATE t_account SET is_main = 0")
    suspend fun unsetMainAccounts()
}
