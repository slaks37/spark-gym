package com.sparkgym.ui.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.model.Muscle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HeatmapViewModel(private val container: AppContainer) : ViewModel() {

    private val _windowDays = MutableStateFlow(7)
    val windowDays: StateFlow<Int> = _windowDays.asStateFlow()

    private val _showFront = MutableStateFlow(true)
    val showFront: StateFlow<Boolean> = _showFront.asStateFlow()

    private val _selected = MutableStateFlow<Muscle?>(null)
    val selected: StateFlow<Muscle?> = _selected.asStateFlow()

    val heat: StateFlow<Map<Muscle, HeatmapEngine.MuscleHeat>> = _windowDays
        .flatMapLatest { container.workoutRepository.observeHeatmap(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val volumeTrend = container.workoutRepository.observeVolumeTrend(28)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exercises: StateFlow<List<com.sparkgym.data.local.ExerciseWithMuscles>> =
        container.workoutRepository.observeExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setWindow(days: Int) {
        _windowDays.value = days
        viewModelScope.launch { container.prefs.update { it.copy(heatmapWindowDays = days) } }
    }

    fun flip() {
        _showFront.value = !_showFront.value
        // A muscle only exists on one side, so keep the selection honest.
        val stillVisible = _selected.value?.let {
            BodyGeometry.polysFor(it, _showFront.value).isNotEmpty()
        } ?: false
        if (!stillVisible) _selected.value = null
    }

    fun select(muscle: Muscle?) {
        _selected.value = if (_selected.value == muscle) null else muscle
    }

    /** Ranked list under the body, worst-trained first. */
    fun ranked(heat: Map<Muscle, HeatmapEngine.MuscleHeat>): List<HeatmapEngine.MuscleHeat> =
        heat.values.filter { it.muscle in BodyGeometry.drawable }.sortedByDescending { it.intensity }

    fun primaryExercisesFor(muscle: Muscle, all: List<com.sparkgym.data.local.ExerciseWithMuscles>): List<com.sparkgym.data.local.ExerciseWithMuscles> {
        val direct = all.filter { item ->
            item.muscles.any { it.muscle == muscle.name && it.contribution >= 1.0f } ||
                com.sparkgym.domain.engine.ExerciseSearch.musclesFor(item.exercise.name).contains(muscle)
        }
        return direct.ifEmpty { all.take(4) }
    }

    fun secondaryExercisesFor(muscle: Muscle, all: List<com.sparkgym.data.local.ExerciseWithMuscles>): List<com.sparkgym.data.local.ExerciseWithMuscles> {
        val synergist = all.filter { item ->
            item.muscles.any { it.muscle == muscle.name && it.contribution < 1.0f }
        }
        return synergist.ifEmpty { all.drop(4).take(4) }
    }

    fun stretchExercisesFor(muscle: Muscle, all: List<com.sparkgym.data.local.ExerciseWithMuscles>): List<com.sparkgym.data.local.ExerciseWithMuscles> {
        return all.filter { item ->
            item.exercise.equipment == com.sparkgym.domain.model.Equipment.BAND ||
                item.exercise.difficulty == com.sparkgym.domain.model.ExerciseDifficulty.BEGINNER
        }.take(4)
    }
}
