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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.S
import com.sparkgym.data.local.FoodEntity
import com.sparkgym.domain.model.Meal
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.common.SparkTextField
import kotlin.math.roundToInt

@Composable
fun FoodSearchScreen(
    viewModel: NutritionViewModel,
    mealKey: String,
    onDone: () -> Unit
) {
    val meal = Meal.fromKey(mealKey)
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val online by viewModel.onlineResults.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val error by viewModel.searchError.collectAsStateWithLifecycle()

    var pending by remember { mutableStateOf<FoodEntity?>(null) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SparkColors.Void)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
            }
            Column {
                Text("${S.addTo} ${S.meal(meal).uppercase()}", style = SystemLabel.copy(color = SparkColors.Cyan))
                Text("Food search", style = MaterialTheme.typography.titleMedium, color = SparkColors.TextPrimary)
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            SparkTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = "Search your food database",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip("Quick add", false, { showQuickAdd = true }, accent = SparkColors.Amber)
                SelectableChip("Create food", false, { showCustom = true }, accent = SparkColors.Violet)
                SelectableChip("Search online", false, { viewModel.searchOnline() }, accent = SparkColors.Success)
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = SparkColors.Danger, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = SparkDimens.ScreenH, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (searching) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = SparkColors.Cyan)
                    }
                }
            }

            if (online.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Icon(
                            Icons.Filled.CloudDownload,
                            null,
                            tint = SparkColors.Success,
                            modifier = Modifier.height(14.dp)
                        )
                        Spacer(Modifier.padding(2.dp))
                        Text("FROM OPEN FOOD FACTS", style = SystemLabel.copy(color = SparkColors.Success))
                    }
                }
                items(online, key = { "off-${it.barcode ?: it.name}" }) { food ->
                    FoodRow(food, isOnline = true) { pending = food }
                }
            }

            item {
                Text(
                    "YOUR DATABASE",
                    style = SystemLabel.copy(color = SparkColors.TextSecondary),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(results, key = { "local-${it.id}" }) { food ->
                FoodRow(food, isOnline = false) { pending = food }
            }
        }
    }

    pending?.let { food ->
        PortionDialog(
            food = food,
            onConfirm = { grams ->
                viewModel.logFood(food, meal, grams)
                pending = null
                onDone()
            },
            onDismiss = { pending = null }
        )
    }

    if (showQuickAdd) {
        QuickAddDialog(
            onConfirm = { label, kcal, p, c, f ->
                viewModel.quickAdd(meal, label, kcal, p, c, f)
                showQuickAdd = false
                onDone()
            },
            onDismiss = { showQuickAdd = false }
        )
    }

    if (showCustom) {
        CustomFoodDialog(
            onConfirm = { name, brand, kcal, p, c, f, servingLabel, servingGrams ->
                viewModel.createCustomFood(
                    name, brand, kcal, p, c, f, servingLabel, servingGrams, meal, servingGrams
                )
                showCustom = false
                onDone()
            },
            onDismiss = { showCustom = false }
        )
    }
}

@Composable
private fun FoodRow(food: FoodEntity, isOnline: Boolean, onClick: () -> Unit) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentPadding = PaddingValues(12.dp),
        accent = if (isOnline) SparkColors.Success else SparkColors.Cyan
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = SparkColors.TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        if (food.brand.isNotBlank()) append("${food.brand} · ")
                        append("${food.caloriesPer100.roundToInt()} kcal/100 g")
                        append(" · P ${food.proteinPer100.roundToInt()}")
                        append(" C ${food.carbsPer100.roundToInt()}")
                        append(" F ${food.fatPer100.roundToInt()}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
            }
            if (food.isCustom) SystemChip("custom", accent = SparkColors.Violet)
        }
    }
}

@Composable
private fun PortionDialog(food: FoodEntity, onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var grams by remember { mutableStateOf(food.servingGrams.roundToInt().toString()) }
    val value = grams.toDoubleOrNull() ?: 0.0
    val factor = value / 100.0

    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(modifier = Modifier.fillMaxWidth(), title = food.name) {
            Text(
                "Default portion: ${food.servingLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))
            SparkTextField(
                value = grams,
                onValueChange = { grams = it },
                label = "Amount",
                keyboardType = KeyboardType.Decimal,
                suffix = "g",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroPreview("KCAL", food.caloriesPer100 * factor, SparkColors.Amber)
                MacroPreview("P", food.proteinPer100 * factor, SparkColors.Protein)
                MacroPreview("C", food.carbsPer100 * factor, SparkColors.Carbs)
                MacroPreview("F", food.fatPer100 * factor, SparkColors.Fat)
            }
            Spacer(Modifier.height(16.dp))
            SystemButton(
                "Log it",
                { if (value > 0) onConfirm(value) },
                enabled = value > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MacroPreview(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.roundToInt().toString(), style = MaterialTheme.typography.titleSmall, color = color)
        Text(label, style = SystemLabel)
    }
}

@Composable
private fun QuickAddDialog(
    onConfirm: (String, Double, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(modifier = Modifier.fillMaxWidth(), title = "Quick add", accent = SparkColors.Amber) {
            SparkTextField(label, { label = it }, "Label", Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SparkTextField(kcal, { kcal = it }, "Calories", Modifier.fillMaxWidth(), KeyboardType.Decimal, "kcal")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SparkTextField(protein, { protein = it }, "Protein", Modifier.weight(1f), KeyboardType.Decimal, "g")
                SparkTextField(carbs, { carbs = it }, "Carbs", Modifier.weight(1f), KeyboardType.Decimal, "g")
                SparkTextField(fat, { fat = it }, "Fat", Modifier.weight(1f), KeyboardType.Decimal, "g")
            }
            Spacer(Modifier.height(16.dp))
            SystemButton(
                "Add",
                {
                    onConfirm(
                        label,
                        kcal.toDoubleOrNull() ?: 0.0,
                        protein.toDoubleOrNull() ?: 0.0,
                        carbs.toDoubleOrNull() ?: 0.0,
                        fat.toDoubleOrNull() ?: 0.0
                    )
                },
                accent = SparkColors.Amber,
                enabled = (kcal.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CustomFoodDialog(
    onConfirm: (String, String, Double, Double, Double, Double, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var servingGrams by remember { mutableStateOf("100") }

    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(modifier = Modifier.fillMaxWidth(), title = "Create food", accent = SparkColors.Violet) {
            Text(
                "Enter macros per 100 g — the app scales portions for you.",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted
            )
            Spacer(Modifier.height(10.dp))
            SparkTextField(name, { name = it }, "Name", Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SparkTextField(brand, { brand = it }, "Brand (optional)", Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SparkTextField(kcal, { kcal = it }, "Calories / 100 g", Modifier.fillMaxWidth(), KeyboardType.Decimal, "kcal")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SparkTextField(protein, { protein = it }, "Protein", Modifier.weight(1f), KeyboardType.Decimal, "g")
                SparkTextField(carbs, { carbs = it }, "Carbs", Modifier.weight(1f), KeyboardType.Decimal, "g")
                SparkTextField(fat, { fat = it }, "Fat", Modifier.weight(1f), KeyboardType.Decimal, "g")
            }
            Spacer(Modifier.height(8.dp))
            SparkTextField(
                servingGrams, { servingGrams = it }, "Default portion",
                Modifier.fillMaxWidth(), KeyboardType.Decimal, "g"
            )
            Spacer(Modifier.height(16.dp))
            SystemButton(
                "Save and log",
                {
                    val grams = servingGrams.toDoubleOrNull() ?: 100.0
                    onConfirm(
                        name,
                        brand,
                        kcal.toDoubleOrNull() ?: 0.0,
                        protein.toDoubleOrNull() ?: 0.0,
                        carbs.toDoubleOrNull() ?: 0.0,
                        fat.toDoubleOrNull() ?: 0.0,
                        "${grams.roundToInt()} g",
                        grams
                    )
                },
                accent = SparkColors.Violet,
                enabled = name.isNotBlank() && (kcal.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
