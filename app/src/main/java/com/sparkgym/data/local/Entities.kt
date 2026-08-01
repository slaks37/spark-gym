package com.sparkgym.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.ExerciseDifficulty
import com.sparkgym.domain.model.ExerciseForce
import com.sparkgym.domain.model.HunterClass
import com.sparkgym.domain.model.QuestMetric
import com.sparkgym.domain.model.QuestSource
import com.sparkgym.domain.model.TrackingType

// ---------------------------------------------------------------------------
// Exercise library
// ---------------------------------------------------------------------------

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["slug"], unique = true), Index("name")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Stable identifier for seeded exercises so re-seeding never duplicates. */
    val slug: String,
    val name: String,
    val equipment: Equipment,
    val force: ExerciseForce,
    val difficulty: ExerciseDifficulty,
    val tracking: TrackingType,
    val instructions: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    /** Used to build a "how to" search when no bundled clip exists. */
    val mediaQuery: String = ""
)

/**
 * Exercise → muscle contributions, as a real join table rather than a CSV column,
 * so the heat map is a single GROUP BY instead of an in-memory fan-out.
 * Contribution is 1.0 for a prime mover and 0.5 for a synergist.
 */
@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exerciseId", "muscle"],
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("exerciseId"), Index("muscle")]
)
data class ExerciseMuscleEntity(
    val exerciseId: Long,
    val muscle: String,
    val contribution: Float
)

// ---------------------------------------------------------------------------
// Routines (programs → days → prescribed exercises)
// ---------------------------------------------------------------------------

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String? = null,
    val name: String,
    val description: String,
    val goal: String,
    val daysPerWeek: Int,
    val level: String,
    val homeFriendly: Boolean,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_days",
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("routineId")]
)
data class RoutineDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val dayIndex: Int,
    val name: String,
    val focus: String
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId"), Index("exerciseId")]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int,
    val repsMin: Int,
    val repsMax: Int,
    val restSeconds: Int,
    val notes: String = ""
)

// ---------------------------------------------------------------------------
// Logged training
// ---------------------------------------------------------------------------

@Entity(tableName = "workout_sessions", indices = [Index("startedAt"), Index("dateEpochDay")])
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineDayId: Long? = null,
    val name: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val dateEpochDay: Long,
    val notes: String = "",
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0,
    val xpAwarded: Int = 0,
    /** Snapshot so bodyweight-exercise volume stays historically accurate. */
    val bodyweightKg: Double = 70.0
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val setNumber: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val durationSeconds: Int = 0,
    val distanceMeters: Double = 0.0,
    val rpe: Double? = null,
    val isWarmup: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    /** Set when this beat the previous best estimated 1RM for the exercise. */
    val isPersonalRecord: Boolean = false
)

@Entity(tableName = "personal_records", indices = [Index(value = ["exerciseId"], unique = true)])
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val bestEstimated1RmKg: Double,
    val bestWeightKg: Double,
    val bestReps: Int,
    val bestVolumeKg: Double,
    val achievedAt: Long
)

// ---------------------------------------------------------------------------
// Nutrition
// ---------------------------------------------------------------------------

@Entity(tableName = "foods", indices = [Index("name"), Index("barcode")])
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String? = null,
    val name: String,
    val brand: String = "",
    /** All macros are stored per 100 g / 100 ml so scaling is trivial. */
    val caloriesPer100: Double,
    val proteinPer100: Double,
    val carbsPer100: Double,
    val fatPer100: Double,
    val fiberPer100: Double = 0.0,
    val sugarPer100: Double = 0.0,
    val sodiumMgPer100: Double = 0.0,
    /** Default portion, e.g. "1 medium (120 g)". */
    val servingLabel: String = "100 g",
    val servingGrams: Double = 100.0,
    val barcode: String? = null,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val source: String = "seed"
)

@Entity(
    tableName = "diary_entries",
    indices = [Index("dateEpochDay"), Index("foodId")]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val meal: String,
    val foodId: Long?,
    /** Denormalised snapshot: editing a food later must not rewrite history. */
    val name: String,
    val brand: String,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val milliliters: Int
)

@Entity(tableName = "body_metrics", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val weightKg: Double,
    val bodyFatPercent: Double? = null,
    val waistCm: Double? = null,
    val notes: String = ""
)

// ---------------------------------------------------------------------------
// Wearable sync (Fitbit / Health Connect)
// ---------------------------------------------------------------------------

@Entity(tableName = "wearable_days", indices = [Index(value = ["dateEpochDay"], unique = true)])
data class WearableDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val steps: Int = 0,
    val distanceMeters: Double = 0.0,
    val activeCalories: Int = 0,
    val totalCalories: Int = 0,
    val restingHeartRate: Int? = null,
    val sleepMinutes: Int = 0,
    val activeMinutes: Int = 0,
    val source: String = "none",
    val syncedAt: Long = System.currentTimeMillis()
)

// ---------------------------------------------------------------------------
// Game state
// ---------------------------------------------------------------------------

@Entity(tableName = "hunter_state")
data class HunterStateEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Hunter",
    val totalXp: Long = 0,
    val unspentPoints: Int = 0,
    val strength: Int = 10,
    val vitality: Int = 10,
    val agility: Int = 10,
    val endurance: Int = 10,
    val intellect: Int = 10,
    val perception: Int = 10,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastQuestDay: Long = 0,
    val hunterClass: HunterClass = HunterClass.AWAKENED,
    val title: String? = null,
    /** Penalty state: how many consecutive days of quests were missed. */
    val penaltyDays: Int = 0
) {
    fun attribute(attr: Attribute): Int = when (attr) {
        Attribute.STRENGTH -> strength
        Attribute.VITALITY -> vitality
        Attribute.AGILITY -> agility
        Attribute.ENDURANCE -> endurance
        Attribute.INTELLECT -> intellect
        Attribute.PERCEPTION -> perception
    }

    fun withAttribute(attr: Attribute, value: Int): HunterStateEntity = when (attr) {
        Attribute.STRENGTH -> copy(strength = value)
        Attribute.VITALITY -> copy(vitality = value)
        Attribute.AGILITY -> copy(agility = value)
        Attribute.ENDURANCE -> copy(endurance = value)
        Attribute.INTELLECT -> copy(intellect = value)
        Attribute.PERCEPTION -> copy(perception = value)
    }
}

@Entity(
    tableName = "quests",
    indices = [Index(value = ["dateEpochDay", "metric"], unique = true)]
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val metric: QuestMetric,
    val target: Double,
    val progress: Double = 0.0,
    val xpReward: Int,
    val source: QuestSource,
    val isCore: Boolean,
    val claimedAt: Long? = null
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val key: String,
    val unlockedAt: Long
)

@Entity(tableName = "xp_events", indices = [Index("createdAt")])
data class XpEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)
