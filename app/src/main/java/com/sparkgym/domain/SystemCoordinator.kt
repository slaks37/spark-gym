package com.sparkgym.domain

import com.sparkgym.core.util.Dates
import com.sparkgym.data.prefs.UserPrefs
import com.sparkgym.data.repository.GameRepository
import com.sparkgym.data.repository.NutritionRepository
import com.sparkgym.data.repository.WearableRepository
import com.sparkgym.data.repository.WorkoutRepository
import com.sparkgym.domain.engine.AttributeEngine
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.engine.XpEngine
import com.sparkgym.domain.model.LevelCurve
import com.sparkgym.domain.model.QuestMetric
import kotlinx.coroutines.flow.first

/**
 * The seam where the four halves of the app meet.
 *
 * Logging a set, eating a meal and syncing a watch all have to end up in the
 * same place — quest progress, XP and the attribute sheet — and putting that
 * fan-out in one class keeps every screen from having to know about the rest.
 */
class SystemCoordinator(
    private val workouts: WorkoutRepository,
    private val nutrition: NutritionRepository,
    private val game: GameRepository,
    private val wearables: WearableRepository,
    private val prefs: UserPrefs
) {

    /** Called on app start and at midnight rollover. */
    suspend fun refreshDailyBoard() {
        val profile = prefs.profile.first()
        val hunter = game.observeProfile().first()
        game.ensureTodaysQuests(
            level = hunter.level,
            proteinTargetG = profile.macroTarget.proteinG,
            weightKg = profile.weightKg
        )
        pushAllQuestProgress()
    }

    /** Recomputes every automatic quest from the sources of truth. */
    suspend fun pushAllQuestProgress() {
        val today = Dates.today()

        wearables.today()?.let { day ->
            game.setQuestProgress(QuestMetric.STEPS, day.steps.toDouble(), today)
            game.setQuestProgress(QuestMetric.ACTIVE_CALORIES, day.activeCalories.toDouble(), today)
            game.setQuestProgress(QuestMetric.SLEEP_MINUTES, day.sleepMinutes.toDouble(), today)
        }

        game.setQuestProgress(QuestMetric.PROTEIN_G, nutrition.proteinOn(today), today)
        game.setQuestProgress(QuestMetric.CALORIES_LOGGED, nutrition.entryCountOn(today).toDouble(), today)
        game.setQuestProgress(QuestMetric.WATER_ML, nutrition.observeWater(today).first().toDouble(), today)
    }

    /** After a set is logged, keep the set-count quest honest. */
    suspend fun onSetLogged(sessionId: Long) {
        val sets = workouts.observeSets(sessionId).first().count { it.isCompleted && !it.isWarmup }
        game.setQuestProgress(QuestMetric.WORKOUT_SETS, sets.toDouble())
    }

    suspend fun onFoodLogged() {
        val today = Dates.today()
        game.setQuestProgress(QuestMetric.PROTEIN_G, nutrition.proteinOn(today), today)
        game.setQuestProgress(QuestMetric.CALORIES_LOGGED, nutrition.entryCountOn(today).toDouble(), today)
    }

    suspend fun onWaterLogged() {
        val today = Dates.today()
        game.setQuestProgress(QuestMetric.WATER_ML, nutrition.observeWater(today).first().toDouble(), today)
    }

    data class FinishOutcome(
        val summary: WorkoutRepository.SessionSummary,
        val xp: XpEngine.SessionReward,
        val levelsGained: IntRange?,
        val pointsGained: Int,
        val newAchievements: List<String>
    )

    /**
     * Ends a session and settles everything it earned: XP, level-ups,
     * achievements, quest progress and an attribute recalculation.
     */
    suspend fun finishWorkout(sessionId: Long): FinishOutcome? {
        val summary = workouts.finishSession(sessionId) ?: return null
        val hunter = game.observeProfile().first()

        val reward = XpEngine.sessionXp(
            completedSets = summary.completedSets,
            volumeKg = summary.volumeKg,
            durationMinutes = summary.durationMinutes,
            personalRecords = summary.personalRecords,
            streakDays = hunter.currentStreak
        )

        val xpResult = game.awardXp(reward.total, "Workout: ${summary.completedSets} sets")
        workouts.recordSessionXp(sessionId, reward.total)

        game.setQuestProgress(QuestMetric.WORKOUT_SETS, summary.completedSets.toDouble())
        game.setQuestProgress(QuestMetric.WORKOUT_MINUTES, summary.durationMinutes.toDouble())
        game.setQuestProgress(QuestMetric.VOLUME_KG, summary.volumeKg)

        val completed = workouts.observeCompletedCount().first()
        val history = workouts.observeHistory().first()
        val achievements = game.evaluateAchievements(
            completedWorkouts = completed,
            totalVolumeKg = history.sumOf { it.totalVolumeKg },
            streak = workouts.trainingStreak(),
            prCount = workouts.observePersonalRecords().first().size,
            loggedFoodDays = 0
        )

        recalculateAttributes()

        return FinishOutcome(
            summary = summary,
            xp = reward,
            levelsGained = xpResult.levelsGained,
            pointsGained = xpResult.pointsGained,
            newAchievements = achievements
        )
    }

    /** Pulls together every input the attribute engine needs. */
    suspend fun recalculateAttributes() {
        val profile = prefs.profile.first()
        val heat = workouts.observeHeatmap(7).first()
        val weeklySets = heat.values.sumOf { it.effectiveSets }
        val weeklyVolume = heat.values.sumOf { it.volumeKg }

        val prs = workouts.observePersonalRecords().first()
        val bestTotal = prs.sortedByDescending { it.bestEstimated1RmKg }.take(3).sumOf { it.bestEstimated1RmKg }
        val relativeStrength = StrengthMath.relativeStrengthScore(bestTotal, profile.weightKg)

        val (avgSteps, avgSleep, restingHr) = wearables.weeklyAverages()
        val activeCalories = wearables.weeklyActiveCalories()

        val cardioMinutes = workouts.observeHistory().first()
            .filter { it.dateEpochDay >= Dates.today() - 6 }
            .sumOf { session ->
                session.finishedAt?.let { ((it - session.startedAt) / 60_000) } ?: 0L
            }.toDouble()

        val nutritionDays = nutrition.observeDailyTotals(Dates.today() - 6, Dates.today()).first()
        val proteinTarget = profile.macroTarget.proteinG

        game.recalculateAttributes(
            AttributeEngine.Inputs(
                weeklyEffectiveSets = weeklySets,
                weeklyVolumeKg = weeklyVolume,
                bestRelativeStrength = relativeStrength,
                avgDailySteps = avgSteps,
                weeklyCardioMinutes = cardioMinutes,
                weeklyActiveCalories = activeCalories,
                nutritionLoggedDays = nutritionDays.count { it.calories > 0 },
                proteinTargetHitDays = nutritionDays.count { it.protein >= proteinTarget * 0.9 },
                avgSleepMinutes = avgSleep,
                restingHeartRate = restingHr
            )
        )
    }

    /** Syncs the wearable, then folds the results into quests and attributes. */
    suspend fun syncWearable(): WearableRepository.SyncResult {
        val profile = prefs.profile.first()
        val result = wearables.sync(profile.wearableSource)
        if (result.status == WearableRepository.SyncStatus.SUCCESS) {
            pushAllQuestProgress()
            recalculateAttributes()
        }
        return result
    }

    /** The dashboard's "what should I do about it" line under the heat map. */
    suspend fun weakestMuscleAdvice(): String {
        val heat = workouts.observeHeatmap(prefs.profile.first().heatmapWindowDays).first()
        val weakest = HeatmapEngine.weakestLinks(heat, 2)
        if (weakest.isEmpty()) return "No training logged yet — the map fills in as you work."
        val names = weakest.joinToString(" and ") { it.muscle.displayName.lowercase() }
        return "Your $names are the coldest regions this week. Put them first in your next session."
    }

    /** Progress text for the status window header. */
    suspend fun levelSummary(): String {
        val hunter = game.observeProfile().first()
        val next = LevelCurve.totalXpForLevel(hunter.level + 1) - hunter.totalXp
        return "$next XP to level ${hunter.level + 1}"
    }
}
