package com.sparkgym.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.S
import com.sparkgym.ui.common.SparkTextField
import kotlin.math.roundToInt

private val AVAILABLE_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

/**
 * Plate calculator visual logic.
 * (Target Weight - Bar Weight) / 2 = weight per side.
 * Returns a list of plates needed per side.
 */
private fun calculatePlates(targetWeight: Double, barWeight: Double): List<Double> {
    var weightPerSide = (targetWeight - barWeight) / 2.0
    if (weightPerSide <= 0) return emptyList()

    val plates = mutableListOf<Double>()
    for (plate in AVAILABLE_PLATES) {
        while (weightPerSide >= plate) {
            plates.add(plate)
            weightPerSide -= plate
        }
    }
    return plates
}

@Composable
fun PlateCalculatorDialog(
    onDismiss: () -> Unit
) {
    var targetInput by remember { mutableStateOf("") }
    var barWeightInput by remember { mutableStateOf("20") } // Default Olympic bar

    val targetWeight = targetInput.toDoubleOrNull() ?: 0.0
    val barWeight = barWeightInput.toDoubleOrNull() ?: 20.0
    val platesNeeded = calculatePlates(targetWeight, barWeight)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        SystemPanel(modifier = Modifier.fillMaxWidth(), title = S.plateCalculator) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SparkTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = S.targetWeight,
                        modifier = Modifier.weight(1f)
                    )
                    SparkTextField(
                        value = barWeightInput,
                        onValueChange = { barWeightInput = it },
                        label = S.barWeight,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (targetWeight > 0) {
                    if (targetWeight <= barWeight) {
                        Text(
                            S.weightTooLight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SparkColors.Amber,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else if (platesNeeded.isEmpty()) {
                        Text(
                            S.noPlatesNeeded,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SparkColors.Cyan,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SparkColors.PanelHigh)
                                .padding(16.dp)
                        ) {
                            Text(
                                S.eachSide,
                                style = MaterialTheme.typography.labelMedium,
                                color = SparkColors.TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            // Group plates by weight to show quantity
                            val plateGroups = platesNeeded.groupBy { it }
                            
                            plateGroups.forEach { (weight, list) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(getPlateColor(weight)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            formatPlateWeight(weight),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        "× ${list.size}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = SparkColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPlateWeight(weight: Double): String {
    return if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
}

/** 
 * Returns standard plate colors (usually matching competition colors).
 * 25kg = Red, 20kg = Blue, 15kg = Yellow, 10kg = Green, 5kg = White/Gray.
 */
private fun getPlateColor(weight: Double): Color = when (weight) {
    25.0 -> Color(0xFFEF4444) // Red
    20.0 -> Color(0xFF3B82F6) // Blue
    15.0 -> Color(0xFFF59E0B) // Yellow
    10.0 -> Color(0xFF10B981) // Green
    5.0 -> Color(0xFF64748B)  // Gray
    2.5 -> Color(0xFF0F172A)  // Black
    1.25 -> Color(0xFF94A3B8) // Silver
    else -> SparkColors.TextSecondary
}
