package com.example.bt_a2000m_nissei.data.db

data class HistoryDetailRow(
    val sessionId: String,
    val performedAt: Long,
    val machineName: String,
    val machineCode: String,

    val period: InspectionPeriod,
    val displayOrder: Int,
    val itemContent: String,
    val itemType: ChecklistItemType,

    val resultBoolean: Boolean?,
    val resultText: String?,
    val resultNumber: Double?
)
