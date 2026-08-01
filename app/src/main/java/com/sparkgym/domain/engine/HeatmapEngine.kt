package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Muscle

/**
 * Turns "effective sets in the last N days" into a 0..1 heat value.
 *
 * The landmarks are the volume ones most hypertrophy coaches work from:
 * roughly 8 hard sets a week is the minimum that does anything, ~20 is where
 * most people stop gaining more from more, and past ~26 you are usually just
 * accumulating fatigue. Small muscles tolerate less absolute volume than big
 * ones, so each muscle carries its own target.
 */
object HeatmapEngine {

    /** Weekly effective sets at which a muscle is considered fully trained. */
    private val WEEKLY_TARGET: Map<Muscle, Double> = mapOf(
        Muscle.CHEST to 18.0,
        Muscle.LATS to 20.0,
        Muscle.TRAPS to 14.0,
        Muscle.LOWER_BACK to 10.0,
        Muscle.FRONT_DELTS to 12.0,
        Muscle.SIDE_DELTS to 18.0,
        Muscle.REAR_DELTS to 16.0,
        Muscle.BICEPS to 16.0,
        Muscle.TRICEPS to 16.0,
        Muscle.FOREARMS to 12.0,
        Muscle.ABS to 14.0,
        Muscle.OBLIQUES to 10.0,
        Muscle.QUADS to 18.0,
        Muscle.HAMSTRINGS to 14.0,
        Muscle.GLUTES to 14.0,
        Muscle.ADDUCTORS to 8.0,
        Muscle.ABDUCTORS to 8.0,
        Muscle.CALVES to 14.0,
        Muscle.NECK to 6.0
    )

    private const val DEFAULT_TARGET = 14.0

    enum class Status(val label: String) {
        UNTRAINED("Untrained"),
        UNDER("Under-trained"),
        OPTIMAL("On target"),
        HIGH("High volume"),
        OVERREACHED("Overreaching")
    }

    data class MuscleHeat(
        val muscle: Muscle,
        val effectiveSets: Double,
        val volumeKg: Double,
        val target: Double,
        /** 0..1 for the colour ramp; values above 1 are clamped for drawing. */
        val intensity: Float,
        val status: Status
    )

    fun targetFor(muscle: Muscle): Double = WEEKLY_TARGET[muscle] ?: DEFAULT_TARGET

    /**
     * @param days how many days the sets were collected over, so a 30-day view
     *        is normalised back to a weekly rate rather than looking blood red.
     */
    fun build(
        setsByMuscle: Map<Muscle, Double>,
        volumeByMuscle: Map<Muscle, Double> = emptyMap(),
        days: Int = 7
    ): Map<Muscle, MuscleHeat> {
        val weeks = (days / 7.0).coerceAtLeast(1.0 / 7.0)
        return Muscle.entries.associateWith { muscle ->
            val raw = setsByMuscle[muscle] ?: 0.0
            val weekly = raw / weeks
            val target = targetFor(muscle)
            val ratio = (weekly / target).toFloat()
            MuscleHeat(
                muscle = muscle,
                effectiveSets = raw,
                volumeKg = volumeByMuscle[muscle] ?: 0.0,
                target = target,
                intensity = ratio.coerceIn(0f, 1.4f),
                status = when {
                    weekly <= 0.01 -> Status.UNTRAINED
                    ratio < 0.55f -> Status.UNDER
                    ratio < 1.0f -> Status.OPTIMAL
                    ratio < 1.35f -> Status.HIGH
                    else -> Status.OVERREACHED
                }
            )
        }
    }

    /**
     * The muscles worth nagging the user about: trained least relative to target.
     * Neck is excluded because almost nobody trains it deliberately and it would
     * permanently occupy the top of the list.
     */
    fun weakestLinks(heat: Map<Muscle, MuscleHeat>, count: Int = 3): List<MuscleHeat> =
        heat.values
            .filter { it.muscle != Muscle.NECK }
            .sortedBy { it.intensity }
            .take(count)

    fun balanceScore(heat: Map<Muscle, MuscleHeat>): Int {
        val values = heat.values.filter { it.muscle != Muscle.NECK }.map { it.intensity.coerceAtMost(1f) }
        if (values.isEmpty()) return 0
        val mean = values.average()
        if (mean <= 0.0) return 0
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        val cv = kotlin.math.sqrt(variance) / mean
        // Perfect balance (cv = 0) is 100; a coefficient of variation of 1 is 0.
        return ((1.0 - cv).coerceIn(0.0, 1.0) * 100).toInt()
    }
}
