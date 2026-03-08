package com.wongchoi500.babylog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM baby_logs ORDER BY (CASE WHEN type = 'SLEEP' AND endTime IS NOT NULL THEN endTime ELSE startTime END) DESC")
    fun getAllLogs(): Flow<List<BabyLog>>

    @Query("SELECT * FROM baby_logs")
    suspend fun getAllLogsList(): List<BabyLog>

    @Query("SELECT * FROM baby_logs WHERE startTime >= :start AND startTime < :end ORDER BY (CASE WHEN type = 'SLEEP' AND endTime IS NOT NULL THEN endTime ELSE startTime END) DESC")
    fun getLogsByDateRange(start: Long, end: Long): Flow<List<BabyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BabyLog)

    @Delete
    suspend fun deleteLog(log: BabyLog)
}
