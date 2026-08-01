package com.sparkgym.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * The slice of the Fitbit Web API the app uses. Dates are `yyyy-MM-dd`, and
 * "-" as the user id always means the authenticated user.
 */
interface FitbitApi {

    @GET("1/user/-/activities/date/{date}.json")
    suspend fun dailyActivity(@Path("date") date: String): FitbitActivityResponse

    @GET("1/user/-/activities/heart/date/{date}/1d.json")
    suspend fun heartRate(@Path("date") date: String): FitbitHeartResponse

    @GET("1.2/user/-/sleep/date/{date}.json")
    suspend fun sleep(@Path("date") date: String): FitbitSleepResponse

    @GET("1/user/-/profile.json")
    suspend fun profile(): FitbitProfileResponse

    companion object {
        private const val BASE_URL = "https://api.fitbit.com/"

        fun create(auth: FitbitAuth, debug: Boolean): FitbitApi {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    // Tokens are fetched per request so a refresh mid-session is invisible.
                    val token = kotlinx.coroutines.runBlocking { auth.accessToken() }
                    val request = chain.request().newBuilder()
                        .apply { if (token != null) header("Authorization", "Bearer $token") }
                        .header("Accept-Language", "en_US") // forces metric-free unit ambiguity away
                        .build()
                    chain.proceed(request)
                }
                .apply {
                    if (debug) {
                        addInterceptor(
                            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                        )
                    }
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(FitbitApi::class.java)
        }
    }
}

// ---------------------------------------------------------------------------
// Response models — only the fields the app reads.
// ---------------------------------------------------------------------------

@Serializable
data class FitbitActivityResponse(
    val summary: FitbitActivitySummary = FitbitActivitySummary()
)

@Serializable
data class FitbitActivitySummary(
    val steps: Int = 0,
    @SerialName("caloriesOut") val caloriesOut: Int = 0,
    @SerialName("activityCalories") val activityCalories: Int = 0,
    @SerialName("fairlyActiveMinutes") val fairlyActiveMinutes: Int = 0,
    @SerialName("veryActiveMinutes") val veryActiveMinutes: Int = 0,
    @SerialName("lightlyActiveMinutes") val lightlyActiveMinutes: Int = 0,
    @SerialName("restingHeartRate") val restingHeartRate: Int? = null,
    val distances: List<FitbitDistance> = emptyList()
) {
    /** Fitbit reports several distance rows; "total" is the one we want, in km. */
    val totalDistanceMeters: Double
        get() = (distances.firstOrNull { it.activity == "total" }?.distance ?: 0.0) * 1000.0

    val activeMinutes: Int get() = fairlyActiveMinutes + veryActiveMinutes
}

@Serializable
data class FitbitDistance(
    val activity: String = "",
    val distance: Double = 0.0
)

@Serializable
data class FitbitHeartResponse(
    @SerialName("activities-heart") val activitiesHeart: List<FitbitHeartDay> = emptyList()
) {
    val restingHeartRate: Int? get() = activitiesHeart.firstOrNull()?.value?.restingHeartRate
}

@Serializable
data class FitbitHeartDay(
    val dateTime: String = "",
    val value: FitbitHeartValue = FitbitHeartValue()
)

@Serializable
data class FitbitHeartValue(
    @SerialName("restingHeartRate") val restingHeartRate: Int? = null
)

@Serializable
data class FitbitSleepResponse(
    val summary: FitbitSleepSummary = FitbitSleepSummary()
)

@Serializable
data class FitbitSleepSummary(
    @SerialName("totalMinutesAsleep") val totalMinutesAsleep: Int = 0,
    @SerialName("totalTimeInBed") val totalTimeInBed: Int = 0,
    @SerialName("totalSleepRecords") val totalSleepRecords: Int = 0
)

@Serializable
data class FitbitProfileResponse(
    val user: FitbitUser = FitbitUser()
)

@Serializable
data class FitbitUser(
    val displayName: String = "",
    val fullName: String = "",
    val avatar: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0
)
