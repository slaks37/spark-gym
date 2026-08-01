package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Meal
import com.sparkgym.data.seed.SeedMealPlan
import kotlin.math.roundToInt

/**
 * Turns a meal template into *your* meal plan.
 *
 * A plan written at 2400 kcal is useless to someone who needs 1900. Rather than
 * shipping ten versions of every plan, each one carries a base calorie figure
 * and this scales the portions to whatever the user's budget actually is —
 * then tells them honestly where the result lands versus their macro targets.
 */
object MealPlanEngine {

    /** Nutrition facts for one food, per 100 g. Supplied by the repository. */
    data class FoodFacts(
        val slug: String,
        val name: String,
        val caloriesPer100: Double,
        val proteinPer100: Double,
        val carbsPer100: Double,
        val fatPer100: Double
    )

    data class ScaledItem(
        val slug: String,
        val name: String,
        val grams: Double,
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val wasScaled: Boolean
    )

    data class ScaledSlot(
        val meal: Meal,
        val title: String,
        val timing: String,
        val items: List<ScaledItem>
    ) {
        val calories: Double get() = items.sumOf { it.calories }
        val protein: Double get() = items.sumOf { it.protein }
    }

    data class ScaledPlan(
        val slug: String,
        val name: String,
        val slots: List<ScaledSlot>,
        val scaleFactor: Double,
        val targetCalories: Int,
        val targetProteinG: Int
    ) {
        val calories: Double get() = slots.sumOf { it.calories }
        val protein: Double get() = slots.sumOf { s -> s.items.sumOf { it.protein } }
        val carbs: Double get() = slots.sumOf { s -> s.items.sumOf { it.carbs } }
        val fat: Double get() = slots.sumOf { s -> s.items.sumOf { it.fat } }

        val proteinGapG: Int get() = (targetProteinG - protein).roundToInt()
        val calorieGap: Int get() = (targetCalories - calories).roundToInt()

        /** Within 5% on calories and 10 g on protein is close enough to eat. */
        val isOnTarget: Boolean
            get() = kotlin.math.abs(calorieGap) <= targetCalories * 0.05 && proteinGapG <= 10
    }

    /**
     * Scaling is deliberately simple and explainable: every non-fixed item moves
     * by the same factor. Portion sizes stay recognisable — 1.3× rice is still
     * rice — and the user can see exactly what changed and why.
     *
     * The factor is clamped so a very small or very large budget does not produce
     * a plate nobody would serve; the leftover shows up as a reported gap instead
     * of a silently absurd portion.
     */
    fun scale(
        plan: SeedMealPlan,
        targetCalories: Int,
        targetProteinG: Int,
        facts: Map<String, FoodFacts>
    ): ScaledPlan {
        val rawFactor = if (plan.baseCalories > 0) targetCalories.toDouble() / plan.baseCalories else 1.0
        val factor = rawFactor.coerceIn(0.55, 1.8)

        val slots = plan.slots.map { slot ->
            val items = slot.items.mapNotNull { seedItem ->
                val food = facts[seedItem.foodSlug] ?: return@mapNotNull null
                val grams = if (seedItem.fixed) seedItem.grams else roundPortion(seedItem.grams * factor)
                val ratio = grams / 100.0
                ScaledItem(
                    slug = seedItem.foodSlug,
                    name = food.name,
                    grams = grams,
                    calories = food.caloriesPer100 * ratio,
                    protein = food.proteinPer100 * ratio,
                    carbs = food.carbsPer100 * ratio,
                    fat = food.fatPer100 * ratio,
                    wasScaled = !seedItem.fixed && kotlin.math.abs(factor - 1.0) > 0.02
                )
            }
            ScaledSlot(slot.meal, slot.title, slot.timing, items)
        }

        return ScaledPlan(
            slug = plan.slug,
            name = plan.name,
            slots = slots,
            scaleFactor = factor,
            targetCalories = targetCalories,
            targetProteinG = targetProteinG
        )
    }

    /**
     * What to do about a shortfall. The advice is specific — "add 40 g of whey"
     * beats "eat more protein", because only one of them is actionable at 9pm.
     */
    fun adjustmentAdvice(scaled: ScaledPlan): List<String> {
        val advice = mutableListOf<String>()

        val proteinGap = scaled.proteinGapG
        if (proteinGap > 10) {
            val whey = (proteinGap / 0.82).roundToInt()
            val eggs = (proteinGap / 6.3).roundToInt()
            advice += "Protein is $proteinGap g short. Add roughly ${roundTo(whey, 5)} g of whey " +
                "(about ${whey / 30} scoops) or $eggs more eggs."
        } else if (proteinGap < -25) {
            advice += "This plan runs ${-proteinGap} g over your protein target. Harmless, but you could " +
                "trade some for carbohydrate around training if you want more energy in the gym."
        }

        val calorieGap = scaled.calorieGap
        if (calorieGap > 150) {
            advice += "Still $calorieGap kcal below your budget after scaling. Add a portion of rice or " +
                "a handful of nuts to the meal you are hungriest at."
        } else if (calorieGap < -150) {
            advice += "This lands ${-calorieGap} kcal above your budget. Trim the starch at whichever meal " +
                "is furthest from your training session."
        }

        if (scaled.scaleFactor >= 1.75) {
            advice += "Your budget is well above what this plan was written for — portions have been capped. " +
                "A mass-gain template will fit you better."
        }
        if (scaled.scaleFactor <= 0.6) {
            advice += "Your budget is well below this plan's base — portions have been capped at a sensible " +
                "floor. A cutting template will fit you better."
        }

        if (advice.isEmpty()) {
            advice += "This plan lands on your targets as written. Log it and get on with your day."
        }
        return advice
    }

    /** Round to the nearest 5 g so the plan reads like food, not like chemistry. */
    private fun roundPortion(grams: Double): Double = when {
        grams < 20 -> (grams).roundToInt().toDouble()
        grams < 100 -> roundTo(grams.roundToInt(), 5).toDouble()
        else -> roundTo(grams.roundToInt(), 10).toDouble()
    }

    private fun roundTo(value: Int, step: Int): Int = ((value + step / 2) / step) * step
}
