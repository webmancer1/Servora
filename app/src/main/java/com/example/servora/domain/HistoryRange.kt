package com.example.servora.domain

enum class HistoryRange(val label: String, val durationMs: Long) {
    HOUR("1H", 3_600_000L),
    DAY("24H", 24 * 3_600_000L),
    WEEK("7D", 7 * 24 * 3_600_000L)
}
