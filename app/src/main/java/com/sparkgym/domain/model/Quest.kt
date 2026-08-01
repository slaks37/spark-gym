package com.sparkgym.domain.model

/**
 * Daily quests are the Solo Leveling loop: a fixed set of targets that reset at
 * midnight, reward XP on completion, and apply a penalty when the day is missed.
 */
enum class QuestMetric(val displayName: String, val unit: String) {
    STEPS("Steps", "steps"),
    WORKOUT_SETS("Working sets", "sets"),
    WORKOUT_MINUTES("Training time", "min"),
    VOLUME_KG("Training volume", "kg"),
    PROTEIN_G("Protein", "g"),
    CALORIES_LOGGED("Meals logged", "entries"),
    WATER_ML("Water", "ml"),
    ACTIVE_CALORIES("Active calories", "kcal"),
    SLEEP_MINUTES("Sleep", "min"),
    PUSHUPS("Push-ups", "reps"),
    SITUPS("Sit-ups", "reps"),
    SQUATS("Bodyweight squats", "reps"),
    RUN_METERS("Running", "m")
}

/** How a quest's progress is obtained. */
enum class QuestSource {
    /** Filled from workout logs, nutrition entries or wearable sync. */
    AUTOMATIC,

    /** The user taps + to count reps (the classic 100 push-ups quest). */
    MANUAL
}

data class DailyQuest(
    val id: Long,
    val metric: QuestMetric,
    val target: Double,
    val progress: Double,
    val xpReward: Int,
    val source: QuestSource,
    val isCore: Boolean
) {
    val completed: Boolean get() = progress >= target
    val ratio: Float get() = if (target <= 0) 1f else (progress / target).toFloat().coerceIn(0f, 1f)
}

/**
 * The default daily set, mirroring the original "daily quest" from the show but
 * scaled to whatever the user can actually do (see QuestEngine).
 */
data class QuestTemplate(
    val metric: QuestMetric,
    val baseTarget: Double,
    val xpReward: Int,
    val source: QuestSource,
    val isCore: Boolean
)

/** Achievements are permanent; quests are daily. */
data class Achievement(
    val key: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val unlockedAt: Long?
) {
    val unlocked: Boolean get() = unlockedAt != null
}
