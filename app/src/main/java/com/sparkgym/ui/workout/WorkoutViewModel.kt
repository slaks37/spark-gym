package com.sparkgym.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.data.local.ExerciseWithMuscles
import com.sparkgym.data.local.PersonalRecordEntity
import com.sparkgym.data.local.PrescribedExercise
import com.sparkgym.data.local.RoutineDayEntity
import com.sparkgym.data.local.RoutineEntity
import com.sparkgym.data.local.WorkoutSessionEntity
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.ExerciseSearch
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Library browsing, routine browsing and history. The session itself lives in [SessionViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val container: AppContainer) : ViewModel() {

    // ------------------------------------------------------------- library

    data class LibraryFilters(
        val query: String = "",
        val muscle: Muscle? = null,
        val equipment: Equipment? = null,
        val favoritesOnly: Boolean = false,
        val homeOnly: Boolean = false
    )

    private val _filters = MutableStateFlow(LibraryFilters())
    val filters: StateFlow<LibraryFilters> = _filters.asStateFlow()

    val library: StateFlow<List<ExerciseWithMuscles>> =
        combine(container.workoutRepository.observeExercises(), _filters) { all, f ->
            all.mapNotNull { item ->
                val e = item.exercise
                if (f.muscle != null && item.muscles.none { it.muscle == f.muscle.name }) return@mapNotNull null
                if (f.equipment != null && e.equipment != f.equipment) return@mapNotNull null
                if (f.favoritesOnly && !e.isFavorite) return@mapNotNull null
                if (f.homeOnly && !e.equipment.isHomeFriendly) return@mapNotNull null

                // The query is matched against muscles and equipment as well as
                // the name, so "dada" finds every chest movement.
                val score = ExerciseSearch.score(
                    f.query,
                    ExerciseSearch.Candidate(
                        name = e.name,
                        equipment = e.equipment,
                        primary = item.muscles.filter { it.contribution >= 1f }
                            .mapNotNull { Muscle.fromKey(it.muscle) }.toSet(),
                        secondary = item.muscles.filter { it.contribution < 1f }
                            .mapNotNull { Muscle.fromKey(it.muscle) }.toSet()
                    )
                ) ?: return@mapNotNull null
                item to score
            }
                // Best match first, then alphabetical so the order is stable.
                .sortedWith(compareByDescending<Pair<ExerciseWithMuscles, Int>> { it.second }
                    .thenBy { it.first.exercise.name })
                .map { it.first }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(query: String) = _filters.update { it.copy(query = query) }

    /**
     * Muscles the typed text names. Surfaced as tappable chips so a search for
     * "punggung" can be promoted into a real filter in one tap.
     */
    val querySuggestions: StateFlow<List<Muscle>> = _filters
        .map { if (it.muscle == null) ExerciseSearch.musclesFor(it.query).take(4) else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun setMuscle(muscle: Muscle?) = _filters.update { it.copy(muscle = muscle) }
    fun setEquipment(equipment: Equipment?) = _filters.update { it.copy(equipment = equipment) }
    fun toggleFavoritesOnly() = _filters.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    fun toggleHomeOnly() = _filters.update { it.copy(homeOnly = !it.homeOnly) }

    fun toggleFavorite(exerciseId: Long, favorite: Boolean) {
        viewModelScope.launch { container.workoutRepository.toggleFavorite(exerciseId, favorite) }
    }

    // ------------------------------------------------------------ routines

    val folders: StateFlow<List<com.sparkgym.data.local.RoutineFolderEntity>> =
        container.workoutRepository.observeRoutineFolders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Gym programmes only — bodyweight circuits have their own screen. */
    val routines: StateFlow<List<RoutineEntity>> =
        container.workoutRepository.observeGymRoutines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedRoutineId = MutableStateFlow<Long?>(null)
    val selectedRoutineId: StateFlow<Long?> = _selectedRoutineId.asStateFlow()

    val routineDays: StateFlow<List<RoutineDayEntity>> = _selectedRoutineId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else container.workoutRepository.observeRoutineDays(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDayId = MutableStateFlow<Long?>(null)
    val selectedDayId: StateFlow<Long?> = _selectedDayId.asStateFlow()

    val dayExercises: StateFlow<List<PrescribedExercise>> = _selectedDayId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else container.workoutRepository.observePrescribed(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectRoutine(id: Long?) {
        _selectedRoutineId.value = id
        _selectedDayId.value = null
    }

    fun selectDay(id: Long?) {
        _selectedDayId.value = id
    }

    fun setActiveRoutine(id: Long) {
        viewModelScope.launch { container.prefs.update { it.copy(activeRoutineId = id) } }
    }

    // ------------------------------------------------------------- custom routine builder

    data class CustomRoutineSpec(
        val name: String,
        val notes: String,
        val days: List<CustomDaySpec>
    )

    data class CustomDaySpec(
        val name: String,
        val exercises: List<CustomExerciseSpec>
    )

    data class CustomExerciseSpec(
        val exerciseId: Long,
        val targetSets: Int
    )

    fun saveCustomRoutine(spec: CustomRoutineSpec, onDone: () -> Unit) {
        viewModelScope.launch {
            val routineId = container.workoutRepository.createCustomRoutine(spec.name, spec.notes)
            spec.days.forEachIndexed { i, day ->
                val dayId = container.workoutRepository.addDayToRoutine(routineId, day.name, i)
                day.exercises.forEachIndexed { j, ex ->
                    container.workoutRepository.addExerciseToDay(dayId, ex.exerciseId, j, ex.targetSets)
                }
            }
            onDone()
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            container.workoutRepository.deleteCustomRoutine(id)
        }
    }

    // ------------------------------------------------------------- history

    val history: StateFlow<List<WorkoutSessionEntity>> =
        container.workoutRepository.observeHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecordEntity>> =
        container.workoutRepository.observePersonalRecords()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val volumeTrend = container.workoutRepository.observeVolumeTrend(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSession: StateFlow<WorkoutSessionEntity?> =
        container.workoutRepository.observeActiveSession()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Exercise name lookup for history rows, so the UI never queries the DB itself. */
    val exerciseNames: StateFlow<Map<Long, String>> =
        container.workoutRepository.observeExercises()
            .map { list -> list.associate { it.exercise.id to it.exercise.name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // -------------------------------------------------------------- start

    private val _startedSessionId = MutableStateFlow<Long?>(null)
    val startedSessionId: StateFlow<Long?> = _startedSessionId.asStateFlow()

    fun startEmptySession() = startSession("Free session", null)

    fun startFromDay(day: RoutineDayEntity) = startSession(day.name, day.id)

    private fun startSession(name: String, dayId: Long?) {
        viewModelScope.launch {
            val weight = container.prefs.profile.first().weightKg
            _startedSessionId.value = container.workoutRepository.startSession(name, dayId, weight)
        }
    }

    fun consumeStartedSession() {
        _startedSessionId.value = null
    }
}
