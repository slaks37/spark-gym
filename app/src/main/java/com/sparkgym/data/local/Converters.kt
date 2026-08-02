package com.sparkgym.data.local

import androidx.room.TypeConverter
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.ExerciseDifficulty
import com.sparkgym.domain.model.ExerciseForce
import com.sparkgym.domain.model.HunterClass
import com.sparkgym.domain.model.QuestMetric
import com.sparkgym.domain.model.QuestSource
import com.sparkgym.domain.model.SetType
import com.sparkgym.domain.model.TrackingType

/**
 * Enums are persisted by name, not ordinal — reordering an enum must never
 * silently rewrite everybody's history.
 */
class Converters {
    @TypeConverter fun equipmentToString(v: Equipment): String = v.name
    @TypeConverter fun stringToEquipment(v: String): Equipment =
        runCatching { Equipment.valueOf(v) }.getOrDefault(Equipment.OTHER)

    @TypeConverter fun forceToString(v: ExerciseForce): String = v.name
    @TypeConverter fun stringToForce(v: String): ExerciseForce =
        runCatching { ExerciseForce.valueOf(v) }.getOrDefault(ExerciseForce.STATIC)

    @TypeConverter fun difficultyToString(v: ExerciseDifficulty): String = v.name
    @TypeConverter fun stringToDifficulty(v: String): ExerciseDifficulty =
        runCatching { ExerciseDifficulty.valueOf(v) }.getOrDefault(ExerciseDifficulty.BEGINNER)

    @TypeConverter fun trackingToString(v: TrackingType): String = v.name
    @TypeConverter fun stringToTracking(v: String): TrackingType =
        runCatching { TrackingType.valueOf(v) }.getOrDefault(TrackingType.WEIGHT_REPS)

    @TypeConverter fun questMetricToString(v: QuestMetric): String = v.name
    @TypeConverter fun stringToQuestMetric(v: String): QuestMetric =
        runCatching { QuestMetric.valueOf(v) }.getOrDefault(QuestMetric.STEPS)

    @TypeConverter fun questSourceToString(v: QuestSource): String = v.name
    @TypeConverter fun stringToQuestSource(v: String): QuestSource =
        runCatching { QuestSource.valueOf(v) }.getOrDefault(QuestSource.MANUAL)

    @TypeConverter fun hunterClassToString(v: HunterClass): String = v.name
    @TypeConverter fun stringToHunterClass(v: String): HunterClass =
        runCatching { HunterClass.valueOf(v) }.getOrDefault(HunterClass.AWAKENED)

    @TypeConverter fun setTypeToString(v: SetType): String = v.name
    @TypeConverter fun stringToSetType(v: String): SetType =
        runCatching { SetType.valueOf(v) }.getOrDefault(SetType.NORMAL)
}
