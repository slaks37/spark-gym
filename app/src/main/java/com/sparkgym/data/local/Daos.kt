package com.sparkgym.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos ORDER BY dateEpochDay DESC")
    fun observePhotos(): Flow<List<ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhotoEntity): Long

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun deletePhoto(id: Long)
}

@Dao
interface ExerciseDao {

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscles(links: List<ExerciseMuscleEntity>)

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun byId(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE slug = :slug LIMIT 1")
    suspend fun bySlug(slug: String): ExerciseEntity?

    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeWithMuscles(id: Long): Flow<ExerciseWithMuscles?>

    @Transaction
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeAllWithMuscles(): Flow<List<ExerciseWithMuscles>>

    @Query("UPDATE exercises SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("SELECT muscle FROM exercise_muscles WHERE exerciseId = :exerciseId")
    suspend fun musclesFor(exerciseId: Long): List<String>

    @Query("DELETE FROM exercises WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustom(id: Long)
}

@Dao
interface RoutineDao {

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun count(): Int

    @Query("SELECT * FROM routine_folders ORDER BY name")
    fun observeFolders(): Flow<List<RoutineFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: RoutineFolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: RoutineDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptions(items: List<RoutineExerciseEntity>)

    @Query("SELECT * FROM routines ORDER BY isCustom DESC, name")
    fun observeRoutines(): Flow<List<RoutineEntity>>

    /** The gym tab: everything except the bodyweight circuits, which have their own screen. */
    @Query("SELECT * FROM routines WHERE goal != :goal ORDER BY isCustom DESC, name")
    fun observeRoutinesExcludingGoal(goal: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE goal = :goal ORDER BY name")
    fun observeRoutinesByGoal(goal: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE goal = :goal")
    suspend fun routinesByGoal(goal: String): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE slug = :slug LIMIT 1")
    suspend fun routineBySlug(slug: String): RoutineEntity?

    @Query("SELECT * FROM routine_days WHERE routineId = :routineId ORDER BY dayIndex")
    suspend fun daysFor(routineId: Long): List<RoutineDayEntity>

    /** Circuits are re-prescribed whenever the level changes, so the old rows go first. */
    @Query("DELETE FROM routine_exercises WHERE dayId = :dayId")
    suspend fun deletePrescriptions(dayId: Long)

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    fun observeRoutine(id: Long): Flow<RoutineWithDays?>

    @Query("SELECT * FROM routine_days WHERE routineId = :routineId ORDER BY dayIndex")
    fun observeDays(routineId: Long): Flow<List<RoutineDayEntity>>

    @Query("SELECT * FROM routine_days WHERE id = :dayId")
    suspend fun day(dayId: Long): RoutineDayEntity?

    @Transaction
    @Query("SELECT * FROM routine_exercises WHERE dayId = :dayId ORDER BY orderIndex")
    fun observePrescribed(dayId: Long): Flow<List<PrescribedExercise>>

    @Transaction
    @Query("SELECT * FROM routine_exercises WHERE dayId = :dayId ORDER BY orderIndex")
    suspend fun prescribed(dayId: Long): List<PrescribedExercise>

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)
}

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun activeSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun session(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSession(id: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC LIMIT :limit")
    fun observeHistory(limit: Int = 100): Flow<List<WorkoutSessionEntity>>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Insert
    suspend fun insertSet(set: SetLogEntity): Long

    @Insert
    suspend fun insertSets(sets: List<SetLogEntity>)

    @Update
    suspend fun updateSet(set: SetLogEntity)

    @Query("DELETE FROM set_logs WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY orderIndex, setNumber")
    fun observeSets(sessionId: Long): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY orderIndex, setNumber")
    suspend fun sets(sessionId: Long): List<SetLogEntity>

    /**
     * Last completed sets for an exercise, so the logger can pre-fill
     * "what you did last time" the way every good gym log does.
     */
    @Query(
        """
        SELECT s.* FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        WHERE s.exerciseId = :exerciseId AND s.isCompleted = 1 AND s.setType != 'WARMUP'
          AND w.finishedAt IS NOT NULL
        ORDER BY w.startedAt DESC, s.setNumber ASC
        LIMIT :limit
        """
    )
    suspend fun recentSetsFor(exerciseId: Long, limit: Int = 8): List<SetLogEntity>

    @Query(
        """
        SELECT e.id AS exerciseId, e.name AS exerciseName,
               COUNT(s.id) AS setCount,
               MAX(s.weightKg) AS topWeightKg,
               MAX(s.reps) AS topReps,
               COALESCE(SUM(s.weightKg * s.reps), 0) AS volumeKg
        FROM set_logs s
        JOIN exercises e ON e.id = s.exerciseId
        WHERE s.sessionId = :sessionId AND s.isCompleted = 1
        GROUP BY e.id, e.name
        ORDER BY MIN(s.orderIndex)
        """
    )
    fun observeSessionBreakdown(sessionId: Long): Flow<List<SessionExerciseRow>>

    /**
     * The heat map, in one query: every completed working set contributes
     * `contribution` effective sets to each muscle it trains.
     */
    @Query(
        """
        SELECT em.muscle AS muscle,
               SUM(em.contribution) AS effectiveSets,
               COALESCE(SUM(s.weightKg * s.reps * em.contribution), 0) AS volumeKg
        FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        JOIN exercise_muscles em ON em.exerciseId = s.exerciseId
        WHERE s.isCompleted = 1 AND s.setType != 'WARMUP'
          AND w.dateEpochDay BETWEEN :fromDay AND :toDay
        GROUP BY em.muscle
        """
    )
    fun observeMuscleVolume(fromDay: Long, toDay: Long): Flow<List<MuscleVolumeRow>>

    @Query(
        """
        SELECT w.dateEpochDay AS dateEpochDay,
               COALESCE(SUM(s.weightKg * s.reps), 0) AS volumeKg,
               COUNT(s.id) AS sets
        FROM workout_sessions w
        LEFT JOIN set_logs s ON s.sessionId = w.id AND s.isCompleted = 1 AND s.setType != 'WARMUP'
        WHERE w.dateEpochDay BETWEEN :fromDay AND :toDay AND w.finishedAt IS NOT NULL
        GROUP BY w.dateEpochDay
        ORDER BY w.dateEpochDay
        """
    )
    fun observeVolumeTrend(fromDay: Long, toDay: Long): Flow<List<VolumePointRow>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE finishedAt IS NOT NULL")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT DISTINCT dateEpochDay FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY dateEpochDay DESC LIMIT :limit")
    suspend fun recentTrainingDays(limit: Int = 400): List<Long>

    /**
     * Best working set per session for one exercise, oldest first — the series
     * the plateau detector runs over.
     */
    @Query(
        """
        SELECT w.dateEpochDay AS dateEpochDay,
               MAX(s.weightKg * (1 + s.reps / 30.0)) AS volumeKg,
               COUNT(s.id) AS sets
        FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        WHERE s.exerciseId = :exerciseId AND s.isCompleted = 1 AND s.setType != 'WARMUP'
          AND s.weightKg > 0 AND s.reps > 0 AND w.finishedAt IS NOT NULL
        GROUP BY w.id
        ORDER BY w.startedAt ASC
        LIMIT :limit
        """
    )
    suspend fun estimatedMaxHistory(exerciseId: Long, limit: Int = 12): List<VolumePointRow>

    @Query(
        """
        SELECT w.dateEpochDay AS dateEpochDay,
               COALESCE(SUM(s.weightKg * s.reps), 0) AS volumeKg,
               COUNT(s.id) AS sets
        FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        WHERE s.exerciseId = :exerciseId AND s.isCompleted = 1 AND s.setType != 'WARMUP'
          AND w.finishedAt IS NOT NULL
        GROUP BY w.id
        ORDER BY w.startedAt ASC
        LIMIT :limit
        """
    )
    suspend fun volumePoints(exerciseId: Long, limit: Int = 20): List<VolumePointRow>

    // We use a flat class to join the session data we need.
    @Query(
        """
        SELECT w.id AS sessionId,
               w.dateEpochDay AS dateEpochDay,
               w.name AS sessionName,
               s.setNumber AS setNumber,
               s.weightKg AS weightKg,
               s.reps AS reps,
               s.setType AS setType,
               s.isPersonalRecord AS isPersonalRecord
        FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        WHERE s.exerciseId = :exerciseId AND s.isCompleted = 1
          AND w.finishedAt IS NOT NULL
        ORDER BY w.startedAt DESC, s.setNumber ASC
        LIMIT :limit
        """
    )
    suspend fun exerciseHistorySets(exerciseId: Long, limit: Int = 200): List<com.sparkgym.domain.model.ExerciseHistoryRow>

    @Query(
        """
        SELECT s.exerciseId FROM set_logs s
        JOIN workout_sessions w ON w.id = s.sessionId
        WHERE s.isCompleted = 1 AND s.setType != 'WARMUP' AND s.weightKg > 0
          AND w.finishedAt IS NOT NULL
        GROUP BY s.exerciseId
        HAVING COUNT(DISTINCT w.id) >= 4
        ORDER BY COUNT(s.id) DESC
        LIMIT :limit
        """
    )
    suspend fun mostTrainedExerciseIds(limit: Int = 8): List<Long>

    @Upsert
    suspend fun upsertPr(pr: PersonalRecordEntity)

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun pr(exerciseId: Long): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records")
    fun observePrs(): Flow<List<PersonalRecordEntity>>
}

@Dao
interface NutritionDao {

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun foodCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun food(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE barcode = :barcode LIMIT 1")
    suspend fun foodByBarcode(barcode: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE slug = :slug LIMIT 1")
    suspend fun foodBySlug(slug: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE slug IN (:slugs)")
    suspend fun foodsBySlugs(slugs: List<String>): List<FoodEntity>

    @Query(
        """
        SELECT * FROM foods
        WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'
        ORDER BY isFavorite DESC, LENGTH(name), name
        LIMIT 60
        """
    )
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY isFavorite DESC, name LIMIT 60")
    fun observeTopFoods(): Flow<List<FoodEntity>>

    @Query("UPDATE foods SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFoodFavorite(id: Long, favorite: Boolean)

    @Insert
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("SELECT * FROM diary_entries WHERE dateEpochDay = :day ORDER BY loggedAt")
    fun observeDiary(day: Long): Flow<List<DiaryEntryEntity>>

    @Query(
        """
        SELECT dateEpochDay AS dateEpochDay,
               COALESCE(SUM(calories), 0) AS calories,
               COALESCE(SUM(protein), 0) AS protein,
               COALESCE(SUM(carbs), 0) AS carbs,
               COALESCE(SUM(fat), 0) AS fat
        FROM diary_entries
        WHERE dateEpochDay BETWEEN :fromDay AND :toDay
        GROUP BY dateEpochDay
        ORDER BY dateEpochDay
        """
    )
    fun observeDailyTotals(fromDay: Long, toDay: Long): Flow<List<DailyTotalsRow>>

    @Query("SELECT COALESCE(SUM(protein), 0) FROM diary_entries WHERE dateEpochDay = :day")
    suspend fun proteinOn(day: Long): Double

    @Query("SELECT COUNT(*) FROM diary_entries WHERE dateEpochDay = :day")
    suspend fun entryCountOn(day: Long): Int

    @Query("SELECT COUNT(DISTINCT dateEpochDay) FROM diary_entries WHERE dateEpochDay BETWEEN :fromDay AND :toDay")
    suspend fun loggedDayCount(fromDay: Long, toDay: Long): Int

    @Query("SELECT * FROM water_logs WHERE dateEpochDay = :day")
    fun observeWater(day: Long): Flow<WaterLogEntity?>

    @Query("SELECT * FROM water_logs WHERE dateEpochDay = :day")
    suspend fun water(day: Long): WaterLogEntity?

    @Upsert
    suspend fun upsertWater(log: WaterLogEntity)

    @Upsert
    suspend fun upsertBodyMetric(metric: BodyMetricEntity)

    @Query("SELECT * FROM body_metrics ORDER BY dateEpochDay DESC LIMIT :limit")
    fun observeBodyMetrics(limit: Int = 90): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics ORDER BY dateEpochDay DESC LIMIT 1")
    suspend fun latestBodyMetric(): BodyMetricEntity?

    @Query("SELECT * FROM body_metrics WHERE dateEpochDay = :day")
    suspend fun bodyMetric(day: Long): BodyMetricEntity?
}

@Dao
interface WearableDao {

    @Upsert
    suspend fun upsert(day: WearableDayEntity)

    @Query("SELECT * FROM wearable_days WHERE dateEpochDay = :day")
    fun observeDay(day: Long): Flow<WearableDayEntity?>

    @Query("SELECT * FROM wearable_days WHERE dateEpochDay = :day")
    suspend fun day(day: Long): WearableDayEntity?

    @Query("SELECT * FROM wearable_days WHERE dateEpochDay BETWEEN :fromDay AND :toDay ORDER BY dateEpochDay")
    fun observeRange(fromDay: Long, toDay: Long): Flow<List<WearableDayEntity>>

    @Query("SELECT * FROM wearable_days ORDER BY dateEpochDay DESC LIMIT 1")
    suspend fun latest(): WearableDayEntity?
}

@Dao
interface GameDao {

    @Query("SELECT * FROM hunter_state WHERE id = 1")
    fun observeState(): Flow<HunterStateEntity?>

    @Query("SELECT * FROM hunter_state WHERE id = 1")
    suspend fun state(): HunterStateEntity?

    @Upsert
    suspend fun upsertState(state: HunterStateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("SELECT * FROM quests WHERE dateEpochDay = :day ORDER BY isCore DESC, id")
    fun observeQuests(day: Long): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE dateEpochDay = :day ORDER BY isCore DESC, id")
    suspend fun quests(day: Long): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE dateEpochDay = :day AND metric = :metric LIMIT 1")
    suspend fun quest(day: Long, metric: String): QuestEntity?

    @Query("UPDATE quests SET progress = :progress WHERE dateEpochDay = :day AND metric = :metric")
    suspend fun setProgress(day: Long, metric: String, progress: Double)

    @Query("SELECT COUNT(*) FROM quests WHERE dateEpochDay = :day")
    suspend fun questCount(day: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE `key` = :key")
    suspend fun achievement(key: String): AchievementEntity?

    @Insert
    suspend fun insertXpEvent(event: XpEventEntity)

    @Query("SELECT * FROM xp_events ORDER BY createdAt DESC LIMIT :limit")
    fun observeXpEvents(limit: Int = 40): Flow<List<XpEventEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_events WHERE createdAt >= :since")
    suspend fun xpSince(since: Long): Int
}
