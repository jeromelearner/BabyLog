package com.wongchoi500.babylog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM baby_logs ORDER BY startTime DESC")
    fun getAllLogs(): Flow<List<BabyLog>>

    @Query("SELECT * FROM baby_logs WHERE startTime >= :start AND startTime < :end ORDER BY startTime DESC")
    fun getLogsByDateRange(start: Long, end: Long): Flow<List<BabyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BabyLog)

    @Delete
    suspend fun deleteLog(log: BabyLog)
}
