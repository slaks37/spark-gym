package com.sparkgym.domain.engine

import com.sparkgym.domain.model.TrackingType
import kotlin.math.roundToInt

/**
 * Auto-regulation: what you should be lifting today, based on what you lifted
 * last time.
 *
 * The rule is double progression — hold the weight until you hit the top of the
 * rep range on every set, then add load and drop back to the bottom of the range.
 * It is the least clever progression model that actually works, which is exactly
 * why it works.
 */
object ProgressionEngine {

    /** Smallest jump worth making, by movement size. */
    fun increment(tracking: TrackingType, isLowerBody: Boolean, currentWeightKg: Double): Double {
        if (tracking == TrackingType.REPS_ONLY || tracking == TrackingType.DURATION) return 0.0
        // Below 40 kg, a fixed 2.5 kg jump is a huge relative increase — scale it.
        val relative = currentWeightKg * 0.025
        val floor = if (isLowerBody) 5.0 else 2.5
        return maxOf(floor, roundToPlate(relative, isLowerBody))
    }

    private fun roundToPlate(value: Double, isLowerBody: Boolean): Double {
        val step = if (isLowerBody) 5.0 else 2.5
        return ((value / step).roundToInt() * step).coerceAtLeast(step)
    }

    data class Suggestion(
        val weightKg: Double,
        val targetReps: Int,
        val rationale: String,
        val isProgression: Boolean
    )

    /**
     * @param lastSets working sets from the most recent session with this exercise,
     *        newest session only, warm-ups excluded
     */
    fun suggest(
        lastSets: List<Pair<Double, Int>>,
        repsMin: Int,
        repsMax: Int,
        tracking: TrackingType,
        isLowerBody: Boolean
    ): Suggestion? {
        if (lastSets.isEmpty()) return null

        val topWeight = lastSets.maxOf { it.first }
        val setsAtTopWeight = lastSets.filter { it.first >= topWeight - 0.01 }
        val allHitCeiling = setsAtTopWeight.isNotEmpty() && setsAtTopWeight.all { it.second >= repsMax }
        val worstReps = setsAtTopWeight.minOf { it.second }

        if (tracking == TrackingType.REPS_ONLY) {
            val best = lastSets.maxOf { it.second }
            return Suggestion(
                weightKg = 0.0,
                targetReps = best + 1,
                rationale = "Bodyweight movement — beat last time by one rep.",
                isProgression = true
            )
        }

        return if (allHitCeiling) {
            val step = increment(tracking, isLowerBody, topWeight)
            Suggestion(
                weightKg = topWeight + step,
                targetReps = repsMin,
                rationale = "You hit $repsMax on every set at ${fmt(topWeight)} kg. Add ${fmt(step)} kg and " +
                    "start again at $repsMin.",
                isProgression = true
            )
        } else {
            Suggestion(
                weightKg = topWeight,
                targetReps = (worstReps + 1).coerceAtMost(repsMax),
                rationale = "Same ${fmt(topWeight)} kg. You managed $worstReps last time — get " +
                    "${(worstReps + 1).coerceAtMost(repsMax)} on every set before the weight moves.",
                isProgression = false
            )
        }
    }

    /**
     * A lift has stalled when its best estimated 1RM has not improved over the
     * last three sessions. Two data points is noise; three is a pattern.
     */
    fun hasStalled(estimated1RmHistory: List<Double>, sessions: Int = 3): Boolean {
        if (estimated1RmHistory.size < sessions + 1) return false
        val recent = estimated1RmHistory.takeLast(sessions)
        val baseline = estimated1RmHistory.dropLast(sessions).max()
        // Anything under a 1% improvement is inside measurement noise.
        return recent.max() <= baseline * 1.01
    }

    data class DeloadVerdict(
        val recommended: Boolean,
        val score: Int,
        val reasons: List<String>
    )

    /**
     * Deload decisions are usually made too late, and always by feel. These are
     * the four signals that actually predict it: accumulated volume, poor sleep,
     * a long unbroken run of training, and lifts that have stopped moving.
     */
    fun deloadCheck(
        weeklyEffectiveSets: Double,
        avgSleepMinutes: Double,
        consecutiveTrainingWeeks: Int,
        stalledLifts: Int,
        restingHrTrend: Int?
    ): DeloadVerdict {
        var score = 0
        val reasons = mutableListOf<String>()

        if (weeklyEffectiveSets > 140) {
            score += 2
            reasons += "Weekly volume is very high (${weeklyEffectiveSets.roundToInt()} effective sets)."
        } else if (weeklyEffectiveSets > 110) {
            score += 1
            reasons += "Weekly volume is on the high side."
        }

        if (avgSleepMinutes in 1.0..360.0) {
            score += 2
            reasons += "Averaging under six hours of sleep — recovery is the limiting factor, not effort."
        } else if (avgSleepMinutes in 1.0..400.0) {
            score += 1
            reasons += "Sleep is a little short of seven hours."
        }

        if (consecutiveTrainingWeeks >= 8) {
            score += 2
            reasons += "$consecutiveTrainingWeeks weeks without a break."
        } else if (consecutiveTrainingWeeks >= 6) {
            score += 1
            reasons += "$consecutiveTrainingWeeks weeks of unbroken training."
        }

        if (stalledLifts >= 3) {
            score += 2
            reasons += "$stalledLifts main lifts have stopped progressing."
        } else if (stalledLifts >= 2) {
            score += 1
            reasons += "Two lifts have stalled."
        }

        restingHrTrend?.let {
            if (it >= 5) {
                score += 1
                reasons += "Resting heart rate is up $it bpm on your baseline."
            }
        }

        return DeloadVerdict(
            recommended = score >= 4,
            score = score,
            reasons = reasons
        )
    }

    private fun fmt(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(java.util.Locale.US, "%.1f", value)
}
