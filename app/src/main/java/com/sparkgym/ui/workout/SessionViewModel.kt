package com.sparkgym.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.data.local.ExerciseEntity
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.data.local.WorkoutSessionEntity
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.SystemCoordinator
import com.sparkgym.domain.engine.StrengthMath
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The live workout screen: the set grid, the rest timer, and the summary that
 * appears when you finish.
 */
class SessionViewModel(private val container: AppContainer) : ViewModel() {

    /** One exercise block in the logger, with its sets and reference data. */
    data class ExerciseBlock(
        val exercise: ExerciseEntity,
        val sets: List<SetLogEntity>,
        val lastTime: List<SetLogEntity>,
        val bestEstimated1Rm: Double
    ) {
        val completedSets get() = sets.count { it.isCompleted }
    }

    data class SessionState(
        val session: WorkoutSessionEntity? = null,
        val blocks: List<ExerciseBlock> = emptyList(),
        val elapsedSeconds: Int = 0,
        val loading: Boolean = true
    ) {
        val totalSets get() = blocks.sumOf { it.sets.size }
        val completedSets get() = blocks.sumOf { it.completedSets }
        val volumeKg
            get() = blocks.sumOf { block ->
                block.sets.filter { it.isCompleted }.sumOf { set ->
                    StrengthMath.setVolumeKg(
                        block.exercise.tracking,
                        set.weightKg,
                        set.reps,
                        set.durationSeconds,
                        set.distanceMeters,
                        session?.bodyweightKg ?: 70.0
                    )
                }
            }
    }

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    // -------------------------------------------------------------- timers

    data class RestTimer(val totalSeconds: Int, val remainingSeconds: Int, val running: Boolean) {
        val progress: Float get() = if (totalSeconds <= 0) 0f else remainingSeconds.toFloat() / totalSeconds
    }

    private val _rest = MutableStateFlow<RestTimer?>(null)
    val rest: StateFlow<RestTimer?> = _rest.asStateFlow()

    private val _finished = MutableStateFlow<SystemCoordinator.FinishOutcome?>(null)
    val finished: StateFlow<SystemCoordinator.FinishOutcome?> = _finished.asStateFlow()

    private val _prFlash = MutableStateFlow<String?>(null)
    val prFlash: StateFlow<String?> = _prFlash.asStateFlow()

    val activeSession: StateFlow<WorkoutSessionEntity?> =
        container.workoutRepository.observeActiveSession()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var sessionId: Long? = null

    init {
        // One second tick drives both the session clock and the rest countdown.
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val session = _state.value.session
                if (session != null && session.finishedAt == null) {
                    _state.value = _state.value.copy(
                        elapsedSeconds = ((System.currentTimeMillis() - session.startedAt) / 1000).toInt()
                    )
                }
                _rest.value?.let { timer ->
                    if (timer.running && timer.remainingSeconds > 0) {
                        _rest.value = timer.copy(remainingSeconds = timer.remainingSeconds - 1)
                    } else if (timer.running && timer.remainingSeconds <= 0) {
                        _rest.value = timer.copy(running = false)
                    }
                }
            }
        }
    }

    fun bind(id: Long) {
        if (sessionId == id) return
        sessionId = id
        viewModelScope.launch {
            container.workoutRepository.observeSets(id).collect { sets ->
                val session = container.workoutRepository.session(id)
                _state.value = _state.value.copy(
                    session = session,
                    blocks = buildBlocks(sets),
                    loading = false,
                    elapsedSeconds = session?.let {
                        ((System.currentTimeMillis() - it.startedAt) / 1000).toInt()
                    } ?: 0
                )
            }
        }
    }

    private suspend fun buildBlocks(sets: List<SetLogEntity>): List<ExerciseBlock> =
        sets.groupBy { it.exerciseId }
            .entries
            .sortedBy { entry -> entry.value.minOf { it.orderIndex } }
            .mapNotNull { (exerciseId, rows) ->
                val exercise = container.workoutRepository.exercise(exerciseId) ?: return@mapNotNull null
                ExerciseBlock(
                    exercise = exercise,
                    sets = rows.sortedBy { it.setNumber },
                    lastTime = container.workoutRepository.lastPerformance(exerciseId),
                    bestEstimated1Rm = container.workoutRepository.observePersonalRecords().first()
                        .firstOrNull { it.exerciseId == exerciseId }?.bestEstimated1RmKg ?: 0.0
                )
            }

    // ---------------------------------------------------------------- edits

    fun updateSet(set: SetLogEntity) {
        viewModelScope.launch { container.workoutRepository.updateSet(set) }
    }

    fun completeSet(set: SetLogEntity, restSeconds: Int) {
        viewModelScope.launch {
            val isPr = container.workoutRepository.completeSet(set)
            sessionId?.let { container.coordinator.onSetLogged(it) }
            if (isPr) {
                val name = _state.value.blocks.firstOrNull { it.exercise.id == set.exerciseId }?.exercise?.name
                _prFlash.value = name?.let { "New record — $it" }
            }
            startRest(restSeconds)
        }
    }

    fun uncompleteSet(set: SetLogEntity) {
        viewModelScope.launch {
            container.workoutRepository.updateSet(
                set.copy(isCompleted = false, completedAt = null, isPersonalRecord = false)
            )
        }
    }

    fun addSet(exerciseId: Long) {
        val id = sessionId ?: return
        viewModelScope.launch { container.workoutRepository.addSetRow(id, exerciseId) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { container.workoutRepository.deleteSet(setId) }
    }

    fun addExercise(exerciseId: Long) {
        val id = sessionId ?: return
        viewModelScope.launch { container.workoutRepository.addExerciseToSession(id, exerciseId) }
    }

    // ----------------------------------------------------------- rest timer

    fun startRest(seconds: Int) {
        if (seconds <= 0) return
        _rest.value = RestTimer(seconds, seconds, running = true)
    }

    fun adjustRest(deltaSeconds: Int) {
        _rest.value = _rest.value?.let {
            it.copy(remainingSeconds = (it.remainingSeconds + deltaSeconds).coerceAtLeast(0), running = true)
        }
    }

    fun stopRest() {
        _rest.value = null
    }

    fun clearPrFlash() {
        _prFlash.value = null
    }

    // -------------------------------------------------------------- finish

    fun finish() {
        val id = sessionId ?: return
        viewModelScope.launch {
            _finished.value = container.coordinator.finishWorkout(id)
            _rest.value = null

            // Mirror the session into Health Connect so the rest of the phone agrees.
            val outcome = _finished.value
            val session = container.workoutRepository.session(id)
            val finishedAt = session?.finishedAt
            if (outcome != null && session != null && finishedAt != null) {
                container.healthConnect.writeWorkout(
                    startMillis = session.startedAt,
                    endMillis = finishedAt,
                    title = session.name,
                    activeCalories = outcome.summary.estimatedCalories
                )
            }
        }
    }

    fun discard() {
        val id = sessionId ?: return
        viewModelScope.launch {
            container.workoutRepository.discardSession(id)
            sessionId = null
            _state.value = SessionState(loading = false)
        }
    }

    fun consumeFinish() {
        _finished.value = null
        sessionId = null
        _state.value = SessionState(loading = false)
    }
}
