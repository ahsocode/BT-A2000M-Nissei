package com.example.bt_a2000m_nissei.data.db

data class HistoryItem(
    val sessionId: String,
    val performedAt: Long,
    val machineName: String,
    val machineCode: String,
    val periods: String,
    val itemCount: Int
)
