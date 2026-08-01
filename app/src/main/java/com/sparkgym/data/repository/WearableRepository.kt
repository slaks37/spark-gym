package com.sparkgym.data.repository

import android.net.Uri
import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.local.WearableDayEntity
import com.sparkgym.data.prefs.UserPrefs
import com.sparkgym.data.prefs.WearableSource
import com.sparkgym.data.remote.FitbitApi
import com.sparkgym.data.remote.FitbitAuth
import com.sparkgym.data.remote.HealthConnectSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * One sync entry point over two very different sources.
 *
 * Health Connect is preferred when it is present and permitted: the Fitbit app
 * already writes into it, so the user links nothing. The Fitbit Web API is the
 * fallback for phones without Health Connect, and for users who want the data
 * even when the Fitbit app is not installed.
 */
class WearableRepository(
    private val db: SparkGymDatabase,
    private val prefs: UserPrefs,
    val fitbitAuth: FitbitAuth,
    private val fitbitApiProvider: () -> FitbitApi,
    val healthConnect: HealthConnectSource
) {

    private val dao get() = db.wearableDao()

    enum class SyncStatus { IDLE, SYNCING, SUCCESS, FAILED, NOT_CONFIGURED }

    data class SyncResult(
        val status: SyncStatus,
        val source: String,
        val daysSynced: Int = 0,
        val message: String? = null
    )

    fun observeToday(): Flow<WearableDayEntity?> = dao.observeDay(Dates.today())

    fun observeRange(days: Int): Flow<List<WearableDayEntity>> =
        dao.observeRange(Dates.today() - days + 1, Dates.today())

    fun observeSteps(): Flow<Int> = observeToday().map { it?.steps ?: 0 }

    suspend fun today(): WearableDayEntity? = dao.day(Dates.today())

    // -------------------------------------------------------------- linking

    fun beginFitbitLink(activityContext: android.content.Context) =
        fitbitAuth.beginAuthorization(activityContext)

    suspend fun completeFitbitLink(redirect: Uri): Result<Unit> {
        val result = fitbitAuth.completeAuthorization(redirect)
        if (result.isSuccess) {
            prefs.update { it.copy(wearableSource = WearableSource.FITBIT) }
        }
        return result
    }

    suspend fun unlinkFitbit() {
        fitbitAuth.unlink()
        prefs.update {
            it.copy(
                wearableSource = if (healthConnect.hasPermissions()) WearableSource.HEALTH_CONNECT
                else WearableSource.NONE
            )
        }
    }

    suspend fun useHealthConnect() {
        prefs.update { it.copy(wearableSource = WearableSource.HEALTH_CONNECT) }
    }

    /** Whichever source is usable right now, preferring what the user picked. */
    suspend fun activeSource(preferred: String): String = when {
        preferred == WearableSource.HEALTH_CONNECT && healthConnect.hasPermissions() ->
            WearableSource.HEALTH_CONNECT
        preferred == WearableSource.FITBIT && fitbitAuth.isLinked -> WearableSource.FITBIT
        healthConnect.hasPermissions() -> WearableSource.HEALTH_CONNECT
        fitbitAuth.isLinked -> WearableSource.FITBIT
        else -> WearableSource.NONE
    }

    // ----------------------------------------------------------------- sync

    suspend fun sync(preferredSource: String, daysBack: Int = 3): SyncResult {
        return when (activeSource(preferredSource)) {
            WearableSource.HEALTH_CONNECT -> syncHealthConnect(daysBack)
            WearableSource.FITBIT -> syncFitbit(daysBack)
            else -> SyncResult(
                SyncStatus.NOT_CONFIGURED,
                WearableSource.NONE,
                message = "Connect Fitbit or grant Health Connect access first"
            )
        }
    }

    private suspend fun syncHealthConnect(daysBack: Int): SyncResult {
        var synced = 0
        val today = LocalDate.now()
        return runCatching {
            for (offset in 0 until daysBack) {
                val date = today.minusDays(offset.toLong())
                val snapshot = healthConnect.readDay(date) ?: continue
                upsertDay(
                    epochDay = date.toEpochDay(),
                    steps = snapshot.steps,
                    distanceMeters = snapshot.distanceMeters,
                    activeCalories = snapshot.activeCalories,
                    totalCalories = snapshot.totalCalories,
                    restingHeartRate = snapshot.restingHeartRate,
                    sleepMinutes = snapshot.sleepMinutes,
                    activeMinutes = snapshot.exerciseMinutes,
                    source = WearableSource.HEALTH_CONNECT
                )
                synced++
            }
            prefs.setLastSyncDay(Dates.today())
            SyncResult(SyncStatus.SUCCESS, WearableSource.HEALTH_CONNECT, synced)
        }.getOrElse {
            SyncResult(SyncStatus.FAILED, WearableSource.HEALTH_CONNECT, synced, it.message)
        }
    }

    private suspend fun syncFitbit(daysBack: Int): SyncResult {
        if (!fitbitAuth.isConfigured) {
            return SyncResult(
                SyncStatus.NOT_CONFIGURED,
                WearableSource.FITBIT,
                message = "No Fitbit client id — add fitbit.clientId to local.properties"
            )
        }
        if (!fitbitAuth.isLinked) {
            return SyncResult(SyncStatus.NOT_CONFIGURED, WearableSource.FITBIT, message = "Fitbit not linked")
        }

        val api = fitbitApiProvider()
        var synced = 0
        val today = Dates.today()

        return runCatching {
            for (offset in 0 until daysBack) {
                val day = today - offset
                val date = Dates.apiDate(day)

                val activity = api.dailyActivity(date).summary
                // Heart rate and sleep are separate endpoints and either can 404
                // for a user who does not wear the band overnight.
                val restingHr = runCatching { api.heartRate(date).restingHeartRate }.getOrNull()
                    ?: activity.restingHeartRate
                val sleepMinutes = runCatching { api.sleep(date).summary.totalMinutesAsleep }.getOrDefault(0)

                upsertDay(
                    epochDay = day,
                    steps = activity.steps,
                    distanceMeters = activity.totalDistanceMeters,
                    activeCalories = activity.activityCalories,
                    totalCalories = activity.caloriesOut,
                    restingHeartRate = restingHr,
                    sleepMinutes = sleepMinutes,
                    activeMinutes = activity.activeMinutes,
                    source = WearableSource.FITBIT
                )
                synced++
            }
            prefs.setLastSyncDay(today)
            SyncResult(SyncStatus.SUCCESS, WearableSource.FITBIT, synced)
        }.getOrElse {
            SyncResult(SyncStatus.FAILED, WearableSource.FITBIT, synced, it.message)
        }
    }

    private suspend fun upsertDay(
        epochDay: Long,
        steps: Int,
        distanceMeters: Double,
        activeCalories: Int,
        totalCalories: Int,
        restingHeartRate: Int?,
        sleepMinutes: Int,
        activeMinutes: Int,
        source: String
    ) {
        // Carry the existing row id: @Upsert updates by primary key, not by the
        // unique date index, so a fresh entity would insert a duplicate.
        val existing = dao.day(epochDay)
        dao.upsert(
            WearableDayEntity(
                id = existing?.id ?: 0,
                dateEpochDay = epochDay,
                steps = steps,
                distanceMeters = distanceMeters,
                activeCalories = activeCalories,
                totalCalories = totalCalories,
                restingHeartRate = restingHeartRate ?: existing?.restingHeartRate,
                sleepMinutes = sleepMinutes,
                activeMinutes = activeMinutes,
                source = source
            )
        )
    }

    /** Averages used by the attribute engine. */
    suspend fun weeklyAverages(): Triple<Double, Double, Int?> {
        val from = Dates.today() - 6
        val days = mutableListOf<WearableDayEntity>()
        for (day in from..Dates.today()) dao.day(day)?.let { days += it }
        if (days.isEmpty()) return Triple(0.0, 0.0, null)
        val avgSteps = days.map { it.steps }.average()
        val avgSleep = days.filter { it.sleepMinutes > 0 }.map { it.sleepMinutes }.average()
            .takeIf { !it.isNaN() } ?: 0.0
        val restingHr = days.mapNotNull { it.restingHeartRate }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        return Triple(avgSteps, avgSleep, restingHr)
    }

    suspend fun weeklyActiveCalories(): Double {
        val from = Dates.today() - 6
        var total = 0.0
        for (day in from..Dates.today()) total += dao.day(day)?.activeCalories?.toDouble() ?: 0.0
        return total
    }
}
