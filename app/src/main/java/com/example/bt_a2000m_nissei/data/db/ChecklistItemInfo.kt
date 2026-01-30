package com.example.bt_a2000m_nissei.data.db

data class ChecklistItemInfo(
    val checksheetId: String,
    val checklistItemId: String,
    val title: String,
    val type: ChecklistItemType
)
