package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChecklistDao {

    @Query("SELECT id FROM users WHERE username = :username LIMIT 1")
    suspend fun findUserIdByUsername(username: String): String?

    @Query("SELECT templateId FROM machine_checklist_bindings WHERE machineId = :machineId AND period = :period LIMIT 1")
    suspend fun getTemplateId(machineId: String, period: InspectionPeriod): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecksheet(checksheet: ChecksheetEntity)

}
