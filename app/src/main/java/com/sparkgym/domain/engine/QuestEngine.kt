package com.sparkgym.domain.engine

import com.sparkgym.domain.model.QuestMetric
import com.sparkgym.domain.model.QuestSource
import com.sparkgym.domain.model.QuestTemplate
import kotlin.math.roundToInt

/**
 * Builds the day's quest board.
 *
 * Targets scale with the player's level so the board stays just out of reach
 * without ever becoming absurd — the ceiling on every metric is the point where
 * more would be worse for the user, not better.
 */
object QuestEngine {

    private val CORE = listOf(
        QuestTemplate(QuestMetric.PUSHUPS, 30.0, 40, QuestSource.MANUAL, true),
        QuestTemplate(QuestMetric.SITUPS, 30.0, 40, QuestSource.MANUAL, true),
        QuestTemplate(QuestMetric.SQUATS, 30.0, 40, QuestSource.MANUAL, true),
        QuestTemplate(QuestMetric.STEPS, 6000.0, 50, QuestSource.AUTOMATIC, true)
    )

    private val SUPPORT = listOf(
        QuestTemplate(QuestMetric.PROTEIN_G, 100.0, 35, QuestSource.AUTOMATIC, false),
        QuestTemplate(QuestMetric.WATER_ML, 2000.0, 25, QuestSource.AUTOMATIC, false),
        QuestTemplate(QuestMetric.WORKOUT_SETS, 12.0, 45, QuestSource.AUTOMATIC, false),
        QuestTemplate(QuestMetric.SLEEP_MINUTES, 420.0, 30, QuestSource.AUTOMATIC, false),
        QuestTemplate(QuestMetric.ACTIVE_CALORIES, 350.0, 30, QuestSource.AUTOMATIC, false)
    )

    /** Caps stop the scaling from producing something nobody should attempt. */
    private val CAPS = mapOf(
        QuestMetric.PUSHUPS to 100.0,
        QuestMetric.SITUPS to 100.0,
        QuestMetric.SQUATS to 100.0,
        QuestMetric.STEPS to 12000.0,
        QuestMetric.PROTEIN_G to 200.0,
        QuestMetric.WATER_ML to 3500.0,
        QuestMetric.WORKOUT_SETS to 28.0,
        QuestMetric.SLEEP_MINUTES to 480.0,
        QuestMetric.ACTIVE_CALORIES to 800.0
    )

    data class GeneratedQuest(
        val metric: QuestMetric,
        val target: Double,
        val xpReward: Int,
        val source: QuestSource,
        val isCore: Boolean
    )

    /**
     * @param level the hunter level, which drives scaling
     * @param proteinTargetG the nutrition goal, so the protein quest matches the plan
     * @param weightKg used for the water quest
     */
    fun generate(
        level: Int,
        proteinTargetG: Int?,
        weightKg: Double?,
        includeSupport: Boolean = true
    ): List<GeneratedQuest> {
        val growth = 1.0 + (level - 1) * 0.045

        val core = CORE.map { template ->
            val raw = template.baseTarget * growth
            val capped = CAPS[template.metric]?.let { minOf(raw, it) } ?: raw
            GeneratedQuest(
                metric = template.metric,
                target = round(template.metric, capped),
                xpReward = template.xpReward,
                source = template.source,
                isCore = true
            )
        }

        if (!includeSupport) return core

        val support = SUPPORT.map { template ->
            val raw = when (template.metric) {
                // These two come from the user's own plan rather than a curve.
                QuestMetric.PROTEIN_G -> proteinTargetG?.toDouble() ?: (template.baseTarget * growth)
                QuestMetric.WATER_ML -> weightKg?.let { EnergyMath.waterTargetMl(it, false).toDouble() }
                    ?: (template.baseTarget * growth)
                else -> template.baseTarget * growth
            }
            val capped = CAPS[template.metric]?.let { minOf(raw, it) } ?: raw
            GeneratedQuest(
                metric = template.metric,
                target = round(template.metric, capped),
                xpReward = template.xpReward,
                source = template.source,
                isCore = false
            )
        }

        return core + support
    }

    /** Bonus for finishing every quest on the board. */
    fun completionBonus(questCount: Int, allCore: Boolean): Int =
        if (allCore) 80 + questCount * 10 else 0

    /**
     * Missing the core quests costs XP. It is capped and never takes you below
     * your current level's floor — a bad week should sting, not erase a month.
     */
    fun penalty(consecutiveMissedDays: Int): Int =
        (25 * consecutiveMissedDays).coerceAtMost(150)

    private fun round(metric: QuestMetric, value: Double): Double = when (metric) {
        QuestMetric.STEPS -> (value / 500).roundToInt() * 500.0
        QuestMetric.WATER_ML -> (value / 250).roundToInt() * 250.0
        QuestMetric.SLEEP_MINUTES -> (value / 15).roundToInt() * 15.0
        QuestMetric.ACTIVE_CALORIES -> (value / 25).roundToInt() * 25.0
        QuestMetric.PROTEIN_G -> (value / 5).roundToInt() * 5.0
        else -> value.roundToInt().toDouble()
    }
}
