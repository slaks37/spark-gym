package com.sparkgym.data.repository

import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.BodyMetricEntity
import com.sparkgym.data.local.DiaryEntryEntity
import com.sparkgym.data.local.FoodEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.local.WaterLogEntity
import com.sparkgym.data.seed.SeedMealPlan
import com.sparkgym.domain.engine.MealPlanEngine
import com.sparkgym.data.remote.OffProduct
import com.sparkgym.data.remote.OpenFoodFactsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Meal(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snacks");

    companion object {
        fun fromKey(key: String): Meal = entries.firstOrNull { it.name == key } ?: SNACK
    }
}

data class MacroTotals(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
) {
    operator fun plus(other: MacroTotals) = MacroTotals(
        calories + other.calories,
        protein + other.protein,
        carbs + other.carbs,
        fat + other.fat
    )
}

class NutritionRepository(
    private val db: SparkGymDatabase,
    private val offApi: OpenFoodFactsApi
) {

    private val dao get() = db.nutritionDao()

    // ------------------------------------------------------------------ food

    fun searchFoods(query: String): Flow<List<FoodEntity>> =
        if (query.isBlank()) dao.observeTopFoods() else dao.searchFoods(query.trim())

    suspend fun food(id: Long) = dao.food(id)

    suspend fun toggleFavorite(id: Long, favorite: Boolean) = dao.setFoodFavorite(id, favorite)

    suspend fun createCustomFood(
        name: String,
        brand: String,
        caloriesPer100: Double,
        proteinPer100: Double,
        carbsPer100: Double,
        fatPer100: Double,
        servingLabel: String,
        servingGrams: Double
    ): Long = dao.insertFood(
        FoodEntity(
            name = name,
            brand = brand,
            caloriesPer100 = caloriesPer100,
            proteinPer100 = proteinPer100,
            carbsPer100 = carbsPer100,
            fatPer100 = fatPer100,
            servingLabel = servingLabel,
            servingGrams = servingGrams,
            isCustom = true,
            source = "custom"
        )
    )

    /**
     * Barcode lookup: local cache first, then Open Food Facts. Anything found
     * online is written to the local table so the second scan is instant and
     * works offline.
     */
    suspend fun lookupBarcode(barcode: String): Result<FoodEntity> {
        dao.foodByBarcode(barcode)?.let { return Result.success(it) }
        return runCatching {
            val response = offApi.product(barcode)
            val product = response.product?.takeIf { it.isUsable }
                ?: error("No usable nutrition data for barcode $barcode")
            val entity = product.toEntity()
            val id = dao.insertFood(entity)
            entity.copy(id = id)
        }
    }

    suspend fun searchOnline(query: String): Result<List<FoodEntity>> = runCatching {
        offApi.search(query).products
            .filter { it.isUsable }
            .map { it.toEntity() }
    }

    /** Persists an online result the user actually picked. */
    suspend fun saveFood(food: FoodEntity): Long = dao.insertFood(food)

    private fun OffProduct.toEntity() = FoodEntity(
        name = productName.trim(),
        brand = brands.split(",").firstOrNull()?.trim().orEmpty(),
        caloriesPer100 = nutriments.energyKcal100g,
        proteinPer100 = nutriments.proteins100g,
        carbsPer100 = nutriments.carbohydrates100g,
        fatPer100 = nutriments.fat100g,
        fiberPer100 = nutriments.fiber100g,
        sugarPer100 = nutriments.sugars100g,
        sodiumMgPer100 = nutriments.sodium100g * 1000,
        servingLabel = servingSize.ifBlank { "100 g" },
        servingGrams = servingQuantity?.takeIf { it > 0 } ?: 100.0,
        barcode = code.ifBlank { null },
        source = "openfoodfacts"
    )

    // ----------------------------------------------------------------- diary

    fun observeDiary(day: Long): Flow<Map<Meal, List<DiaryEntryEntity>>> =
        dao.observeDiary(day).map { entries ->
            Meal.entries.associateWith { meal ->
                entries.filter { Meal.fromKey(it.meal) == meal }
            }
        }

    fun observeTotals(day: Long): Flow<MacroTotals> =
        dao.observeDiary(day).map { entries ->
            entries.fold(MacroTotals()) { acc, e ->
                acc + MacroTotals(e.calories, e.protein, e.carbs, e.fat)
            }
        }

    fun observeDailyTotals(fromDay: Long, toDay: Long) = dao.observeDailyTotals(fromDay, toDay)

    suspend fun logFood(day: Long, meal: Meal, food: FoodEntity, grams: Double) {
        val factor = grams / 100.0
        dao.insertEntry(
            DiaryEntryEntity(
                dateEpochDay = day,
                meal = meal.name,
                foodId = food.id.takeIf { it > 0 },
                name = food.name,
                brand = food.brand,
                grams = grams,
                calories = food.caloriesPer100 * factor,
                protein = food.proteinPer100 * factor,
                carbs = food.carbsPer100 * factor,
                fat = food.fatPer100 * factor
            )
        )
    }

    /** Free-text quick add for when you know the numbers but not the food. */
    suspend fun quickAdd(day: Long, meal: Meal, label: String, totals: MacroTotals) {
        dao.insertEntry(
            DiaryEntryEntity(
                dateEpochDay = day,
                meal = meal.name,
                foodId = null,
                name = label.ifBlank { "Quick add" },
                brand = "",
                grams = 0.0,
                calories = totals.calories,
                protein = totals.protein,
                carbs = totals.carbs,
                fat = totals.fat
            )
        )
    }

    suspend fun updateEntry(entry: DiaryEntryEntity) = dao.updateEntry(entry)

    suspend fun deleteEntry(id: Long) = dao.deleteEntry(id)

    suspend fun proteinOn(day: Long) = dao.proteinOn(day)

    suspend fun loggedDayCount(fromDay: Long, toDay: Long) = dao.loggedDayCount(fromDay, toDay)

    // ------------------------------------------------------------ meal plans

    /** Nutrition facts for the foods a plan references, keyed by slug. */
    suspend fun factsForPlan(plan: SeedMealPlan): Map<String, MealPlanEngine.FoodFacts> {
        val slugs = plan.slots.flatMap { slot -> slot.items.map { it.foodSlug } }.distinct()
        return dao.foodsBySlugs(slugs).mapNotNull { food ->
            food.slug?.let { slug ->
                slug to MealPlanEngine.FoodFacts(
                    slug = slug,
                    name = food.name,
                    caloriesPer100 = food.caloriesPer100,
                    proteinPer100 = food.proteinPer100,
                    carbsPer100 = food.carbsPer100,
                    fatPer100 = food.fatPer100
                )
            }
        }.toMap()
    }

    /**
     * Writes a scaled plan into the diary. Existing entries for the day are left
     * alone — applying a plan adds to what you ate, it does not pretend the
     * morning did not happen.
     */
    suspend fun applyMealPlan(day: Long, scaled: MealPlanEngine.ScaledPlan) {
        for (slot in scaled.slots) {
            for (item in slot.items) {
                val food = dao.foodBySlug(item.slug)
                dao.insertEntry(
                    DiaryEntryEntity(
                        dateEpochDay = day,
                        meal = slot.meal.name,
                        foodId = food?.id,
                        name = item.name,
                        brand = food?.brand.orEmpty(),
                        grams = item.grams,
                        calories = item.calories,
                        protein = item.protein,
                        carbs = item.carbs,
                        fat = item.fat
                    )
                )
            }
        }
    }

    suspend fun entryCountOn(day: Long) = dao.entryCountOn(day)

    // ----------------------------------------------------------------- water

    fun observeWater(day: Long): Flow<Int> = dao.observeWater(day).map { it?.milliliters ?: 0 }

    suspend fun addWater(day: Long, milliliters: Int) {
        val existing = dao.water(day)
        dao.upsertWater(
            WaterLogEntity(
                id = existing?.id ?: 0,
                dateEpochDay = day,
                milliliters = ((existing?.milliliters ?: 0) + milliliters).coerceAtLeast(0)
            )
        )
    }

    // ------------------------------------------------------------ body stats

    fun observeBodyMetrics() = dao.observeBodyMetrics()

    suspend fun latestWeight(): Double? = dao.latestBodyMetric()?.weightKg

    suspend fun logWeight(weightKg: Double, bodyFat: Double? = null, day: Long = Dates.today()) {
        // The unique index is on the date, but @Upsert falls back to an update by
        // primary key — so carry the existing row's id or the second weigh-in of
        // the day would be silently dropped.
        val existing = dao.bodyMetric(day)
        dao.upsertBodyMetric(
            BodyMetricEntity(
                id = existing?.id ?: 0,
                dateEpochDay = day,
                weightKg = weightKg,
                bodyFatPercent = bodyFat
            )
        )
    }
}
