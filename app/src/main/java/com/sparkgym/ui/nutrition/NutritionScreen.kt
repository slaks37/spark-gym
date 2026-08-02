package com.sparkgym.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.ProgressRing
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.S
import com.sparkgym.ui.common.SystemMessageDialog
import com.sparkgym.domain.model.Meal
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    onAddFood: (Meal) -> Unit,
    onOpenMealPlans: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val earned by viewModel.earned.collectAsStateWithLifecycle()

    if (earned.isNotEmpty()) {
        SystemMessageDialog(
            title = S.achievementUnlocked,
            lines = earned.flatMap { listOf(it.title, it.description, "+${it.xp} XP") },
            accent = SparkColors.Amber,
            confirmText = S.acknowledge,
            onDismiss = viewModel::dismissEarned
        )
    }
    val target = state.target

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = SparkDimens.screen,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDay(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, "Previous day", tint = SparkColors.TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FUEL", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        Dates.pretty(state.day),
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
                IconButton(
                    onClick = { viewModel.changeDay(1) },
                    enabled = state.day < Dates.today()
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        "Next day",
                        tint = if (state.day < Dates.today()) SparkColors.TextSecondary else SparkColors.TextMuted
                    )
                }
            }
        }

        item {
            SystemPanel(accent = SparkColors.Cyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = if (target.calories > 0) {
                            (state.totals.calories / target.calories).toFloat()
                        } else 0f,
                        modifier = Modifier.size(110.dp),
                        color = SparkColors.Cyan,
                        strokeWidth = 9.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.remaining.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = SparkColors.TextPrimary
                            )
                            Text("LEFT", style = SystemLabel)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BudgetRow("Goal", target.calories)
                        BudgetRow("Food", state.totals.calories.roundToInt(), SparkColors.Amber)
                        BudgetRow("Exercise", state.burnedCalories, SparkColors.Danger)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroBar(
                        "Protein", state.totals.protein, target.proteinG.toDouble(),
                        SparkColors.Protein, Modifier.weight(1f)
                    )
                    MacroBar(
                        "Carbs", state.totals.carbs, target.carbsG.toDouble(),
                        SparkColors.Carbs, Modifier.weight(1f)
                    )
                    MacroBar(
                        "Fat", state.totals.fat, target.fatG.toDouble(),
                        SparkColors.Fat, Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SystemButton(
                "Meal plans",
                onOpenMealPlans,
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accent = SparkColors.Amber,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SystemPanel(title = "Water", accent = SparkColors.Carbs) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalDrink, null, tint = SparkColors.Carbs, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        SegmentedBar(
                            progress = if (state.waterTarget > 0) state.waterMl.toFloat() / state.waterTarget else 0f,
                            color = SparkColors.Carbs,
                            segments = 16
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "${state.waterMl} / ${state.waterTarget} ml",
                            style = SystemLabel,
                            color = SparkColors.TextMuted
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    SystemButton("+250", { viewModel.addWater(250) }, accent = SparkColors.Carbs)
                }
            }
        }

        Meal.entries.forEach { meal ->
            item(key = "meal-${meal.name}") {
                val entries = state.meals[meal].orEmpty()
                val mealCalories = entries.sumOf { it.calories }.roundToInt()

                SystemPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                S.meal(meal),
                                style = MaterialTheme.typography.titleSmall,
                                color = SparkColors.TextPrimary
                            )
                            Text("$mealCalories kcal", style = SystemLabel, color = SparkColors.TextMuted)
                        }
                        Icon(
                            Icons.Filled.Add,
                            "Add food",
                            tint = SparkColors.Cyan,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onAddFood(meal) }
                        )
                    }

                    if (entries.isNotEmpty()) Spacer(Modifier.height(8.dp))

                    entries.forEach { entry ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    entry.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SparkColors.TextSecondary
                                )
                                Text(
                                    buildString {
                                        if (entry.grams > 0) append("${entry.grams.roundToInt()} g · ")
                                        append("P ${entry.protein.roundToInt()}")
                                        append("  C ${entry.carbs.roundToInt()}")
                                        append("  F ${entry.fat.roundToInt()}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.TextMuted
                                )
                            }
                            Text(
                                "${entry.calories.roundToInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SparkColors.Amber
                            )
                            IconButton(
                                onClick = { viewModel.deleteEntry(entry.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    "Remove",
                                    tint = SparkColors.TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Calories remaining = goal − food + exercise. Exercise comes from your watch, " +
                    "so it only appears once a sync has run.",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun BudgetRow(label: String, value: Int, color: Color = SparkColors.TextSecondary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label.uppercase(), style = SystemLabel)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun MacroBar(
    label: String,
    current: Double,
    target: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label.uppercase(), style = SystemLabel.copy(color = color))
        Spacer(Modifier.height(5.dp))
        SegmentedBar(
            progress = if (target > 0) (current / target).toFloat() else 0f,
            color = color,
            segments = 12,
            height = 7.dp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${current.roundToInt()} / ${target.roundToInt()} g",
            style = MaterialTheme.typography.bodySmall,
            color = SparkColors.TextMuted
        )
    }
}
