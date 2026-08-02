package com.sparkgym.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        RoutineEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        SetLogEntity::class,
        PersonalRecordEntity::class,
        FoodEntity::class,
        DiaryEntryEntity::class,
        WaterLogEntity::class,
        BodyMetricEntity::class,
        WearableDayEntity::class,
        HunterStateEntity::class,
        QuestEntity::class,
        AchievementEntity::class,
        XpEventEntity::class,
        RoutineFolderEntity::class,
        ProgressPhotoEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SparkGymDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun wearableDao(): WearableDao
    abstract fun gameDao(): GameDao
    abstract fun progressPhotoDao(): ProgressPhotoDao

    companion object {
        private const val NAME = "spark_gym.db"

        @Volatile private var instance: SparkGymDatabase? = null

        fun get(context: Context): SparkGymDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SparkGymDatabase::class.java,
                    NAME
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
