package com.sparkgym.data.repository

import com.sparkgym.data.local.ExerciseEntity
import com.sparkgym.data.local.ExerciseMuscleEntity
import com.sparkgym.data.local.FoodEntity
import com.sparkgym.data.local.RoutineDayEntity
import com.sparkgym.data.local.RoutineEntity
import com.sparkgym.data.local.RoutineExerciseEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.FoodSeed
import com.sparkgym.data.seed.RoutineSeed

/**
 * First-run population. Idempotent: everything keys off a slug, so shipping
 * more exercises in a later version only adds the new ones.
 */
class SeedRepository(private val db: SparkGymDatabase) {

    suspend fun seedIfNeeded() {
        seedExercises()
        seedRoutines()
        seedFoods()
    }

    private suspend fun seedExercises() {
        val dao = db.exerciseDao()
        val existing = ExerciseSeed.exercises.associateWith { dao.bySlug(it.slug) }

        val missing = existing.filterValues { it == null }.keys
        if (missing.isEmpty()) return

        for (seed in missing) {
            val id = dao.insert(
                ExerciseEntity(
                    slug = seed.slug,
                    name = seed.name,
                    equipment = seed.equipment,
                    force = seed.force,
                    difficulty = seed.difficulty,
                    tracking = seed.tracking,
                    instructions = seed.instructions,
                    mediaQuery = "${seed.name} proper form"
                )
            )
            val links = seed.primary.map { ExerciseMuscleEntity(id, it.name, PRIMARY_CONTRIBUTION) } +
                seed.secondary
                    .filterNot { it in seed.primary }
                    .map { ExerciseMuscleEntity(id, it.name, SECONDARY_CONTRIBUTION) }
            dao.insertMuscles(links)
        }
    }

    private suspend fun seedRoutines() {
        val routineDao = db.routineDao()
        val exerciseDao = db.exerciseDao()
        if (routineDao.count() > 0) return

        for (seed in RoutineSeed.routines) {
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

    private suspend fun seedFoods() {
        val dao = db.nutritionDao()
        if (dao.foodCount() > 0) return
        dao.insertFoods(
            FoodSeed.foods.map { seed ->
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
    }
}
