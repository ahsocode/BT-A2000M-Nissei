package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("""
        SELECT
            cs.sessionId,
            cs.performedAt,
            m.machineName,
            m.machineCode,
            GROUP_CONCAT(cs.period) as periods,
            COUNT(cs.id) as itemCount
        FROM checksheets cs
        JOIN machines m ON cs.machineId = m.id
        GROUP BY cs.sessionId
        ORDER BY cs.performedAt DESC
    """)
    suspend fun getHistory(): List<HistoryItem>
}
