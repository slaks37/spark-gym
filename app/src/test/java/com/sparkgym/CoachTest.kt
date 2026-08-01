package com.sparkgym

import com.sparkgym.domain.model.Meal
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.FoodSeed
import com.sparkgym.data.seed.MealPlanSeed
import com.sparkgym.data.seed.RoutineSeed
import com.sparkgym.domain.engine.CoachEngine
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.MealPlanEngine
import com.sparkgym.domain.engine.ProgressionEngine
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.TrackingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionEngineTest {

    @Test
    fun `hitting the top of the range adds weight and resets reps`() {
        val suggestion = ProgressionEngine.suggest(
            lastSets = listOf(80.0 to 8, 80.0 to 8, 80.0 to 8),
            repsMin = 6, repsMax = 8,
            tracking = TrackingType.WEIGHT_REPS,
            isLowerBody = false
        )
        assertNotNull(suggestion)
        assertTrue(suggestion!!.isProgression)
        assertTrue(suggestion.weightKg > 80.0)
        assertEquals(6, suggestion.targetReps)
    }

    @Test
    fun `missing the top of the range holds the weight`() {
        val suggestion = ProgressionEngine.suggest(
            lastSets = listOf(80.0 to 8, 80.0 to 7, 80.0 to 6),
            repsMin = 6, repsMax = 8,
            tracking = TrackingType.WEIGHT_REPS,
            isLowerBody = false
        )!!
        assertFalse(suggestion.isProgression)
        assertEquals(80.0, suggestion.weightKg, 0.001)
        assertEquals(7, suggestion.targetReps)
    }

    @Test
    fun `lower body gets a bigger jump than upper body`() {
        val upper = ProgressionEngine.increment(TrackingType.WEIGHT_REPS, isLowerBody = false, currentWeightKg = 60.0)
        val lower = ProgressionEngine.increment(TrackingType.WEIGHT_REPS, isLowerBody = true, currentWeightKg = 60.0)
        assertTrue(lower > upper)
    }

    @Test
    fun `no history means no suggestion`() {
        assertNull(
            ProgressionEngine.suggest(emptyList(), 6, 8, TrackingType.WEIGHT_REPS, false)
        )
    }

    @Test
    fun `bodyweight movements progress by reps`() {
        val suggestion = ProgressionEngine.suggest(
            lastSets = listOf(0.0 to 15, 0.0 to 12),
            repsMin = 8, repsMax = 20,
            tracking = TrackingType.REPS_ONLY,
            isLowerBody = false
        )!!
        assertEquals(16, suggestion.targetReps)
        assertEquals(0.0, suggestion.weightKg, 0.001)
    }

    @Test
    fun `a flat estimated 1RM series is a stall`() {
        val flat = listOf(100.0, 102.0, 104.0, 104.2, 104.0, 103.5)
        assertTrue(ProgressionEngine.hasStalled(flat))
    }

    @Test
    fun `a climbing series is not a stall`() {
        val climbing = listOf(100.0, 102.0, 104.0, 107.0, 110.0, 113.0)
        assertFalse(ProgressionEngine.hasStalled(climbing))
    }

    @Test
    fun `too little history never reports a stall`() {
        assertFalse(ProgressionEngine.hasStalled(listOf(100.0, 100.0)))
    }

    @Test
    fun `deload fires when several fatigue signals stack up`() {
        val verdict = ProgressionEngine.deloadCheck(
            weeklyEffectiveSets = 150.0,
            avgSleepMinutes = 340.0,
            consecutiveTrainingWeeks = 9,
            stalledLifts = 3,
            restingHrTrend = 6
        )
        assertTrue(verdict.recommended)
        assertTrue(verdict.reasons.size >= 4)
    }

    @Test
    fun `a well recovered lifter is not told to deload`() {
        val verdict = ProgressionEngine.deloadCheck(
            weeklyEffectiveSets = 70.0,
            avgSleepMinutes = 460.0,
            consecutiveTrainingWeeks = 3,
            stalledLifts = 0,
            restingHrTrend = 0
        )
        assertFalse(verdict.recommended)
        assertEquals(0, verdict.score)
    }

    @Test
    fun `missing sleep data does not fake a fatigue signal`() {
        val verdict = ProgressionEngine.deloadCheck(
            weeklyEffectiveSets = 60.0,
            avgSleepMinutes = 0.0,   // nothing synced
            consecutiveTrainingWeeks = 2,
            stalledLifts = 0,
            restingHrTrend = null
        )
        assertEquals(0, verdict.score)
    }
}

class CoachEngineTest {

    private fun snapshot(
        heat: Map<Muscle, HeatmapEngine.MuscleHeat> = HeatmapEngine.build(emptyMap()),
        weeklySets: Double = 60.0,
        sessionsThisWeek: Int = 3,
        daysSince: Int = 1,
        streak: Int = 3,
        weeks: Int = 3,
        stalled: List<String> = emptyList(),
        prs: Int = 0,
        sleep: Double = 450.0,
        steps: Double = 9000.0,
        hrDelta: Int? = 0,
        proteinTarget: Int = 160,
        avgProtein: Double = 160.0,
        avgCalories: Double = 2400.0,
        calorieTarget: Int = 2400,
        loggedDays: Int = 5,
        weightTrend: Double? = 0.0,
        cut: Boolean = false,
        bulk: Boolean = false,
        totalSessions: Int = 40
    ) = CoachEngine.Snapshot(
        heat, weeklySets, sessionsThisWeek, daysSince, streak, weeks, stalled, prs,
        sleep, steps, hrDelta, proteinTarget, avgProtein, avgCalories, calorieTarget,
        loggedDays, weightTrend, cut, bulk, totalSessions
    )

    @Test
    fun `a new user is told to start, not analysed`() {
        val insights = CoachEngine.analyse(snapshot(totalSessions = 0))
        assertTrue(insights.any { it.category == CoachEngine.Category.CONSISTENCY })
        assertTrue(insights.none { it.category == CoachEngine.Category.BALANCE })
    }

    @Test
    fun `short sleep raises a recovery insight`() {
        val insights = CoachEngine.analyse(snapshot(sleep = 320.0))
        assertTrue(insights.any { it.category == CoachEngine.Category.RECOVERY })
    }

    @Test
    fun `a protein shortfall is flagged with a concrete action`() {
        val insights = CoachEngine.analyse(snapshot(avgProtein = 90.0, proteinTarget = 160))
        val protein = insights.first { it.category == CoachEngine.Category.NUTRITION }
        assertNotNull(protein.action)
        assertTrue(protein.body.contains("90"))
    }

    @Test
    fun `a stalled cut is called out`() {
        val insights = CoachEngine.analyse(snapshot(cut = true, weightTrend = 0.0, loggedDays = 5))
        assertTrue(insights.any { it.title.contains("not moving", ignoreCase = true) })
    }

    @Test
    fun `bulking too fast is called out`() {
        val insights = CoachEngine.analyse(snapshot(bulk = true, weightTrend = 0.9, loggedDays = 5))
        assertTrue(insights.any { it.title.contains("faster", ignoreCase = true) })
    }

    @Test
    fun `pressing far more than pulling raises a balance warning`() {
        val heat = HeatmapEngine.build(
            mapOf(
                Muscle.CHEST to 20.0,
                Muscle.FRONT_DELTS to 10.0,
                Muscle.LATS to 8.0,
                Muscle.REAR_DELTS to 2.0
            )
        )
        val insights = CoachEngine.analyse(snapshot(heat = heat))
        assertTrue(insights.any { it.title.contains("press", ignoreCase = true) })
    }

    @Test
    fun `insights come back ordered by priority`() {
        val insights = CoachEngine.analyse(
            snapshot(sleep = 300.0, avgProtein = 60.0, streak = 10, weeks = 9, weeklySets = 150.0, stalled = listOf("Squat", "Bench", "Row"))
        )
        val ordinals = insights.map { it.priority.ordinal }
        assertEquals(ordinals.sorted(), ordinals)
    }

    @Test
    fun `headline never comes back empty`() {
        assertTrue(CoachEngine.headline(emptyList()).isNotBlank())
        assertTrue(CoachEngine.headline(CoachEngine.analyse(snapshot())).isNotBlank())
    }
}

class MealPlanEngineTest {

    /** Every plan's foods, faked straight from the seed database. */
    private val facts = FoodSeed.foods.associate { food ->
        food.slug to MealPlanEngine.FoodFacts(
            slug = food.slug,
            name = food.name,
            caloriesPer100 = food.kcal,
            proteinPer100 = food.protein,
            carbsPer100 = food.carbs,
            fatPer100 = food.fat
        )
    }

    @Test
    fun `every meal plan references only real foods`() {
        val known = FoodSeed.foods.map { it.slug }.toSet()
        MealPlanSeed.plans.forEach { plan ->
            plan.slots.forEach { slot ->
                slot.items.forEach { item ->
                    assertTrue(
                        "${plan.slug} references unknown food ${item.foodSlug}",
                        item.foodSlug in known
                    )
                }
            }
        }
    }

    @Test
    fun `plans land near their own stated base calories`() {
        MealPlanSeed.plans.forEach { plan ->
            val scaled = MealPlanEngine.scale(plan, plan.baseCalories, 150, facts)
            val drift = kotlin.math.abs(scaled.calories - plan.baseCalories) / plan.baseCalories
            assertTrue(
                "${plan.slug} is ${(drift * 100).toInt()}% off its stated ${plan.baseCalories} kcal",
                drift < 0.20
            )
        }
    }

    @Test
    fun `scaling up increases calories`() {
        val plan = MealPlanSeed.plans.first { it.slug == "maintain-balanced" }
        val small = MealPlanEngine.scale(plan, 1800, 140, facts)
        val large = MealPlanEngine.scale(plan, 3000, 180, facts)
        assertTrue(large.calories > small.calories)
    }

    @Test
    fun `fixed items do not scale`() {
        val plan = MealPlanSeed.plans.first { it.slug == "cut-warung" }
        val small = MealPlanEngine.scale(plan, 1200, 140, facts)
        val large = MealPlanEngine.scale(plan, 3200, 200, facts)

        val smallCoffee = small.slots.flatMap { it.items }.first { it.slug == "kopi-hitam" }
        val largeCoffee = large.slots.flatMap { it.items }.first { it.slug == "kopi-hitam" }
        assertEquals(smallCoffee.grams, largeCoffee.grams, 0.001)
    }

    @Test
    fun `the scale factor is clamped at both ends`() {
        val plan = MealPlanSeed.plans.first { it.slug == "mass-gain" }
        val tiny = MealPlanEngine.scale(plan, 900, 120, facts)
        assertTrue(tiny.scaleFactor >= 0.55)

        val cutPlan = MealPlanSeed.plans.first { it.slug == "cut-warung" }
        val huge = MealPlanEngine.scale(cutPlan, 6000, 250, facts)
        assertTrue(huge.scaleFactor <= 1.8)
    }

    @Test
    fun `a protein shortfall produces actionable advice`() {
        val plan = MealPlanSeed.plans.first { it.slug == "maintain-balanced" }
        val scaled = MealPlanEngine.scale(plan, 2400, 250, facts)
        val advice = MealPlanEngine.adjustmentAdvice(scaled)
        assertTrue(advice.any { it.contains("whey", ignoreCase = true) })
    }

    @Test
    fun `every plan covers at least three meals`() {
        MealPlanSeed.plans.forEach { plan ->
            assertTrue("${plan.slug} has too few meals", plan.slots.size >= 3)
            assertTrue(
                "${plan.slug} has no main meal",
                plan.slots.any { it.meal == Meal.LUNCH || it.meal == Meal.DINNER }
            )
        }
    }
}

class SeedContentTest {

    @Test
    fun `exercise slugs are unique`() {
        val slugs = ExerciseSeed.exercises.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size)
    }

    @Test
    fun `every exercise has at least one prime mover`() {
        ExerciseSeed.exercises.forEach {
            assertTrue("${it.slug} has no primary muscle", it.primary.isNotEmpty())
        }
    }

    @Test
    fun `no exercise lists a muscle as both primary and secondary`() {
        ExerciseSeed.exercises.forEach { exercise ->
            val overlap = exercise.primary.intersect(exercise.secondary.toSet())
            assertTrue("${exercise.slug} double-counts $overlap", overlap.isEmpty())
        }
    }

    @Test
    fun `every muscle the heat map can draw has at least three exercises`() {
        val counts = Muscle.entries.associateWith { muscle ->
            ExerciseSeed.exercises.count { muscle in it.primary || muscle in it.secondary }
        }
        counts.forEach { (muscle, count) ->
            assertTrue("$muscle only has $count exercises", count >= 3)
        }
    }

    @Test
    fun `every routine references real exercises`() {
        val known = ExerciseSeed.exercises.map { it.slug }.toSet()
        RoutineSeed.routines.forEach { routine ->
            routine.days.forEach { day ->
                day.exercises.forEach {
                    assertTrue("${routine.slug} references ${it.exerciseSlug}", it.exerciseSlug in known)
                }
            }
        }
    }

    @Test
    fun `routine slugs are unique and every routine has days`() {
        val slugs = RoutineSeed.routines.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size)
        RoutineSeed.routines.forEach {
            assertTrue("${it.slug} has no days", it.days.isNotEmpty())
            it.days.forEach { day ->
                assertTrue("${it.slug}/${day.name} is empty", day.exercises.isNotEmpty())
            }
        }
    }

    @Test
    fun `prescriptions have sane rep ranges`() {
        RoutineSeed.routines.forEach { routine ->
            routine.days.forEach { day ->
                day.exercises.forEach { p ->
                    assertTrue("${routine.slug}: bad rep range", p.repsMin <= p.repsMax)
                    assertTrue("${routine.slug}: bad set count", p.sets in 1..12)
                    assertTrue("${routine.slug}: bad rest", p.restSeconds in 0..600)
                }
            }
        }
    }

    /** Alcohol carries 7 kcal/g that no macro column accounts for. */
    private val alcoholic = setOf("bir")

    @Test
    fun `food slugs are unique and macros are plausible`() {
        val slugs = FoodSeed.foods.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size)

        FoodSeed.foods.forEach { food ->
            assertTrue("${food.slug} has a non-positive serving", food.servingGrams > 0)
            if (food.kcal <= 20 || food.slug in alcoholic) return@forEach

            // Atwater factors, with fibre at 2 kcal/g rather than 4 — otherwise
            // every leafy vegetable looks like a data error.
            val netCarbs = (food.carbs - food.fiber).coerceAtLeast(0.0)
            val fromMacros = food.protein * 4 + netCarbs * 4 + food.fiber * 2 + food.fat * 9
            val drift = kotlin.math.abs(fromMacros - food.kcal) / food.kcal
            assertTrue(
                "${food.slug}: ${food.kcal} kcal but macros give ${fromMacros.toInt()}",
                drift < 0.25
            )
        }
    }
}
