package com.sparkgym.data.repository

import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.RoutineEntity
import com.sparkgym.data.local.RoutineExerciseEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.local.WorkoutSessionEntity
import com.sparkgym.data.seed.BodyweightWorkouts
import com.sparkgym.domain.engine.BodyweightEngine
import com.sparkgym.domain.model.BodyweightCircuit
import com.sparkgym.domain.model.BodyweightFocus
import com.sparkgym.domain.model.BodyweightLevel
import com.sparkgym.domain.model.ResolvedCircuit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The bodyweight side of training.
 *
 * Circuits live in the routine tables (see [SeedRepository]), so this class is
 * mostly a translator: it pairs the stored routine rows back up with their
 * circuit definitions, re-prescribes a day when the level changes, and then
 * hands off to [WorkoutRepository] so a circuit is logged, scored and heat-mapped
 * exactly like any other session.
 */
class BodyweightRepository(
    private val db: SparkGymDatabase,
    private val workouts: WorkoutRepository
) {

    private val routineDao get() = db.routineDao()
    private val exerciseDao get() = db.exerciseDao()
    private val workoutDao get() = db.workoutDao()

    /** A circuit definition paired with the routine row that backs it. */
    data class CircuitEntry(
        val circuit: BodyweightCircuit,
        val routine: RoutineEntity
    )

    /**
     * Circuits that actually made it into the database. A definition with no
     * routine row is dropped rather than shown as a card that cannot start.
     */
    fun observeCircuits(): Flow<List<CircuitEntry>> =
        routineDao.observeRoutinesByGoal(BodyweightWorkouts.ROUTINE_GOAL).map { rows ->
            rows.mapNotNull { routine ->
                val circuit = routine.slug?.let(BodyweightWorkouts::bySlug)
                    ?: return@mapNotNull null
                CircuitEntry(circuit, routine)
            }
        }

    /** Sessions started from a circuit, newest first — the streak and history strip. */
    fun observeHistory(limit: Int = 30): Flow<List<WorkoutSessionEntity>> =
        workoutDao.observeHistory(200).map { sessions ->
            val names = BodyweightWorkouts.circuits.map { it.name }.toSet()
            sessions.filter { it.name in names }.take(limit)
        }

    fun resolve(circuit: BodyweightCircuit, level: BodyweightLevel): ResolvedCircuit =
        BodyweightEngine.resolve(circuit, level)

    fun workoutOfTheDay(): BodyweightCircuit? =
        BodyweightEngine.workoutOfTheDay(BodyweightWorkouts.circuits, Dates.today())

    /**
     * Start a circuit at a level.
     *
     * The stored prescriptions are rewritten first, because the level changes
     * every number in them. That makes this a write on a shared template, which
     * is fine — the template belongs to this feature and nothing else reads it
     * concurrently — and it means the routine detail screen, the session logger
     * and the rest timer all agree without any of them knowing about levels.
     *
     * Returns the session id, or null if the circuit has no routine row.
     */
    suspend fun startCircuit(
        circuit: BodyweightCircuit,
        level: BodyweightLevel,
        bodyweightKg: Double
    ): Long? {
        val dayId = represcribe(circuit, level) ?: return null
        return workouts.startSession(circuit.name, dayId, bodyweightKg)
    }

    /**
     * Rewrite one circuit's prescriptions at the given level and return the day
     * id. Exposed separately so the detail screen can preview a level without
     * committing to a session.
     */
    suspend fun represcribe(circuit: BodyweightCircuit, level: BodyweightLevel): Long? {
        val routine = routineDao.routineBySlug(circuit.slug) ?: return null
        val day = routineDao.daysFor(routine.id).firstOrNull() ?: return null
        val resolved = BodyweightEngine.resolve(circuit, level)

        routineDao.deletePrescriptions(day.id)
        routineDao.insertPrescriptions(
            resolved.moves.mapIndexedNotNull { order, move ->
                val exercise = exerciseDao.bySlug(move.move.exerciseSlug)
                    ?: return@mapIndexedNotNull null
                RoutineExerciseEntity(
                    dayId = day.id,
                    exerciseId = exercise.id,
                    orderIndex = order,
                    targetSets = resolved.rounds,
                    repsMin = move.reps,
                    repsMax = move.reps,
                    restSeconds = resolved.restSeconds,
                    notes = if (move.move.note.isBlank()) move.prescription
                    else "${move.prescription} · ${move.move.note}"
                )
            }
        )
        routineDao.insertRoutine(routine.copy(level = level.name))
        return day.id
    }

    /** Display names for the moves in a circuit, so the UI never queries the DB itself. */
    suspend fun moveNames(circuit: BodyweightCircuit): Map<String, String> =
        circuit.moves.associate { move ->
            move.exerciseSlug to (exerciseDao.bySlug(move.exerciseSlug)?.name ?: move.exerciseSlug)
        }

    /** Exercise ids for the moves, so a card can deep-link into the library. */
    suspend fun moveIds(circuit: BodyweightCircuit): Map<String, Long> =
        circuit.moves.mapNotNull { move ->
            exerciseDao.bySlug(move.exerciseSlug)?.let { move.exerciseSlug to it.id }
        }.toMap()

    companion object {
        val focuses: List<BodyweightFocus> = BodyweightFocus.entries
        val levels: List<BodyweightLevel> = BodyweightLevel.entries
    }
}
