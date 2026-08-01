package com.sparkgym.ui.connect

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.data.local.WearableDayEntity
import com.sparkgym.data.prefs.WearableSource
import com.sparkgym.data.remote.HealthConnectSource
import com.sparkgym.data.repository.WearableRepository
import com.sparkgym.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Fitbit / Health Connect screen. */
class ConnectViewModel(private val container: AppContainer) : ViewModel() {

    data class ConnectState(
        val fitbitConfigured: Boolean = false,
        val fitbitLinked: Boolean = false,
        val healthConnectAvailability: HealthConnectSource.Availability =
            HealthConnectSource.Availability.NOT_SUPPORTED,
        val healthConnectGranted: Boolean = false,
        val preferredSource: String = WearableSource.NONE,
        val syncing: Boolean = false,
        val lastResult: WearableRepository.SyncResult? = null
    )

    private val _state = MutableStateFlow(ConnectState())
    val state: StateFlow<ConnectState> = _state.asStateFlow()

    val recentDays: StateFlow<List<WearableDayEntity>> =
        container.wearableRepository.observeRange(7)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val healthConnectPermissions: Set<String> get() = container.healthConnect.permissions

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                fitbitConfigured = container.fitbitAuth.isConfigured,
                fitbitLinked = container.fitbitAuth.isLinked,
                healthConnectAvailability = container.healthConnect.availability(),
                healthConnectGranted = container.healthConnect.hasPermissions(),
                preferredSource = container.prefs.profile.first().wearableSource
            )
        }
    }

    fun linkFitbit(context: Context) {
        container.wearableRepository.beginFitbitLink(context)
    }

    /** Called from the activity when the `sparkgym://fitbit-callback` intent lands. */
    fun handleRedirect(uri: Uri) {
        viewModelScope.launch {
            val result = container.wearableRepository.completeFitbitLink(uri)
            refresh()
            if (result.isSuccess) {
                sync()
            } else {
                _state.value = _state.value.copy(
                    lastResult = WearableRepository.SyncResult(
                        WearableRepository.SyncStatus.FAILED,
                        WearableSource.FITBIT,
                        message = result.exceptionOrNull()?.message
                    )
                )
            }
        }
    }

    fun unlinkFitbit() {
        viewModelScope.launch {
            container.wearableRepository.unlinkFitbit()
            refresh()
        }
    }

    fun onHealthConnectGranted() {
        viewModelScope.launch {
            container.wearableRepository.useHealthConnect()
            refresh()
            sync()
        }
    }

    fun preferSource(source: String) {
        viewModelScope.launch {
            container.prefs.update { it.copy(wearableSource = source) }
            refresh()
        }
    }

    fun sync() {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncing = true)
            val result = container.coordinator.syncWearable()
            _state.value = _state.value.copy(syncing = false, lastResult = result)
        }
    }
}
