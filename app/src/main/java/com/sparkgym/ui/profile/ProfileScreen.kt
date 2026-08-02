package com.sparkgym.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import kotlinx.coroutines.launch
import com.sparkgym.core.util.ImageUtils
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import androidx.compose.ui.platform.LocalContext
import com.sparkgym.core.util.AppLanguage
import com.sparkgym.core.util.S
import com.sparkgym.ui.common.ProfileAvatar
import com.sparkgym.domain.engine.EnergyMath
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.common.SparkTextField
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onOpenConnect: () -> Unit,
    onOpenProgressPhotos: () -> Unit,
    onOpenLicences: () -> Unit
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // One picker for the whole screen. There used to be two, writing to two
    // different preference keys, so Remove cleared one while the avatar on
    // screen read the other and nothing appeared to happen.
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                ImageUtils.saveAvatar(context, uri)?.let(viewModel::setAvatarPath)
            }
        }
    }

    val target = profile.macroTarget

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = SparkDimens.screen,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
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
            SystemPanel(title = S.profilePhoto) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        path = profile.avatarPath,
                        name = profile.name,
                        size = 76.dp,
                        onClick = {
                            avatarPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (profile.avatarPath == null) S.tapToAddPhoto else S.changePhoto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SparkColors.TextSecondary
                        )
                        if (profile.avatarPath != null) {
                            Spacer(Modifier.height(8.dp))
                            SystemButton(
                                S.removePhoto,
                                { viewModel.setAvatarPath(null) },
                                accent = SparkColors.Danger
                            )
                        }
                    }
                }
            }
        }

        item {
            SystemPanel(title = S.language) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // The photo lives in its own panel above; decoding it a
                    // second time here ran BitmapFactory on the main thread on
                    // every recomposition.
                    SparkTextField(name, { name = it }, "Hunter name", Modifier.weight(1f))
                }
                
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
            SystemButton("CONNECT DEVICES", onOpenConnect, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SystemButton("PROGRESS PHOTOS", onOpenProgressPhotos, modifier = Modifier.fillMaxWidth(), accent = SparkColors.Violet)
            Spacer(Modifier.height(10.dp))
            // Reachable from inside the app because CC BY-SA and ODbL require it.
            SystemButton(
                S.openSourceLicences.uppercase(),
                onOpenLicences,
                modifier = Modifier.fillMaxWidth(),
                accent = SparkColors.TextMuted
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
