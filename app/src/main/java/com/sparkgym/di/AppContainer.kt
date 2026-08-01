package com.sparkgym.di

import android.content.Context
import com.sparkgym.BuildConfig
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.prefs.UserPrefs
import com.sparkgym.data.remote.FitbitApi
import com.sparkgym.data.remote.FitbitAuth
import com.sparkgym.data.remote.HealthConnectSource
import com.sparkgym.data.remote.OpenFoodFactsApi
import com.sparkgym.data.repository.BodyweightRepository
import com.sparkgym.data.repository.GameRepository
import com.sparkgym.data.repository.NutritionRepository
import com.sparkgym.data.repository.SeedRepository
import com.sparkgym.data.repository.WearableRepository
import com.sparkgym.data.repository.WorkoutRepository
import com.sparkgym.domain.SystemCoordinator

/**
 * Hand-rolled dependency graph.
 *
 * A single-module app with six repositories does not need an annotation
 * processor to wire itself, and skipping one keeps the build fast and the
 * stack traces readable.
 */
class AppContainer(private val context: Context) {

    /**
     * The Fitbit OAuth redirect arrives as an Intent on MainActivity, which is
     * nowhere near the screen that asked for it — this is the hand-off point.
     */
    val pendingFitbitRedirect = kotlinx.coroutines.flow.MutableStateFlow<android.net.Uri?>(null)

    val database: SparkGymDatabase by lazy { SparkGymDatabase.get(context) }

    val prefs: UserPrefs by lazy { UserPrefs(context) }

    val fitbitAuth: FitbitAuth by lazy { FitbitAuth(context) }

    private val fitbitApi: FitbitApi by lazy { FitbitApi.create(fitbitAuth, BuildConfig.DEBUG) }

    private val openFoodFacts: OpenFoodFactsApi by lazy {
        OpenFoodFactsApi.create("SparkGym/${BuildConfig.VERSION_NAME} (Android)")
    }

    val healthConnect: HealthConnectSource by lazy { HealthConnectSource(context) }

    val seedRepository: SeedRepository by lazy { SeedRepository(database) }

    val workoutRepository: WorkoutRepository by lazy { WorkoutRepository(database) }

    val bodyweightRepository: BodyweightRepository by lazy {
        BodyweightRepository(database, workoutRepository)
    }

    val nutritionRepository: NutritionRepository by lazy {
        NutritionRepository(database, openFoodFacts)
    }

    val gameRepository: GameRepository by lazy { GameRepository(database) }

    val wearableRepository: WearableRepository by lazy {
        WearableRepository(
            db = database,
            prefs = prefs,
            fitbitAuth = fitbitAuth,
            fitbitApiProvider = { fitbitApi },
            healthConnect = healthConnect
        )
    }

    val coordinator: SystemCoordinator by lazy {
        SystemCoordinator(
            workouts = workoutRepository,
            nutrition = nutritionRepository,
            game = gameRepository,
            wearables = wearableRepository,
            prefs = prefs
        )
    }
}
