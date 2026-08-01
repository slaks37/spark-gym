package com.sparkgym.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.core.util.AppLanguage
import com.sparkgym.data.prefs.UserProfile
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.EnergyMath
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Onboarding and settings — the same fields, shown two different ways. */
class ProfileViewModel(private val container: AppContainer) : ViewModel() {

    val profile: StateFlow<UserProfile> = container.prefs.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    val hunter = container.gameRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(
        name: String,
        sex: EnergyMath.Sex,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activity: EnergyMath.ActivityLevel,
        goal: EnergyMath.Goal,
        completeOnboarding: Boolean = false
    ) {
        viewModelScope.launch {
            container.prefs.update {
                it.copy(
                    name = name.ifBlank { "Hunter" },
                    sex = sex,
                    age = age.coerceIn(13, 100),
                    heightCm = heightCm.coerceIn(120.0, 230.0),
                    weightKg = weightKg.coerceIn(30.0, 250.0),
                    activity = activity,
                    goal = goal,
                    onboarded = it.onboarded || completeOnboarding
                )
            }
            container.gameRepository.setName(name)
            container.nutritionRepository.logWeight(weightKg)
            // Targets changed, so tomorrow's quests should reflect them today.
            container.coordinator.refreshDailyBoard()
        }
    }

    fun setCalorieOverride(calories: Int?) {
        viewModelScope.launch { container.prefs.update { it.copy(calorieOverride = calories) } }
    }

    fun setProteinOverride(protein: Int?) {
        viewModelScope.launch { container.prefs.update { it.copy(proteinOverride = protein) } }
    }

    fun setRestSeconds(seconds: Int) {
        viewModelScope.launch { container.prefs.update { it.copy(defaultRestSeconds = seconds) } }
    }

    fun setUseMetric(metric: Boolean) {
        viewModelScope.launch { container.prefs.update { it.copy(useMetric = metric) } }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { container.prefs.update { it.copy(language = language) } }
    }
}
