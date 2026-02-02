package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import java.util.UUID

@Dao
interface RunnerDao {

    @Query(
        """SELECT
            ci.id as checklistItemId,
            ci.content as title,
            ci.itemType as type,
            b.period as period
        FROM machine_checklist_bindings b
        JOIN checklist_items ci ON b.templateId = ci.templateId
        WHERE b.machineId = :machineId AND b.period IN (:periods)
        ORDER BY b.period, ci.displayOrder"""
    )
    suspend fun getChecklistItems(machineId: String, periods: List<InspectionPeriod>): List<ChecklistItemInfo>

    @Transaction
    suspend fun saveSession(
        machineId: String,
        periods: List<InspectionPeriod>,
        templateIdByPeriod: Map<InspectionPeriod, String>,
        performedByUserId: String,
        answers: List<AnswerRow>
    ) {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()

        for (p in periods) {
            val templateId = templateIdByPeriod[p] ?: continue

            val checksheetId = UUID.randomUUID().toString()
            insertChecksheet(
                ChecksheetEntity(
                    id = checksheetId,
                    sessionId = sessionId,
                    machineId = machineId,
                    templateId = templateId,
                    period = p,
                    performedByUserId = performedByUserId,
                    performedAt = now,
                    status = "COMPLETED",
                    synced = false,
                    createdAt = now
                )
            )

            answers.filter { it.period == p }.forEach { ans ->
                insertResult(
                    ChecksheetItemResultEntity(
                        id = UUID.randomUUID().toString(),
                        checksheetId = checksheetId,
                        checklistItemId = ans.checklistItemId,
                        resultBoolean = ans.resultBoolean,
                        resultText = ans.resultText,
                        resultNumber = ans.resultNumber
                    )
                )
            }
        }
    }

    @Insert
    suspend fun insertChecksheet(checksheet: ChecksheetEntity)

    @Insert
    suspend fun insertResult(result: ChecksheetItemResultEntity)
}
