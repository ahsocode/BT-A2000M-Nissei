package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checksheet_item_results")
data class ChecksheetItemResultEntity(
    @PrimaryKey val id: String,
    val checksheetId: String,
    val checklistItemId: String,
    val resultBoolean: Boolean?,
    val resultText: String?,
    val resultNumber: Double?
)
