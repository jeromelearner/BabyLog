package com.wongchoi500.babylog.data

import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    val allLogs: Flow<List<BabyLog>> = logDao.getAllLogs()

    fun getLogsByDateRange(start: Long, end: Long): Flow<List<BabyLog>> {
        return logDao.getLogsByDateRange(start, end)
    }

    suspend fun getAllLogsList(): List<BabyLog> {
        return logDao.getAllLogsList()
    }

    suspend fun insert(log: BabyLog) {
        logDao.insertLog(log)
    }

    suspend fun delete(log: BabyLog) {
        logDao.deleteLog(log)
    }
}
