package com.sparkgym.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.data.seed.MealPlanSeed
import kotlin.math.roundToInt

/**
 * Meal plan browser. Every plan is shown already scaled to the user's own
 * calorie and protein targets, so what is on screen is what they would eat —
 * not a generic 2000 kcal template they have to translate in their head.
 */
@Composable
fun MealPlanScreen(
    viewModel: NutritionViewModel,
    onBack: () -> Unit
) {
    val plans by viewModel.mealPlanSummaries.collectAsStateWithLifecycle()
    val selected by viewModel.selectedPlan.collectAsStateWithLifecycle()
    val advice by viewModel.planAdvice.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
                }
                Column {
                    Text("MEAL PLANS", style = SystemLabel.copy(color = SparkColors.Amber))
                    Text(
                        "Scaled to your targets",
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        val current = selected
        if (current == null) {
            items2(plans) { summary ->
                val plan = MealPlanSeed.bySlug(summary.slug) ?: return@items2
                SystemPanel(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectPlan(summary.slug) },
                    accent = SparkColors.Amber
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                plan.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = SparkColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextMuted
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SystemChip(plan.goalTag, accent = SparkColors.Violet)
                        SystemChip("${summary.scaledCalories} kcal")
                        SystemChip("${summary.scaledProtein} g protein", accent = SparkColors.Protein)
                        SystemChip("${plan.slots.size} meals", accent = SparkColors.TextMuted)
                    }
                }
            }
        } else {
            val plan = MealPlanSeed.bySlug(current.slug)

            item {
                SystemPanel(accent = SparkColors.Amber) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                current.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = SparkColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Scaled ${(current.scaleFactor * 100).roundToInt()}% of the template",
                                style = SystemLabel,
                                color = SparkColors.TextMuted
                            )
                        }
                        SystemButton("Change", { viewModel.selectPlan(null) }, accent = SparkColors.TextMuted)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PlanMacro("${current.calories.roundToInt()}", "kcal", SparkColors.Amber)
                        PlanMacro("${current.protein.roundToInt()}", "protein", SparkColors.Protein)
                        PlanMacro("${current.carbs.roundToInt()}", "carbs", SparkColors.Carbs)
                        PlanMacro("${current.fat.roundToInt()}", "fat", SparkColors.Fat)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Target: ${current.targetCalories} kcal · ${current.targetProteinG} g protein",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextMuted
                    )
                }
            }

            plan?.let {
                item {
                    SystemPanel(title = "Coach's note", accent = SparkColors.Violet) {
                        Text(
                            it.coachNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SparkColors.TextSecondary
                        )
                    }
                }
            }

            if (advice.isNotEmpty()) {
                item {
                    SystemPanel(
                        title = "Adjustments",
                        accent = if (current.isOnTarget) SparkColors.Success else SparkColors.Cyan
                    ) {
                        advice.forEach {
                            Text(
                                "· $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextSecondary,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            items2(current.slots) { slot ->
                SystemPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                slot.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = SparkColors.TextPrimary
                            )
                            Text(
                                "${slot.meal.displayName} · ${slot.timing}",
                                style = SystemLabel,
                                color = SparkColors.TextMuted
                            )
                        }
                        Text(
                            "${slot.calories.roundToInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SparkColors.Amber
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    slot.items.forEach { food ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                food.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${food.grams.roundToInt()} g",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (food.wasScaled) SparkColors.Cyan else SparkColors.TextMuted
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "P${food.protein.roundToInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.Protein
                            )
                        }
                    }
                }
            }

            item {
                SystemButton(
                    "Log this plan to today",
                    { viewModel.applySelectedPlan(); onBack() },
                    accent = SparkColors.Success,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    "Logging a plan adds every item to today's diary. It does not clear what you have " +
                        "already eaten — delete anything that does not apply.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanMacro(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label.uppercase(), style = SystemLabel)
    }
}

/** LazyListScope.items without needing the key-based overload's import dance. */
private fun <T> androidx.compose.foundation.lazy.LazyListScope.items2(
    list: List<T>,
    content: @Composable (T) -> Unit
) = items(list.size) { index -> content(list[index]) }
