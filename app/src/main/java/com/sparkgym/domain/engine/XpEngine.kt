package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.HunterClass
import com.sparkgym.domain.model.Rank
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Everything that turns real training into XP.
 *
 * The rules are deliberately transparent: a user should be able to look at a
 * session and roughly predict the reward. Nothing is random.
 */
object XpEngine {

    // Base rewards
    private const val XP_PER_COMPLETED_SET = 6
    private const val XP_PER_1000KG_VOLUME = 10
    private const val XP_SESSION_COMPLETION = 40
    private const val XP_PER_PR = 60
    private const val XP_PER_10_MINUTES = 5

    /** Streak multiplier tops out at +50% so week one is not hopeless. */
    fun streakMultiplier(streakDays: Int): Double =
        (1.0 + 0.05 * ln(1.0 + streakDays)).coerceAtMost(1.5)

    data class SessionReward(
        val base: Int,
        val multiplier: Double,
        val total: Int,
        val breakdown: List<Pair<String, Int>>
    )

    fun sessionXp(
        completedSets: Int,
        volumeKg: Double,
        durationMinutes: Int,
        personalRecords: Int,
        streakDays: Int
    ): SessionReward {
        val setXp = completedSets * XP_PER_COMPLETED_SET
        val volumeXp = (volumeKg / 1000.0 * XP_PER_1000KG_VOLUME).roundToInt()
        val timeXp = (durationMinutes / 10) * XP_PER_10_MINUTES
        val prXp = personalRecords * XP_PER_PR
        val completion = if (completedSets > 0) XP_SESSION_COMPLETION else 0

        val breakdown = buildList {
            if (completion > 0) add("Session completed" to completion)
            if (setXp > 0) add("$completedSets sets" to setXp)
            if (volumeXp > 0) add("${volumeKg.roundToInt()} kg moved" to volumeXp)
            if (timeXp > 0) add("$durationMinutes min under the bar" to timeXp)
            if (prXp > 0) add("$personalRecords personal record${if (personalRecords > 1) "s" else ""}" to prXp)
        }

        val base = setXp + volumeXp + timeXp + prXp + completion
        val mult = streakMultiplier(streakDays)
        return SessionReward(base, mult, (base * mult).roundToInt(), breakdown)
    }

    /** Attribute points awarded per level. Rare levels give a bonus. */
    fun pointsForLevelUp(newLevel: Int): Int = when {
        newLevel % 10 == 0 -> 5
        newLevel % 5 == 0 -> 4
        else -> 3
    }

    /** How many levels were gained crossing from one XP total to another. */
    fun levelsGained(oldXp: Long, newXp: Long): IntRange? {
        val before = com.sparkgym.domain.model.LevelCurve.levelForTotalXp(oldXp)
        val after = com.sparkgym.domain.model.LevelCurve.levelForTotalXp(newXp)
        return if (after > before) (before + 1)..after else null
    }
}

/**
 * Attributes drift toward what your data says you actually are. Each attribute
 * is a 0-99 score derived from a rolling window, then nudged upward only — the
 * System does not take away what you earned, it just stops giving more.
 */
object AttributeEngine {

    data class Inputs(
        val weeklyEffectiveSets: Double,
        val weeklyVolumeKg: Double,
        val bestRelativeStrength: Double,
        val avgDailySteps: Double,
        val weeklyCardioMinutes: Double,
        val weeklyActiveCalories: Double,
        val nutritionLoggedDays: Int,
        val proteinTargetHitDays: Int,
        val avgSleepMinutes: Double,
        val restingHeartRate: Int?
    )

    /** Target value each attribute is pulled toward, before the "never decrease" rule. */
    fun targets(inputs: Inputs): Map<Attribute, Int> = mapOf(
        Attribute.STRENGTH to scale(inputs.bestRelativeStrength, 0.0, 120.0),
        Attribute.VITALITY to scale(inputs.weeklyEffectiveSets, 0.0, 120.0),
        Attribute.AGILITY to scale(inputs.avgDailySteps, 0.0, 15000.0),
        Attribute.ENDURANCE to scale(
            inputs.weeklyCardioMinutes * 3 + inputs.weeklyActiveCalories / 25.0,
            0.0, 300.0
        ),
        Attribute.INTELLECT to scale(
            inputs.nutritionLoggedDays * 6.0 + inputs.proteinTargetHitDays * 8.0,
            0.0, 98.0
        ),
        Attribute.PERCEPTION to perception(inputs.avgSleepMinutes, inputs.restingHeartRate)
    )

    /**
     * Applies the targets to the current sheet. Attributes rise by at most one
     * point per recalculation, so the status window animates instead of jumping.
     */
    fun step(current: Map<Attribute, Int>, targets: Map<Attribute, Int>): Map<Attribute, Int> =
        Attribute.entries.associateWith { attr ->
            val now = current[attr] ?: 10
            val target = targets[attr] ?: now
            if (target > now) now + 1 else now
        }

    private fun scale(value: Double, min: Double, max: Double): Int {
        if (max <= min) return 10
        val t = ((value - min) / (max - min)).coerceIn(0.0, 1.0)
        return (10 + t * 89).roundToInt()
    }

    private fun perception(avgSleepMinutes: Double, restingHr: Int?): Int {
        // 7.5 h of sleep is the ceiling; more is not rewarded further.
        val sleepScore = (avgSleepMinutes / 450.0).coerceIn(0.0, 1.0)
        // 45 bpm resting is elite, 80 is untrained.
        val hrScore = restingHr?.let { ((80.0 - it) / 35.0).coerceIn(0.0, 1.0) } ?: 0.4
        return (10 + (sleepScore * 0.6 + hrScore * 0.4) * 89).roundToInt()
    }

    /** Class is a read of the training bias, recomputed whenever attributes change. */
    fun classify(attributes: Map<Attribute, Int>, rank: Rank): HunterClass {
        if (rank == Rank.S && attributes.values.all { it >= 80 }) return HunterClass.MONARCH
        val top = attributes.maxByOrNull { it.value } ?: return HunterClass.AWAKENED
        val spread = (attributes.values.maxOrNull() ?: 0) - (attributes.values.minOrNull() ?: 0)
        if (spread <= 12) return HunterClass.FIGHTER
        return when (top.key) {
            Attribute.STRENGTH -> HunterClass.TANK
            Attribute.VITALITY -> HunterClass.ASSASSIN
            Attribute.AGILITY, Attribute.ENDURANCE -> HunterClass.RANGER
            Attribute.INTELLECT, Attribute.PERCEPTION -> HunterClass.MAGE
        }
    }
}
