package com.sparkgym.domain.engine

import com.sparkgym.domain.model.TrackingType
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Estimated 1RM and set-volume maths, kept in one place so the logger, the PR
 * detector and the strength attribute all agree on the numbers.
 */
object StrengthMath {

    /**
     * Epley below 10 reps, Brzycki above — Epley drifts high on long sets and
     * Brzycki collapses past 12, so we blend rather than pick a favourite.
     */
    fun estimatedOneRepMax(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0 || reps <= 0) return 0.0
        if (reps == 1) return weightKg
        if (reps > 15) return weightKg * (1 + reps / 30.0) // rough, and flagged as such in the UI
        val epley = weightKg * (1 + reps / 30.0)
        val brzycki = weightKg * 36.0 / (37.0 - reps)
        val t = ((reps - 5).coerceIn(0, 5)) / 5.0
        return epley * (1 - t) + brzycki * t
    }

    /** Weight you should be able to move for [targetReps], given a 1RM. */
    fun weightForReps(oneRm: Double, targetReps: Int): Double {
        if (oneRm <= 0 || targetReps <= 0) return 0.0
        return oneRm / (1 + targetReps / 30.0)
    }

    /**
     * Volume load for a set. Bodyweight movements are charged at a fraction of
     * bodyweight so a set of push-ups is not worth zero and not worth a deadlift.
     */
    fun setVolumeKg(
        tracking: TrackingType,
        weightKg: Double,
        reps: Int,
        durationSeconds: Int,
        distanceMeters: Double,
        bodyweightKg: Double
    ): Double = when (tracking) {
        TrackingType.WEIGHT_REPS -> weightKg * reps
        TrackingType.REPS_ONLY -> bodyweightKg * BODYWEIGHT_LOAD_FACTOR * reps
        TrackingType.BODYWEIGHT_PLUS -> (bodyweightKg + weightKg) * reps
        // A plank is load × time; treat every 3 seconds as a "rep" of bodyweight.
        TrackingType.DURATION -> bodyweightKg * BODYWEIGHT_LOAD_FACTOR * (durationSeconds / 3.0)
        // Running volume is distance-based; scaled so 5 km ≈ a moderate lifting session.
        TrackingType.DISTANCE_DURATION -> distanceMeters * bodyweightKg / 1000.0
    }

    /** Rough calorie burn for a logged set, used when no wearable data exists. */
    fun estimatedCalories(volumeKg: Double, durationSeconds: Int): Int {
        val fromVolume = volumeKg * 0.0008
        val fromTime = durationSeconds * 0.08
        return (fromVolume + fromTime).roundToInt().coerceAtLeast(0)
    }

    /** Plate breakdown per side for a barbell lift. */
    fun platesPerSide(totalKg: Double, barKg: Double = 20.0, available: List<Double> = DEFAULT_PLATES): List<Double> {
        var perSide = (totalKg - barKg) / 2.0
        if (perSide <= 0) return emptyList()
        val result = mutableListOf<Double>()
        for (plate in available.sortedDescending()) {
            while (perSide >= plate - 0.001) {
                result += plate
                perSide -= plate
            }
        }
        return result
    }

    /**
     * Wilks-style relative strength score so a 60 kg and a 100 kg lifter can be
     * compared. Uses the 2020 IPF GL coefficients, men's raw, as a single curve —
     * good enough for a personal progress number.
     */
    fun relativeStrengthScore(totalKg: Double, bodyweightKg: Double): Double {
        if (totalKg <= 0 || bodyweightKg <= 0) return 0.0
        val a = 1199.72839
        val b = 1025.18162
        val c = 0.00921
        val denominator = a - b * kotlin.math.exp(-c * bodyweightKg)
        if (denominator <= 0) return 0.0
        return totalKg * 100.0 / denominator
    }

    private const val BODYWEIGHT_LOAD_FACTOR = 0.65
    private val DEFAULT_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
}

/** Mifflin-St Jeor plus activity, the standard the nutrition screen budgets from. */
object EnergyMath {

    enum class Sex { MALE, FEMALE }

    enum class ActivityLevel(val label: String, val multiplier: Double) {
        SEDENTARY("Sedentary — desk job, no training", 1.2),
        LIGHT("Light — 1-3 sessions a week", 1.375),
        MODERATE("Moderate — 3-5 sessions a week", 1.55),
        HIGH("High — 6-7 sessions a week", 1.725),
        ATHLETE("Athlete — twice daily", 1.9)
    }

    enum class Goal(val label: String, val calorieDelta: Double, val proteinPerKg: Double) {
        CUT("Cut — lose fat", -0.20, 2.2),
        MAINTAIN("Maintain", 0.0, 1.8),
        LEAN_BULK("Lean bulk", 0.10, 2.0),
        BULK("Bulk — gain size", 0.20, 1.8)
    }

    fun bmr(sex: Sex, weightKg: Double, heightCm: Double, age: Int): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (sex == Sex.MALE) base + 5 else base - 161
    }

    fun tdee(sex: Sex, weightKg: Double, heightCm: Double, age: Int, activity: ActivityLevel): Double =
        bmr(sex, weightKg, heightCm, age) * activity.multiplier

    data class MacroTarget(
        val calories: Int,
        val proteinG: Int,
        val carbsG: Int,
        val fatG: Int
    )

    /**
     * Protein is set from bodyweight, fat gets 25% of calories with a floor, and
     * carbohydrate takes whatever remains — the order every decent coach uses.
     */
    fun macroTarget(tdee: Double, goal: Goal, weightKg: Double): MacroTarget {
        val calories = (tdee * (1 + goal.calorieDelta)).coerceAtLeast(1200.0)
        val proteinG = (weightKg * goal.proteinPerKg).roundToInt()
        val fatG = ((calories * 0.25) / 9.0).roundToInt().coerceAtLeast((weightKg * 0.6).roundToInt())
        val remaining = calories - proteinG * 4 - fatG * 9
        val carbsG = (remaining / 4.0).roundToInt().coerceAtLeast(0)
        return MacroTarget(calories.roundToInt(), proteinG, carbsG, fatG)
    }

    /** Daily water target: 35 ml per kg, plus 500 ml on a training day. */
    fun waterTargetMl(weightKg: Double, trainedToday: Boolean): Int =
        (weightKg * 35).roundToInt() + if (trainedToday) 500 else 0

    fun bmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val m = heightCm / 100.0
        return weightKg / m.pow(2)
    }
}
