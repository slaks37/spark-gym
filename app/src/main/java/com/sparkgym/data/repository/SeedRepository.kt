package com.sparkgym.data.repository

import com.sparkgym.data.local.ExerciseEntity
import com.sparkgym.data.local.ExerciseMuscleEntity
import com.sparkgym.data.local.FoodEntity
import com.sparkgym.data.local.RoutineDayEntity
import com.sparkgym.data.local.RoutineEntity
import com.sparkgym.data.local.RoutineExerciseEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.seed.BodyweightWorkouts
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.FoodSeed
import com.sparkgym.data.seed.RoutineSeed
import com.sparkgym.domain.engine.BodyweightEngine
import com.sparkgym.domain.model.BodyweightLevel

/**
 * First-run population. Idempotent: everything keys off a slug, so shipping
 * more exercises in a later version only adds the new ones.
 */
class SeedRepository(private val db: SparkGymDatabase) {

    suspend fun seedIfNeeded() {
        seedExercises()
        seedRoutines()
        seedBodyweightCircuits()
        seedFoods()
    }

    /**
     * Circuits are stored as ordinary routines — one routine, one day, one
     * prescription per move — so starting one goes through exactly the same
     * session machinery as a gym day, and a finished circuit feeds the heat
     * map, XP and quests without a single special case downstream.
     *
     * Keyed on slug rather than a count, so a later version that ships more
     * circuits adds only the new ones. Prescriptions are written at Level II;
     * [com.sparkgym.data.repository.BodyweightRepository] rewrites them when the
     * user trains at a different level.
     */
    private suspend fun seedBodyweightCircuits() {
        val routineDao = db.routineDao()
        val exerciseDao = db.exerciseDao()

        for (circuit in BodyweightWorkouts.circuits) {
            if (routineDao.routineBySlug(circuit.slug) != null) continue

            val resolved = BodyweightEngine.resolve(circuit, BodyweightLevel.TWO)
            val routineId = routineDao.insertRoutine(
                RoutineEntity(
                    slug = circuit.slug,
                    name = circuit.name,
                    description = circuit.tagline,
                    goal = BodyweightWorkouts.ROUTINE_GOAL,
                    daysPerWeek = 1,
                    level = BodyweightLevel.TWO.name,
                    homeFriendly = true
                )
            )
            val dayId = routineDao.insertDay(
                RoutineDayEntity(
                    routineId = routineId,
                    dayIndex = 0,
                    name = circuit.name,
                    focus = circuit.focus.displayName
                )
            )
            routineDao.insertPrescriptions(
                resolved.moves.mapIndexedNotNull { order, move ->
                    val exercise = exerciseDao.bySlug(move.move.exerciseSlug)
                        ?: return@mapIndexedNotNull null
                    RoutineExerciseEntity(
                        dayId = dayId,
                        exerciseId = exercise.id,
                        orderIndex = order,
                        targetSets = resolved.rounds,
                        repsMin = move.reps,
                        repsMax = move.reps,
                        restSeconds = resolved.restSeconds,
                        notes = buildNote(move.prescription, move.move.note)
                    )
                }
            )
        }
    }

    /**
     * Inserts new exercises and brings existing ones up to date.
     *
     * Keying off the slug and skipping anything already present is enough for a
     * first install, but it means a shipped correction — a fixed instruction, a
     * re-mapped muscle, an equipment type that was wrong — reaches new installs
     * only, and everyone already using the app keeps the mistake forever.
     *
     * So rows are updated in place rather than skipped. Two things are never
     * touched: the row id, because routines, set logs and the muscle join all
     * reference it, and `isFavorite`, which belongs to the user rather than the
     * catalogue. Exercises the user created themselves are left alone entirely.
     *
     * The muscle join is rewritten only when it actually differs, so a routine
     * upgrade does not churn every row on every launch.
     */
    private suspend fun seedExercises() {
        val dao = db.exerciseDao()

        for (seed in ExerciseSeed.exercises) {
            val existing = dao.bySlug(seed.slug)

            if (existing == null) {
                val id = dao.insert(
                    ExerciseEntity(
                        slug = seed.slug,
                        name = seed.name,
                        equipment = seed.equipment,
                        force = seed.force,
                        difficulty = seed.difficulty,
                        tracking = seed.tracking,
                        instructions = seed.instructions,
                        mediaQuery = "${seed.name} proper form",
                        imageUri = seed.imageUri
                    )
                )
                dao.insertMuscles(muscleLinks(id, seed))
                continue
            }

            // Somebody's own exercise that happens to share a slug is theirs.
            if (existing.isCustom) continue

            val updated = existing.copy(
                name = seed.name,
                equipment = seed.equipment,
                force = seed.force,
                difficulty = seed.difficulty,
                tracking = seed.tracking,
                instructions = seed.instructions,
                mediaQuery = "${seed.name} proper form",
                imageUri = seed.imageUri
            )
            if (updated != existing) dao.update(updated)

            val wanted = muscleLinks(existing.id, seed)
            val current = dao.muscleLinksFor(existing.id)
            if (current.toSet() != wanted.toSet()) {
                dao.clearMuscles(existing.id)
                dao.insertMuscles(wanted)
            }
        }
    }

    private fun muscleLinks(exerciseId: Long, seed: com.sparkgym.data.seed.SeedExercise) =
        seed.primary.map { ExerciseMuscleEntity(exerciseId, it.name, PRIMARY_CONTRIBUTION) } +
            seed.secondary
                .filterNot { it in seed.primary }
                .map { ExerciseMuscleEntity(exerciseId, it.name, SECONDARY_CONTRIBUTION) }

    /**
     * Adds routines that are not there yet, and leaves the rest alone.
     *
     * The old guard — bail out if the table has any rows at all — meant a
     * programme shipped in a later version reached first installs only. Keying
     * on slug instead lets new programmes arrive for everyone.
     *
     * Existing routines are deliberately not rewritten: the user may have made
     * one their active programme or reordered a day, and silently replacing
     * that on launch is worse than a stale description.
     */
    private suspend fun seedRoutines() {
        val routineDao = db.routineDao()
        val exerciseDao = db.exerciseDao()

        for (seed in RoutineSeed.routines) {
            if (routineDao.routineBySlug(seed.slug) != null) continue

            val routineId = routineDao.insertRoutine(
                RoutineEntity(
                    slug = seed.slug,
                    name = seed.name,
                    description = seed.description,
                    goal = seed.goal,
                    daysPerWeek = seed.days.size,
                    level = seed.level,
                    homeFriendly = seed.homeFriendly
                )
            )
            seed.days.forEachIndexed { dayIndex, day ->
                val dayId = routineDao.insertDay(
                    RoutineDayEntity(
                        routineId = routineId,
                        dayIndex = dayIndex,
                        name = day.name,
                        focus = day.focus
                    )
                )
                val prescriptions = day.exercises.mapIndexedNotNull { order, p ->
                    val exercise = exerciseDao.bySlug(p.exerciseSlug) ?: return@mapIndexedNotNull null
                    RoutineExerciseEntity(
                        dayId = dayId,
                        exerciseId = exercise.id,
                        orderIndex = order,
                        targetSets = p.sets,
                        repsMin = p.repsMin,
                        repsMax = p.repsMax,
                        restSeconds = p.restSeconds,
                        notes = p.notes
                    )
                }
                routineDao.insertPrescriptions(prescriptions)
            }
        }
    }

    /** Adds foods missing from the table; a user's own entries are untouched. */
    private suspend fun seedFoods() {
        val dao = db.nutritionDao()
        val missing = FoodSeed.foods.filter { dao.foodBySlug(it.slug) == null }
        if (missing.isEmpty()) return
        dao.insertFoods(
            missing.map { seed ->
                FoodEntity(
                    slug = seed.slug,
                    name = seed.name,
                    brand = seed.brand,
                    caloriesPer100 = seed.kcal,
                    proteinPer100 = seed.protein,
                    carbsPer100 = seed.carbs,
                    fatPer100 = seed.fat,
                    fiberPer100 = seed.fiber,
                    servingLabel = seed.servingLabel,
                    servingGrams = seed.servingGrams
                )
            }
        )
    }

    private companion object {
        const val PRIMARY_CONTRIBUTION = 1.0f
        const val SECONDARY_CONTRIBUTION = 0.5f

        /** "40 s · Per side" — the prescription and its caveat in one notes column. */
        fun buildNote(prescription: String, note: String): String =
            if (note.isBlank()) prescription else "$prescription · $note"
    }
}
