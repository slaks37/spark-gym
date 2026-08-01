package com.sparkgym.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.CoachEngine
import com.sparkgym.domain.engine.ProgressionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The Coach tab. Everything here is derived on demand rather than observed,
 * because the analysis is expensive and only needs to be right when you look
 * at it — not on every set you log.
 */
class CoachViewModel(private val container: AppContainer) : ViewModel() {

    data class CoachState(
        val loading: Boolean = true,
        val insights: List<CoachEngine.Insight> = emptyList(),
        val snapshot: CoachEngine.Snapshot? = null,
        val deload: ProgressionEngine.DeloadVerdict? = null
    ) {
        val headline: String get() = CoachEngine.headline(insights)
        val actionable get() = insights.filter { it.priority != CoachEngine.Priority.PRAISE }
        val wins get() = insights.filter { it.priority == CoachEngine.Priority.PRAISE }
    }

    private val _state = MutableStateFlow(CoachState())
    val state: StateFlow<CoachState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val snapshot = container.coordinator.buildCoachSnapshot()
            _state.value = CoachState(
                loading = false,
                insights = CoachEngine.analyse(snapshot),
                snapshot = snapshot,
                deload = ProgressionEngine.deloadCheck(
                    weeklyEffectiveSets = snapshot.weeklyEffectiveSets,
                    avgSleepMinutes = snapshot.avgSleepMinutes,
                    consecutiveTrainingWeeks = snapshot.consecutiveTrainingWeeks,
                    stalledLifts = snapshot.stalledLifts.size,
                    restingHrTrend = snapshot.restingHrDelta
                )
            )
        }
    }
}
