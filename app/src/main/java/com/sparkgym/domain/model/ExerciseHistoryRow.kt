package com.sparkgym.domain.model

data class ExerciseHistoryRow(
    val sessionId: Long,
    val dateEpochDay: Long,
    val sessionName: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val setType: SetType,
    val isPersonalRecord: Boolean
)
