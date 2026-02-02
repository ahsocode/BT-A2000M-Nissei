package com.example.bt_a2000m_nissei.data.db

data class AnswerRow(
    val period: InspectionPeriod,
    val checklistItemId: String,
    val resultBoolean: Boolean?,
    val resultText: String?,
    val resultNumber: Double?
)
