package com.sparkgym.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** Effective-sets-per-muscle row produced by the heat map query. */
data class MuscleVolumeRow(
    val muscle: String,
    val effectiveSets: Double,
    val volumeKg: Double
)

/** One row per exercise in a session, for history summaries. */
data class SessionExerciseRow(
    val exerciseId: Long,
    val exerciseName: String,
    val setCount: Int,
    val topWeightKg: Double,
    val topReps: Int,
    val volumeKg: Double
)

data class DailyTotalsRow(
    val dateEpochDay: Long,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

data class VolumePointRow(
    val dateEpochDay: Long,
    val volumeKg: Double,
    val sets: Int
)

data class ExerciseWithMuscles(
    @Embedded val exercise: ExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "exerciseId")
    val muscles: List<ExerciseMuscleEntity>
)

data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(parentColumn = "id", entityColumn = "dayId")
    val exercises: List<RoutineExerciseEntity>
)

data class RoutineWithDays(
    @Embedded val routine: RoutineEntity,
    @Relation(
        entity = RoutineDayEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val days: List<RoutineDayWithExercises>
)

/** Prescribed exercise joined to its library row, for the day detail screen. */
data class PrescribedExercise(
    @Embedded val prescription: RoutineExerciseEntity,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: ExerciseEntity
)
