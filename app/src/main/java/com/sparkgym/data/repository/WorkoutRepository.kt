package com.sparkgym.data.repository

import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.ExerciseEntity
import com.sparkgym.data.local.MuscleVolumeRow
import com.sparkgym.data.local.PersonalRecordEntity
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.local.WorkoutSessionEntity
import com.sparkgym.data.seed.BodyweightWorkouts
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.ProgressionEngine
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.TrackingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Everything about logged training: starting a session, recording sets,
 * detecting records, and the aggregates the heat map and charts read.
 */
class WorkoutRepository(private val db: SparkGymDatabase) {

    private val workoutDao get() = db.workoutDao()
    private val exerciseDao get() = db.exerciseDao()
    private val routineDao get() = db.routineDao()

    // ------------------------------------------------------------- exercises

    fun observeExercises() = exerciseDao.observeAllWithMuscles()

    fun observeExercise(id: Long) = exerciseDao.observeWithMuscles(id)

    suspend fun exercise(id: Long) = exerciseDao.byId(id)

    suspend fun toggleFavorite(id: Long, favorite: Boolean) = exerciseDao.setFavorite(id, favorite)

    suspend fun createCustomExercise(
        name: String,
        equipment: com.sparkgym.domain.model.Equipment,
        tracking: TrackingType,
        primary: List<Muscle>,
        secondary: List<Muscle>,
        instructions: String
    ): Long {
        val id = exerciseDao.insert(
            ExerciseEntity(
                slug = "custom-${System.currentTimeMillis()}",
                name = name,
                equipment = equipment,
                force = com.sparkgym.domain.model.ExerciseForce.STATIC,
                difficulty = com.sparkgym.domain.model.ExerciseDifficulty.INTERMEDIATE,
                tracking = tracking,
                instructions = instructions,
                isCustom = true
            )
        )
        exerciseDao.insertMuscles(
            primary.map { com.sparkgym.data.local.ExerciseMuscleEntity(id, it.name, 1.0f) } +
                secondary.filterNot { it in primary }
                    .map { com.sparkgym.data.local.ExerciseMuscleEntity(id, it.name, 0.5f) }
        )
        return id
    }

    // -------------------------------------------------------------- routines

    fun observeRoutines() = routineDao.observeRoutines()

    /** Everything except the bodyweight circuits, which get their own screen. */
    fun observeGymRoutines() =
        routineDao.observeRoutinesExcludingGoal(BodyweightWorkouts.ROUTINE_GOAL)
    fun observeRoutine(id: Long) = routineDao.observeRoutine(id)
    fun observeRoutineDays(routineId: Long) = routineDao.observeDays(routineId)
    fun observePrescribed(dayId: Long) = routineDao.observePrescribed(dayId)

    // --------------------------------------------------------------- session

    fun observeActiveSession(): Flow<WorkoutSessionEntity?> = workoutDao.observeActiveSession()

    fun observeSets(sessionId: Long) = workoutDao.observeSets(sessionId)

    fun observeHistory() = workoutDao.observeHistory()

    fun observeSessionBreakdown(sessionId: Long) = workoutDao.observeSessionBreakdown(sessionId)

    suspend fun session(id: Long) = workoutDao.session(id)

    /**
     * Starts a session, optionally pre-loading every prescribed set from a
     * routine day as empty rows the user just fills in.
     */
    suspend fun startSession(
        name: String,
        routineDayId: Long?,
        bodyweightKg: Double
    ): Long {
        workoutDao.activeSession()?.let { return it.id }

        val now = System.currentTimeMillis()
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(
                routineDayId = routineDayId,
                name = name,
                startedAt = now,
                dateEpochDay = Dates.epochDay(now),
                bodyweightKg = bodyweightKg
            )
        )

        if (routineDayId != null) {
            val prescriptions = routineDao.prescribed(routineDayId)
            val rows = prescriptions.mapIndexed { order, item ->
                (1..item.prescription.targetSets).map { setNumber ->
                    SetLogEntity(
                        sessionId = sessionId,
                        exerciseId = item.exercise.id,
                        orderIndex = order,
                        setNumber = setNumber
                    )
                }
            }.flatten()
            workoutDao.insertSets(rows)
        }

        return sessionId
    }

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, sets: Int = 3) {
        val existing = workoutDao.sets(sessionId)
        val nextOrder = (existing.maxOfOrNull { it.orderIndex } ?: -1) + 1
        workoutDao.insertSets(
            (1..sets).map {
                SetLogEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    orderIndex = nextOrder,
                    setNumber = it
                )
            }
        )
    }

    suspend fun addSetRow(sessionId: Long, exerciseId: Long) {
        val existing = workoutDao.sets(sessionId).filter { it.exerciseId == exerciseId }
        val order = existing.firstOrNull()?.orderIndex ?: 0
        workoutDao.insertSet(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                orderIndex = order,
                setNumber = (existing.maxOfOrNull { it.setNumber } ?: 0) + 1,
                weightKg = existing.lastOrNull()?.weightKg ?: 0.0,
                reps = existing.lastOrNull()?.reps ?: 0
            )
        )
    }

    suspend fun deleteSet(id: Long) = workoutDao.deleteSet(id)

    /** Saves a set and returns true when it set a new estimated-1RM record. */
    suspend fun completeSet(set: SetLogEntity): Boolean {
        val exercise = exerciseDao.byId(set.exerciseId)
        val isPr = if (exercise != null && !set.isWarmup) checkRecord(exercise, set) else false
        workoutDao.updateSet(
            set.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                isPersonalRecord = isPr
            )
        )
        return isPr
    }

    suspend fun updateSet(set: SetLogEntity) = workoutDao.updateSet(set)

    suspend fun lastPerformance(exerciseId: Long): List<SetLogEntity> =
        workoutDao.recentSetsFor(exerciseId)

    private suspend fun checkRecord(exercise: ExerciseEntity, set: SetLogEntity): Boolean {
        if (exercise.tracking != TrackingType.WEIGHT_REPS && exercise.tracking != TrackingType.BODYWEIGHT_PLUS) {
            return false
        }
        if (set.weightKg <= 0 || set.reps <= 0) return false

        val estimated = StrengthMath.estimatedOneRepMax(set.weightKg, set.reps)
        val existing = workoutDao.pr(exercise.id)
        val isPr = existing == null || estimated > existing.bestEstimated1RmKg + 0.01

        if (isPr) {
            workoutDao.upsertPr(
                PersonalRecordEntity(
                    id = existing?.id ?: 0,
                    exerciseId = exercise.id,
                    bestEstimated1RmKg = estimated,
                    bestWeightKg = maxOf(set.weightKg, existing?.bestWeightKg ?: 0.0),
                    bestReps = maxOf(set.reps, existing?.bestReps ?: 0),
                    bestVolumeKg = maxOf(set.weightKg * set.reps, existing?.bestVolumeKg ?: 0.0),
                    achievedAt = System.currentTimeMillis()
                )
            )
        }
        return isPr
    }

    data class SessionSummary(
        val sessionId: Long,
        val completedSets: Int,
        val volumeKg: Double,
        val durationMinutes: Int,
        val personalRecords: Int,
        val estimatedCalories: Int
    )

    /** Closes the session and returns what it was worth, for the XP award. */
    suspend fun finishSession(sessionId: Long): SessionSummary? {
        val session = workoutDao.session(sessionId) ?: return null
        val sets = workoutDao.sets(sessionId).filter { it.isCompleted }
        val exercises = sets.map { it.exerciseId }.distinct()
            .mapNotNull { exerciseDao.byId(it) }
            .associateBy { it.id }

        var volume = 0.0
        var durationSeconds = 0
        for (set in sets) {
            val tracking = exercises[set.exerciseId]?.tracking ?: TrackingType.WEIGHT_REPS
            volume += StrengthMath.setVolumeKg(
                tracking = tracking,
                weightKg = set.weightKg,
                reps = set.reps,
                durationSeconds = set.durationSeconds,
                distanceMeters = set.distanceMeters,
                bodyweightKg = session.bodyweightKg
            )
            durationSeconds += set.durationSeconds
        }

        val now = System.currentTimeMillis()
        val elapsedMinutes = ((now - session.startedAt) / 60_000).toInt().coerceIn(0, 480)
        val prCount = sets.count { it.isPersonalRecord }

        workoutDao.updateSession(
            session.copy(
                finishedAt = now,
                totalVolumeKg = volume,
                totalSets = sets.size
            )
        )

        // Delete the untouched placeholder rows so history is not full of blanks.
        workoutDao.sets(sessionId).filterNot { it.isCompleted }.forEach { workoutDao.deleteSet(it.id) }

        return SessionSummary(
            sessionId = sessionId,
            completedSets = sets.size,
            volumeKg = volume,
            durationMinutes = elapsedMinutes,
            personalRecords = prCount,
            estimatedCalories = StrengthMath.estimatedCalories(volume, durationSeconds)
        )
    }

    suspend fun recordSessionXp(sessionId: Long, xp: Int) {
        workoutDao.session(sessionId)?.let { workoutDao.updateSession(it.copy(xpAwarded = xp)) }
    }

    suspend fun discardSession(sessionId: Long) = workoutDao.deleteSession(sessionId)

    // --------------------------------------------------------------- heatmap

    /** Effective sets per muscle over a rolling window, ready for the heat map. */
    fun observeHeatmap(windowDays: Int): Flow<Map<Muscle, HeatmapEngine.MuscleHeat>> {
        val today = Dates.today()
        val from = today - (windowDays - 1)
        return workoutDao.observeMuscleVolume(from, today).map { rows ->
            HeatmapEngine.build(
                setsByMuscle = rows.toMuscleMap { it.effectiveSets },
                volumeByMuscle = rows.toMuscleMap { it.volumeKg },
                days = windowDays
            )
        }
    }

    private inline fun List<MuscleVolumeRow>.toMuscleMap(select: (MuscleVolumeRow) -> Double): Map<Muscle, Double> =
        mapNotNull { row -> Muscle.fromKey(row.muscle)?.let { it to select(row) } }.toMap()

    fun observeVolumeTrend(days: Int) = workoutDao.observeVolumeTrend(Dates.today() - days + 1, Dates.today())

    fun observeCompletedCount() = workoutDao.observeCompletedCount()

    fun observePersonalRecords() = workoutDao.observePrs()

    suspend fun trainingDays(): List<Long> = workoutDao.recentTrainingDays()

    /** Sessions logged in the current rolling week. */
    suspend fun sessionsThisWeek(): Int =
        workoutDao.recentTrainingDays().count { it >= Dates.today() - 6 }

    suspend fun daysSinceLastSession(): Int {
        val last = workoutDao.recentTrainingDays().maxOrNull() ?: return Int.MAX_VALUE
        return (Dates.today() - last).toInt()
    }

    /**
     * How many consecutive calendar weeks contain at least one session, counting
     * back from this one. Feeds the deload check.
     */
    suspend fun consecutiveTrainingWeeks(): Int {
        val days = workoutDao.recentTrainingDays().toSet()
        if (days.isEmpty()) return 0
        var weeks = 0
        var weekStart = Dates.today() - 6
        while (weeks < 52) {
            val hasSession = (weekStart..(weekStart + 6)).any { it in days }
            if (!hasSession) break
            weeks++
            weekStart -= 7
        }
        return weeks
    }

    /**
     * The main lifts whose estimated 1RM has flatlined. Only exercises with a
     * real history are considered — three sessions is not enough to call a stall.
     */
    suspend fun stalledLifts(): List<String> {
        val ids = workoutDao.mostTrainedExerciseIds()
        return ids.mapNotNull { id ->
            val history = workoutDao.estimatedMaxHistory(id).map { it.volumeKg }
            if (ProgressionEngine.hasStalled(history)) exerciseDao.byId(id)?.name else null
        }
    }

    /** Personal records set inside the given window. */
    suspend fun recentPrCount(withinDays: Int = 21): Int {
        val cutoff = System.currentTimeMillis() - withinDays * 86_400_000L
        return workoutDao.observePrs().first().count { it.achievedAt >= cutoff }
    }

    /** Today's suggested load for an exercise, from double progression. */
    suspend fun progressionFor(
        exerciseId: Long,
        repsMin: Int,
        repsMax: Int
    ): ProgressionEngine.Suggestion? {
        val exercise = exerciseDao.byId(exerciseId) ?: return null
        val recent = workoutDao.recentSetsFor(exerciseId, limit = 6)
            .filter { it.reps > 0 }
            .map { it.weightKg to it.reps }
        val muscles = exerciseDao.musclesFor(exerciseId)
        val isLowerBody = muscles.any {
            it in setOf("QUADS", "HAMSTRINGS", "GLUTES", "CALVES", "ADDUCTORS", "ABDUCTORS")
        }
        return ProgressionEngine.suggest(recent, repsMin, repsMax, exercise.tracking, isLowerBody)
    }

    /** Consecutive days ending today (or yesterday) with a finished session. */
    suspend fun trainingStreak(): Int {
        val days = workoutDao.recentTrainingDays().toSortedSet().reversed()
        if (days.isEmpty()) return 0
        val today = Dates.today()
        var expected = when (days.first()) {
            today, today - 1 -> days.first()
            else -> return 0
        }
        var streak = 0
        for (day in days) {
            if (day == expected) {
                streak++
                expected -= 1
            } else if (day < expected) break
        }
        return streak
    }
}
