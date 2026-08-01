package com.sparkgym

import android.app.Application
import com.sparkgym.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SparkGymApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        appScope.launch {
            // Seed first — the quest board needs the routine and food tables to
            // exist before it can reference anything.
            container.seedRepository.seedIfNeeded()
            container.coordinator.refreshDailyBoard()
        }
    }
}
