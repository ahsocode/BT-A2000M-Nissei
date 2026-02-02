package com.example.bt_a2000m_nissei.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val content: String,
    val itemType: ChecklistItemType,
    val displayOrder: Int,
    val isRequired: Boolean
)
