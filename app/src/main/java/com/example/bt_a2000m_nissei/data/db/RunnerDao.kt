package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface RunnerDao {
    @Query("""
        SELECT
            cs.id as checksheetId,
            ci.id as checklistItemId,
            ci.content as title,
            ci.itemType as type
        FROM checksheets cs
        JOIN checklist_items ci ON cs.templateId = ci.templateId
        WHERE cs.sessionId = :sessionId
        ORDER BY cs.period, ci.displayOrder
    """)
    suspend fun getChecklistItemsForSession(sessionId: String): List<ChecklistItemInfo>
}
