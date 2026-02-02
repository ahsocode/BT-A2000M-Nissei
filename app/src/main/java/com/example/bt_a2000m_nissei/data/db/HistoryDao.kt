package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("""
    SELECT
        cs.sessionId AS sessionId,
        MAX(cs.performedAt) AS performedAt,
        m.machineName AS machineName,
        m.machineCode AS machineCode,

        COALESCE(u.fullName, '(unknown)') AS performedByName,

        GROUP_CONCAT(cs.period) AS periods,
        COUNT(cs.id) AS itemCount
    FROM checksheets cs
    JOIN machines m ON m.id = cs.machineId
    LEFT JOIN users u ON u.id = cs.performedByUserId
    GROUP BY cs.sessionId
    ORDER BY MAX(cs.performedAt) DESC
""")
    suspend fun getHistory(): List<HistoryItem>


    @Query("""
    SELECT
        m.machineName AS machineName,
        m.machineCode AS machineCode,
        cs.performedAt AS performedAt,
        cs.period AS period,
        ci.content AS content,
        ci.itemType AS itemType,
        ci.displayOrder AS displayOrder,
        r.resultBoolean AS resultBoolean,
        r.resultText AS resultText,
        r.resultNumber AS resultNumber
    FROM checksheets cs
    JOIN machines m ON m.id = cs.machineId
    JOIN checklist_items ci ON ci.templateId = cs.templateId
    LEFT JOIN checksheet_item_results r
        ON r.checksheetId = cs.id AND r.checklistItemId = ci.id
    WHERE cs.sessionId = :sessionId
    ORDER BY
        CASE cs.period
            WHEN 'DAILY' THEN 1
            WHEN 'WEEKLY' THEN 2
            WHEN 'MONTHLY' THEN 3
            ELSE 99
        END,
        ci.displayOrder ASC
""")
    suspend fun getHistorySessionDetails(sessionId: String): List<HistoryDetailItem>

}
