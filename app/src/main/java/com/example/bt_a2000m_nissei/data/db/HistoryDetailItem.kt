package com.example.bt_a2000m_nissei.data.db

data class HistoryDetailItem(
    val machineName: String,
    val machineCode: String,
    val performedAt: Long,
    val period: InspectionPeriod,
    val displayOrder: Int,
    val content: String,
    val itemType: ChecklistItemType,
    val resultBoolean: Boolean?,
    val resultText: String?,
    val resultNumber: Double?
)
