package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checksheets")
data class ChecksheetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val machineId: String,
    val templateId: String,
    val period: InspectionPeriod,
    val performedByUserId: String,
    val performedAt: Long,
    val status: String,
    val synced: Boolean,
    val createdAt: Long
)
