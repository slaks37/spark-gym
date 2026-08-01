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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.domain.engine.EnergyMath
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.common.SparkTextField

/**
 * Three steps, framed as the System awakening you. Everything asked for here is
 * something the app genuinely needs to compute targets — no vanity questions.
 */
@Composable
fun OnboardingScreen(viewModel: ProfileViewModel, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("172") }
    var weight by remember { mutableStateOf("70") }
    var sex by remember { mutableStateOf(EnergyMath.Sex.MALE) }
    var activity by remember { mutableStateOf(EnergyMath.ActivityLevel.MODERATE) }
    var goal by remember { mutableStateOf(EnergyMath.Goal.MAINTAIN) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("[ SYSTEM ]", style = SystemLabel.copy(color = SparkColors.Cyan))
                Spacer(Modifier.height(6.dp))
                Text(
                    when (step) {
                        0 -> "You have acquired the qualifications to be a Player."
                        1 -> "Calibrating your body."
                        else -> "Setting your objective."
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = SparkColors.TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                SegmentedBar(progress = (step + 1) / 3f, segments = 3)
            }
        }

        when (step) {
            0 -> item {
                SystemPanel(title = "Identity") {
                    Text(
                        "Every hunter needs a name. This is what the status window will show.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    SparkTextField(name, { name = it }, "Hunter name", Modifier.fillMaxWidth())
                }
            }

            1 -> item {
                SystemPanel(title = "Body") {
                    Text(
                        "Used to compute your calorie budget and to score bodyweight exercises. " +
                            "You can change any of it later.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SparkTextField(age, { age = it }, "Age", Modifier.weight(1f), KeyboardType.Number)
                        SparkTextField(height, { height = it }, "Height", Modifier.weight(1f), KeyboardType.Decimal, "cm")
                        SparkTextField(weight, { weight = it }, "Weight", Modifier.weight(1f), KeyboardType.Decimal, "kg")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EnergyMath.Sex.entries.forEach { option ->
                            SelectableChip(
                                option.name.lowercase().replaceFirstChar { it.uppercase() },
                                sex == option,
                                { sex = option }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("HOW OFTEN DO YOU TRAIN?", style = SystemLabel)
                    Spacer(Modifier.height(8.dp))
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

            else -> item {
                SystemPanel(title = "Objective", accent = SparkColors.Amber) {
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
                    Spacer(Modifier.height(14.dp))
                    val w = weight.toDoubleOrNull() ?: 70.0
                    val h = height.toDoubleOrNull() ?: 172.0
                    val a = age.toIntOrNull() ?: 25
                    val tdee = EnergyMath.tdee(sex, w, h, a, activity)
                    val target = EnergyMath.macroTarget(tdee, goal, w)
                    Text(
                        "Your daily budget: ${target.calories} kcal, ${target.proteinG} g protein, " +
                            "${target.carbsG} g carbs, ${target.fatG} g fat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.Success
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (step > 0) {
                    SystemButton("Back", { step-- }, accent = SparkColors.TextMuted, modifier = Modifier.weight(1f))
                }
                SystemButton(
                    if (step < 2) "Continue" else "Awaken",
                    {
                        if (step < 2) {
                            step++
                        } else {
                            viewModel.save(
                                name = name,
                                sex = sex,
                                age = age.toIntOrNull() ?: 25,
                                heightCm = height.toDoubleOrNull() ?: 172.0,
                                weightKg = weight.toDoubleOrNull() ?: 70.0,
                                activity = activity,
                                goal = goal,
                                completeOnboarding = true
                            )
                            onDone()
                        }
                    },
                    accent = if (step < 2) SparkColors.Cyan else SparkColors.Success,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
