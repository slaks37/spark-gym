package com.sparkgym.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sparkgym.core.util.AppLanguage
import com.sparkgym.domain.engine.EnergyMath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "spark_gym_prefs")

/** Everything the user tells us about themselves, plus app-level settings. */
data class UserProfile(
    val name: String = "Hunter",
    val sex: EnergyMath.Sex = EnergyMath.Sex.MALE,
    val age: Int = 25,
    val heightCm: Double = 172.0,
    val weightKg: Double = 70.0,
    val activity: EnergyMath.ActivityLevel = EnergyMath.ActivityLevel.MODERATE,
    val goal: EnergyMath.Goal = EnergyMath.Goal.MAINTAIN,
    val useMetric: Boolean = true,
    val defaultRestSeconds: Int = 90,
    val calorieOverride: Int? = null,
    val proteinOverride: Int? = null,
    val onboarded: Boolean = false,
    val activeRoutineId: Long? = null,
    val heatmapWindowDays: Int = 7,
    val wearableSource: String = WearableSource.NONE,
    val language: AppLanguage = AppLanguage.EN,
    /** content:// URI of the user's chosen photo, null when unset. */
    val avatarUri: String? = null,
    val avatarPath: String? = null
) {
    val tdee: Double get() = EnergyMath.tdee(sex, weightKg, heightCm, age, activity)

    val macroTarget: EnergyMath.MacroTarget
        get() {
            val computed = EnergyMath.macroTarget(tdee, goal, weightKg)
            return computed.copy(
                calories = calorieOverride ?: computed.calories,
                proteinG = proteinOverride ?: computed.proteinG
            )
        }
}

object WearableSource {
    const val NONE = "none"
    const val HEALTH_CONNECT = "health_connect"
    const val FITBIT = "fitbit"
}

class UserPrefs(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("name")
        val SEX = stringPreferencesKey("sex")
        val AGE = intPreferencesKey("age")
        val HEIGHT = doublePreferencesKey("height_cm")
        val WEIGHT = doublePreferencesKey("weight_kg")
        val ACTIVITY = stringPreferencesKey("activity")
        val GOAL = stringPreferencesKey("goal")
        val METRIC = booleanPreferencesKey("use_metric")
        val REST = intPreferencesKey("default_rest")
        val CAL_OVERRIDE = intPreferencesKey("calorie_override")
        val PROTEIN_OVERRIDE = intPreferencesKey("protein_override")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val ACTIVE_ROUTINE = longPreferencesKey("active_routine")
        val HEATMAP_WINDOW = intPreferencesKey("heatmap_window")
        val WEARABLE = stringPreferencesKey("wearable_source")
        val AVATAR = stringPreferencesKey("avatar_uri")
        val LAST_SYNC_DAY = longPreferencesKey("last_sync_day")
        val LANGUAGE = stringPreferencesKey("language")
        val AVATAR_PATH = stringPreferencesKey("avatar_path")
    }

    val profile: Flow<UserProfile> = context.dataStore.data.map { it.toProfile() }

    private fun Preferences.toProfile() = UserProfile(
        name = this[Keys.NAME] ?: "Hunter",
        sex = this[Keys.SEX]?.let { runCatching { EnergyMath.Sex.valueOf(it) }.getOrNull() } ?: EnergyMath.Sex.MALE,
        age = this[Keys.AGE] ?: 25,
        heightCm = this[Keys.HEIGHT] ?: 172.0,
        weightKg = this[Keys.WEIGHT] ?: 70.0,
        activity = this[Keys.ACTIVITY]?.let { runCatching { EnergyMath.ActivityLevel.valueOf(it) }.getOrNull() }
            ?: EnergyMath.ActivityLevel.MODERATE,
        goal = this[Keys.GOAL]?.let { runCatching { EnergyMath.Goal.valueOf(it) }.getOrNull() }
            ?: EnergyMath.Goal.MAINTAIN,
        useMetric = this[Keys.METRIC] ?: true,
        defaultRestSeconds = this[Keys.REST] ?: 90,
        calorieOverride = this[Keys.CAL_OVERRIDE],
        proteinOverride = this[Keys.PROTEIN_OVERRIDE],
        onboarded = this[Keys.ONBOARDED] ?: false,
        activeRoutineId = this[Keys.ACTIVE_ROUTINE],
        heatmapWindowDays = this[Keys.HEATMAP_WINDOW] ?: 7,
        wearableSource = this[Keys.WEARABLE] ?: WearableSource.NONE,
        avatarUri = this[Keys.AVATAR],
        language = this[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.EN,
        avatarPath = this[Keys.AVATAR_PATH]
    )

    suspend fun update(transform: (UserProfile) -> UserProfile) {
        context.dataStore.edit { prefs ->
            val updated = transform(prefs.toProfile())
            prefs[Keys.NAME] = updated.name
            prefs[Keys.SEX] = updated.sex.name
            prefs[Keys.AGE] = updated.age
            prefs[Keys.HEIGHT] = updated.heightCm
            prefs[Keys.WEIGHT] = updated.weightKg
            prefs[Keys.ACTIVITY] = updated.activity.name
            prefs[Keys.GOAL] = updated.goal.name
            prefs[Keys.METRIC] = updated.useMetric
            prefs[Keys.REST] = updated.defaultRestSeconds
            prefs[Keys.ONBOARDED] = updated.onboarded
            prefs[Keys.HEATMAP_WINDOW] = updated.heatmapWindowDays
            prefs[Keys.WEARABLE] = updated.wearableSource
            prefs[Keys.LANGUAGE] = updated.language.name
            updated.avatarUri?.let { prefs[Keys.AVATAR] = it } ?: prefs.remove(Keys.AVATAR)
            updated.calorieOverride?.let { prefs[Keys.CAL_OVERRIDE] = it } ?: prefs.remove(Keys.CAL_OVERRIDE)
            updated.proteinOverride?.let { prefs[Keys.PROTEIN_OVERRIDE] = it } ?: prefs.remove(Keys.PROTEIN_OVERRIDE)
            updated.activeRoutineId?.let { prefs[Keys.ACTIVE_ROUTINE] = it } ?: prefs.remove(Keys.ACTIVE_ROUTINE)
            updated.avatarPath?.let { prefs[Keys.AVATAR_PATH] = it } ?: prefs.remove(Keys.AVATAR_PATH)
        }
    }

    val lastSyncDay: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_SYNC_DAY] ?: 0L }

    suspend fun setLastSyncDay(day: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNC_DAY] = day }
    }
}
