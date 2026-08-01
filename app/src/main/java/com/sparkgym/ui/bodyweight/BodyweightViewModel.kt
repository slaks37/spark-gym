package com.sparkgym.ui.bodyweight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.data.local.WorkoutSessionEntity
import com.sparkgym.data.repository.BodyweightRepository
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.BodyweightEngine
import com.sparkgym.domain.model.BodyweightCircuit
import com.sparkgym.domain.model.BodyweightFocus
import com.sparkgym.domain.model.BodyweightLevel
import com.sparkgym.domain.model.ResolvedCircuit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Browsing, filtering and starting bodyweight circuits. */
class BodyweightViewModel(private val container: AppContainer) : ViewModel() {

    private val repo: BodyweightRepository = container.bodyweightRepository

    // ------------------------------------------------------------- filters

    private val _focus = MutableStateFlow<BodyweightFocus?>(null)
    val focus: StateFlow<BodyweightFocus?> = _focus.asStateFlow()

    private val _level = MutableStateFlow(BodyweightLevel.TWO)
    val level: StateFlow<BodyweightLevel> = _level.asStateFlow()

    fun setFocus(focus: BodyweightFocus?) = _focus.update { focus }

    fun setLevel(level: BodyweightLevel) = _level.update { level }

    // -------------------------------------------------------------- content

    /** Every circuit resolved at the current level, then filtered by focus. */
    val circuits: StateFlow<List<ResolvedCircuit>> =
        combine(repo.observeCircuits(), _focus, _level) { entries, focus, level ->
            entries
                .filter { focus == null || it.circuit.focus == focus }
                .map { repo.resolve(it.circuit, level) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * The daily pick, resolved at the current level. Deterministic on the date,
     * so it does not change if the screen is reopened.
     */
    val featured: StateFlow<ResolvedCircuit?> =
        combine(repo.observeCircuits(), _level) { entries, level ->
            val pick = BodyweightEngine.workoutOfTheDay(
                entries.map { it.circuit },
                com.sparkgym.core.util.Dates.today()
            )
            pick?.let { repo.resolve(it, level) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val history: StateFlow<List<WorkoutSessionEntity>> =
        repo.observeHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** How many of the last seven days had a circuit logged. */
    val weekCount: StateFlow<Int> =
        repo.observeHistory(60)
            .map { sessions ->
                val today = com.sparkgym.core.util.Dates.today()
                sessions.map { it.dateEpochDay }
                    .filter { it > today - 7 }
                    .distinct()
                    .size
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // --------------------------------------------------------------- detail

    private val _selected = MutableStateFlow<ResolvedCircuit?>(null)
    val selected: StateFlow<ResolvedCircuit?> = _selected.asStateFlow()

    /** Exercise display names for whatever circuit is open, keyed by slug. */
    private val _moveNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val moveNames: StateFlow<Map<String, String>> = _moveNames.asStateFlow()

    fun open(circuit: BodyweightCircuit) {
        _selected.value = repo.resolve(circuit, _level.value)
        viewModelScope.launch { _moveNames.value = repo.moveNames(circuit) }
    }

    fun close() {
        _selected.value = null
    }

    /** Changing the level on the detail sheet re-resolves what is on screen. */
    fun setLevelForSelection(level: BodyweightLevel) {
        _level.value = level
        _selected.update { current -> current?.let { repo.resolve(it.circuit, level) } }
    }

    /** Calories shown to the user use their real weight, not the 70 kg default. */
    val bodyweightKg: StateFlow<Double> =
        container.prefs.profile
            .map { it.weightKg }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                BodyweightEngine.DEFAULT_BODYWEIGHT_KG
            )

    // ---------------------------------------------------------------- start

    private val _startedSessionId = MutableStateFlow<Long?>(null)
    val startedSessionId: StateFlow<Long?> = _startedSessionId.asStateFlow()

    fun start(resolved: ResolvedCircuit) {
        viewModelScope.launch {
            val weight = container.prefs.profile.first().weightKg
            _startedSessionId.value =
                repo.startCircuit(resolved.circuit, resolved.level, weight)
            _selected.value = null
        }
    }

    fun consumeStartedSession() {
        _startedSessionId.value = null
    }
}
