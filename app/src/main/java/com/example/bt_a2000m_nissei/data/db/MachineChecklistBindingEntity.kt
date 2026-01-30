package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machine_checklist_bindings")
data class MachineChecklistBindingEntity(
    @PrimaryKey val id: String,
    val machineId: String,
    val period: InspectionPeriod,
    val templateId: String
)
