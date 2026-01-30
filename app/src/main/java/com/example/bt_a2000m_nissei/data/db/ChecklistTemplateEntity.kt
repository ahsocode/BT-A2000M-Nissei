package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_templates")
data class ChecklistTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val period: InspectionPeriod,
    val isActive: Boolean,
    val createdAt: Long
)
