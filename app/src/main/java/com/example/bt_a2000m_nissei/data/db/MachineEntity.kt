package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val id: String,
    val machineCode: String,
    val machineName: String,
    val description: String?,
    val location: String?,
    val note: String?,
    val createdAt: Long
)
