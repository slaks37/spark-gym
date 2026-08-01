package com.sparkgym.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.AppLanguage
import com.sparkgym.domain.engine.EnergyMath
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.common.SparkTextField
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onOpenConnect: () -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val hunter by viewModel.hunter.collectAsStateWithLifecycle()

    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var age by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var height by remember(profile.heightCm) { mutableStateOf(profile.heightCm.roundToInt().toString()) }
    var weight by remember(profile.weightKg) { mutableStateOf(profile.weightKg.toString()) }
    var sex by remember(profile.sex) { mutableStateOf(profile.sex) }
    var activity by remember(profile.activity) { mutableStateOf(profile.activity) }
    var goal by remember(profile.goal) { mutableStateOf(profile.goal) }

    val target = profile.macroTarget

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
                    Text("PROFILE", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        hunter?.let { "Level ${it.level} · ${it.rank.label}-rank" } ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        item {
            SystemPanel(title = "Language") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        SelectableChip(
                            text = "${lang.flag}  ${lang.label}",
                            selected = profile.language == lang,
                            onClick = { viewModel.setLanguage(lang) }
                        )
                    }
                }
            }
        }

        item {
            SystemPanel(title = "You") {
                SparkTextField(name, { name = it }, "Hunter name", Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SparkTextField(age, { age = it }, "Age", Modifier.weight(1f), KeyboardType.Number)
                    SparkTextField(height, { height = it }, "Height", Modifier.weight(1f), KeyboardType.Decimal, "cm")
                    SparkTextField(weight, { weight = it }, "Weight", Modifier.weight(1f), KeyboardType.Decimal, "kg")
                }
                Spacer(Modifier.height(12.dp))
                Text("SEX (for the BMR formula)", style = SystemLabel)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergyMath.Sex.entries.forEach { option ->
                        SelectableChip(
                            option.name.lowercase().replaceFirstChar { it.uppercase() },
                            sex == option,
                            { sex = option }
                        )
                    }
                }
            }
        }

        item {
            SystemPanel(title = "Activity") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EnergyMath.ActivityLevel.entries.forEach { option ->
                        SelectableChip(
                            option.label,
                            activity == option,
                            { activity = option },
                            modifier = Modifier.fillMaxWidth(),
                            accent = SparkColors.Violet
                        )
                    }
                }
            }
        }

        item {
            SystemPanel(title = "Goal", accent = SparkColors.Amber) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EnergyMath.Goal.entries.forEach { option ->
                        SelectableChip(
                            option.label,
                            goal == option,
                            { goal = option },
                            modifier = Modifier.fillMaxWidth(),
                            accent = SparkColors.Amber
                        )
                    }
                }
            }
        }

        item {
            SystemPanel(title = "Your daily targets", accent = SparkColors.Success) {
                Text(
                    "TDEE ${profile.tdee.roundToInt()} kcal · BMI ${
                        String.format(
                            java.util.Locale.US, "%.1f",
                            EnergyMath.bmi(profile.weightKg, profile.heightCm)
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
                Spacer(Modifier.height(10.dp))
                TargetRow("Calories", "${target.calories} kcal")
                TargetRow("Protein", "${target.proteinG} g")
                TargetRow("Carbs", "${target.carbsG} g")
                TargetRow("Fat", "${target.fatG} g")
                TargetRow("Water", "${EnergyMath.waterTargetMl(profile.weightKg, true)} ml")
            }
        }

        item {
            SystemButton(
                "Save",
                {
                    viewModel.save(
                        name = name,
                        sex = sex,
                        age = age.toIntOrNull() ?: profile.age,
                        heightCm = height.toDoubleOrNull() ?: profile.heightCm,
                        weightKg = weight.toDoubleOrNull() ?: profile.weightKg,
                        activity = activity,
                        goal = goal
                    )
                },
                accent = SparkColors.Success,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SystemButton(
                "Wearables and Health Connect",
                onOpenConnect,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SystemPanel(title = "Rest timer") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60, 90, 120, 180).forEach { seconds ->
                        SelectableChip(
                            "${seconds}s",
                            profile.defaultRestSeconds == seconds,
                            { viewModel.setRestSeconds(seconds) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().height(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), style = SystemLabel)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = SparkColors.TextPrimary)
    }
}
