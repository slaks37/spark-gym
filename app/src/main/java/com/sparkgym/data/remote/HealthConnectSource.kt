package com.sparkgym.data.remote

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

/**
 * Health Connect is the *other* way to get Fitbit data, and usually the better
 * one: the Fitbit app writes steps, heart rate, sleep and calories into it, so
 * the user links nothing and no token ever touches this app. It also picks up
 * Samsung Health, Wear OS, Garmin and anything else on the phone.
 */
class HealthConnectSource(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class)
    )

    enum class Availability { AVAILABLE, UPDATE_REQUIRED, NOT_SUPPORTED }

    fun availability(): Availability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> Availability.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.UPDATE_REQUIRED
        else -> Availability.NOT_SUPPORTED
    }

    private fun clientOrNull(): HealthConnectClient? =
        if (availability() == Availability.AVAILABLE) {
            runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
        } else null

    suspend fun hasPermissions(): Boolean {
        val client = clientOrNull() ?: return false
        val granted = runCatching { client.permissionController.getGrantedPermissions() }.getOrNull().orEmpty()
        // Reads are what matter; the write permissions are a nice-to-have.
        return granted.containsAll(
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
            )
        )
    }

    data class DaySnapshot(
        val steps: Int,
        val activeCalories: Int,
        val totalCalories: Int,
        val restingHeartRate: Int?,
        val sleepMinutes: Int,
        val exerciseMinutes: Int,
        val distanceMeters: Double = 0.0
    )

    suspend fun readDay(date: LocalDate): DaySnapshot? {
        val client = clientOrNull() ?: return null
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val range = TimeRangeFilter.between(start, end)

        val aggregate: AggregationResult? = runCatching {
            client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        HeartRateRecord.BPM_MIN
                    ),
                    timeRangeFilter = range
                )
            )
        }.getOrNull()

        val sleepMinutes = runCatching {
            client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, range))
                .records.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                .toInt()
        }.getOrDefault(0)

        val exerciseMinutes = runCatching {
            client.readRecords(ReadRecordsRequest(ExerciseSessionRecord::class, range))
                .records.sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                .toInt()
        }.getOrDefault(0)

        return DaySnapshot(
            steps = aggregate?.get(StepsRecord.COUNT_TOTAL)?.toInt() ?: 0,
            activeCalories = aggregate?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
                ?.inKilocalories?.toInt() ?: 0,
            totalCalories = aggregate?.get(TotalCaloriesBurnedRecord.ENERGY_TOTAL)
                ?.inKilocalories?.toInt() ?: 0,
            // Minimum bpm across the day is a reasonable proxy when the provider
            // does not expose a dedicated resting-heart-rate record.
            restingHeartRate = aggregate?.get(HeartRateRecord.BPM_MIN)?.toInt(),
            sleepMinutes = sleepMinutes,
            exerciseMinutes = exerciseMinutes
        )
    }

    /** Latest bodyweight the phone knows about, so the profile can stay current. */
    suspend fun latestWeightKg(): Double? {
        val client = clientOrNull() ?: return null
        val end = java.time.Instant.now()
        val start = end.minus(java.time.Duration.ofDays(60))
        return runCatching {
            client.readRecords(ReadRecordsRequest(WeightRecord::class, TimeRangeFilter.between(start, end)))
                .records.maxByOrNull { it.time }?.weight?.inKilograms
        }.getOrNull()
    }

    /** Writes a finished Spark Gym session back so the rest of the phone agrees. */
    suspend fun writeWorkout(
        startMillis: Long,
        endMillis: Long,
        title: String,
        activeCalories: Int
    ): Boolean {
        val client = clientOrNull() ?: return false
        val zone = ZoneId.systemDefault()
        val start = java.time.Instant.ofEpochMilli(startMillis)
        val end = java.time.Instant.ofEpochMilli(endMillis)
        val offset = zone.rules.getOffset(start)

        return runCatching {
            client.insertRecords(
                listOf(
                    ExerciseSessionRecord(
                        startTime = start,
                        startZoneOffset = offset,
                        endTime = end,
                        endZoneOffset = zone.rules.getOffset(end),
                        exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                        title = title
                    ),
                    ActiveCaloriesBurnedRecord(
                        startTime = start,
                        startZoneOffset = offset,
                        endTime = end,
                        endZoneOffset = zone.rules.getOffset(end),
                        energy = androidx.health.connect.client.units.Energy.kilocalories(activeCalories.toDouble())
                    )
                )
            )
            true
        }.getOrDefault(false)
    }
}
