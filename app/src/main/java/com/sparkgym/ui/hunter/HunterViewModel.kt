package com.sparkgym.ui.hunter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.WearableDayEntity
import com.sparkgym.data.prefs.UserProfile
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.model.Achievement
import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.DailyQuest
import com.sparkgym.domain.model.HunterProfile
import com.sparkgym.domain.model.Muscle
import com.sparkgym.data.repository.MacroTotals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the status window and the quest board — the two screens that make this
 * a Solo Leveling app rather than a spreadsheet.
 */
class HunterViewModel(private val container: AppContainer) : ViewModel() {

    data class DashboardState(
        val hunter: HunterProfile? = null,
        val profile: UserProfile = UserProfile(),
        val quests: List<DailyQuest> = emptyList(),
        val heat: Map<Muscle, HeatmapEngine.MuscleHeat> = emptyMap(),
        val today: WearableDayEntity? = null,
        val macros: MacroTotals = MacroTotals(),
        val workoutsCompleted: Int = 0,
        val advice: String = ""
    ) {
        val coreQuests get() = quests.filter { it.isCore }
        val supportQuests get() = quests.filter { !it.isCore }
        val questsCompleted get() = quests.count { it.completed }
        val boardCleared get() = coreQuests.isNotEmpty() && coreQuests.all { it.completed }
        val balanceScore get() = HeatmapEngine.balanceScore(heat)
    }

    /** Transient "the System has something to say" popups. */
    data class SystemMessage(val title: String, val lines: List<String>, val accentViolet: Boolean = false)

    private val _message = MutableStateFlow<SystemMessage?>(null)
    val message: StateFlow<SystemMessage?> = _message.asStateFlow()

    val state: StateFlow<DashboardState> = combine(
        container.gameRepository.observeProfile(),
        container.prefs.profile,
        container.gameRepository.observeQuests(),
        container.workoutRepository.observeHeatmap(7),
        container.wearableRepository.observeToday()
    ) { hunter, profile, quests, heat, today ->
        DashboardState(
            hunter = hunter,
            profile = profile,
            quests = quests,
            heat = heat,
            today = today
        )
    }.combine(container.nutritionRepository.observeTotals(Dates.today())) { base, macros ->
        base.copy(macros = macros)
    }.combine(container.workoutRepository.observeCompletedCount()) { base, count ->
        base.copy(workoutsCompleted = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    val achievements: StateFlow<List<Achievement>> =
        container.gameRepository.observeAchievements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            container.coordinator.refreshDailyBoard()
            refreshAdvice()
        }
    }

    private val _advice = MutableStateFlow("")
    val advice: StateFlow<String> = _advice.asStateFlow()

    private fun refreshAdvice() {
        viewModelScope.launch {
            _advice.value = container.coordinator.weakestMuscleAdvice()
        }
    }

    fun incrementQuest(quest: DailyQuest, delta: Double) {
        viewModelScope.launch {
            val before = quest.completed
            val updated = container.gameRepository.addQuestProgress(quest.id, delta)
            if (updated != null && !before && updated.completed) {
                _message.value = SystemMessage(
                    title = "Quest complete",
                    lines = listOf(
                        updated.metric.displayName,
                        "+${updated.xpReward} XP"
                    )
                )
            }
        }
    }

    fun spendPoint(attribute: Attribute) {
        viewModelScope.launch { container.gameRepository.spendPoint(attribute) }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            container.gameRepository.setName(name)
            container.prefs.update { it.copy(name = name) }
        }
    }

    fun sync() {
        viewModelScope.launch {
            val result = container.coordinator.syncWearable()
            _message.value = SystemMessage(
                title = "Sync",
                lines = listOfNotNull(
                    when (result.status) {
                        com.sparkgym.data.repository.WearableRepository.SyncStatus.SUCCESS ->
                            "${result.daysSynced} days pulled from ${result.source.replace('_', ' ')}"
                        com.sparkgym.data.repository.WearableRepository.SyncStatus.NOT_CONFIGURED ->
                            "No source connected"
                        else -> "Sync failed"
                    },
                    result.message
                )
            )
            refreshAdvice()
        }
    }

    fun dismissMessage() {
        _message.value = null
    }
}
