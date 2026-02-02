package com.example.bt_a2000m_nissei.data.db

data class ChecklistItemInfo(
    val checklistItemId: String,
    val title: String,
    val type: ChecklistItemType,
    val period: InspectionPeriod
)
