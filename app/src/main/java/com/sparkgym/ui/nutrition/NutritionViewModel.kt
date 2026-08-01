package com.sparkgym.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.DiaryEntryEntity
import com.sparkgym.data.local.FoodEntity
import com.sparkgym.data.prefs.UserProfile
import com.sparkgym.data.repository.MacroTotals
import com.sparkgym.data.repository.Meal
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.EnergyMath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The MyFitnessPal half: diary, food search, water and the calorie budget. */
@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModel(private val container: AppContainer) : ViewModel() {

    private val _day = MutableStateFlow(Dates.today())
    val day: StateFlow<Long> = _day.asStateFlow()

    data class DiaryState(
        val day: Long = Dates.today(),
        val meals: Map<Meal, List<DiaryEntryEntity>> = emptyMap(),
        val totals: MacroTotals = MacroTotals(),
        val waterMl: Int = 0,
        val profile: UserProfile = UserProfile(),
        val burnedCalories: Int = 0
    ) {
        val target: EnergyMath.MacroTarget get() = profile.macroTarget

        /** MyFitnessPal's headline number: goal − eaten + exercise. */
        val remaining: Int get() = target.calories - totals.calories.toInt() + burnedCalories

        val waterTarget: Int get() = EnergyMath.waterTargetMl(profile.weightKg, burnedCalories > 150)
    }

    val state: StateFlow<DiaryState> = _day.flatMapLatest { day ->
        combine(
            container.nutritionRepository.observeDiary(day),
            container.nutritionRepository.observeTotals(day),
            container.nutritionRepository.observeWater(day),
            container.prefs.profile,
            container.wearableRepository.observeToday()
        ) { meals, totals, water, profile, wearable ->
            DiaryState(
                day = day,
                meals = meals,
                totals = totals,
                waterMl = water,
                profile = profile,
                burnedCalories = wearable?.activeCalories ?: 0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DiaryState())

    fun changeDay(delta: Long) {
        _day.value = (_day.value + delta).coerceAtMost(Dates.today())
    }

    // ----------------------------------------------------------- searching

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<FoodEntity>> = _query
        .flatMapLatest { container.nutritionRepository.searchFoods(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _onlineResults = MutableStateFlow<List<FoodEntity>>(emptyList())
    val onlineResults: StateFlow<List<FoodEntity>> = _onlineResults.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
        if (value.isBlank()) _onlineResults.value = emptyList()
    }

    /** Explicit rather than automatic: nobody wants a network call per keystroke. */
    fun searchOnline() {
        val q = _query.value.trim()
        if (q.length < 3) {
            _searchError.value = "Type at least three characters"
            return
        }
        viewModelScope.launch {
            _searching.value = true
            _searchError.value = null
            container.nutritionRepository.searchOnline(q)
                .onSuccess { _onlineResults.value = it }
                .onFailure { _searchError.value = it.message ?: "Search failed" }
            _searching.value = false
        }
    }

    fun lookupBarcode(barcode: String, meal: Meal, grams: Double = 100.0) {
        viewModelScope.launch {
            container.nutritionRepository.lookupBarcode(barcode)
                .onSuccess { logFood(it, meal, grams) }
                .onFailure { _searchError.value = it.message ?: "Barcode not found" }
        }
    }

    // ------------------------------------------------------------- logging

    fun logFood(food: FoodEntity, meal: Meal, grams: Double) {
        viewModelScope.launch {
            val saved = if (food.id == 0L) {
                val id = container.nutritionRepository.saveFood(food)
                food.copy(id = id)
            } else food
            container.nutritionRepository.logFood(_day.value, meal, saved, grams)
            container.coordinator.onFoodLogged()
        }
    }

    fun quickAdd(meal: Meal, label: String, calories: Double, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            container.nutritionRepository.quickAdd(
                _day.value, meal, label, MacroTotals(calories, protein, carbs, fat)
            )
            container.coordinator.onFoodLogged()
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            container.nutritionRepository.deleteEntry(id)
            container.coordinator.onFoodLogged()
        }
    }

    fun addWater(ml: Int) {
        viewModelScope.launch {
            container.nutritionRepository.addWater(_day.value, ml)
            container.coordinator.onWaterLogged()
        }
    }

    fun createCustomFood(
        name: String,
        brand: String,
        caloriesPer100: Double,
        proteinPer100: Double,
        carbsPer100: Double,
        fatPer100: Double,
        servingLabel: String,
        servingGrams: Double,
        meal: Meal?,
        gramsToLog: Double
    ) {
        viewModelScope.launch {
            val id = container.nutritionRepository.createCustomFood(
                name, brand, caloriesPer100, proteinPer100, carbsPer100, fatPer100, servingLabel, servingGrams
            )
            if (meal != null) {
                container.nutritionRepository.food(id)?.let {
                    container.nutritionRepository.logFood(_day.value, meal, it, gramsToLog)
                    container.coordinator.onFoodLogged()
                }
            }
        }
    }

    fun clearError() {
        _searchError.value = null
    }

    // ------------------------------------------------------------ progress

    val weeklyTotals = container.nutritionRepository
        .observeDailyTotals(Dates.today() - 6, Dates.today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bodyMetrics = container.nutritionRepository.observeBodyMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun logWeight(weightKg: Double, bodyFat: Double?) {
        viewModelScope.launch {
            container.nutritionRepository.logWeight(weightKg, bodyFat)
            container.prefs.update { it.copy(weightKg = weightKg) }
        }
    }

    suspend fun currentProfile(): UserProfile = container.prefs.profile.first()
}
