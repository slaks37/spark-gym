package com.sparkgym.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemPanel

/**
 * The "[System]" pop-up. Deliberately not a Material AlertDialog — the whole
 * point is that it looks like a game notification.
 */
@Composable
fun SystemMessageDialog(
    title: String,
    lines: List<String>,
    onDismiss: () -> Unit,
    accent: Color = SparkColors.Cyan,
    confirmText: String = "Acknowledge",
    secondaryText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(accent = accent, modifier = Modifier.fillMaxWidth()) {
            Text(
                "[ $title ]".uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
            Spacer(Modifier.height(12.dp))
            lines.forEach {
                Text(it, color = SparkColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (secondaryText != null && onSecondaryAction != null) {
                    SystemButton(secondaryText, onSecondaryAction, accent = SparkColors.Violet, modifier = Modifier.weight(1f))
                }
                SystemButton(confirmText, onDismiss, accent = accent, modifier = if (secondaryText != null) Modifier.weight(1f) else Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    accent: Color = SparkColors.Danger
) {
    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(accent = accent, modifier = Modifier.fillMaxWidth()) {
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, color = accent)
            Spacer(Modifier.height(10.dp))
            Text(body, color = SparkColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton("Cancel", onDismiss, accent = SparkColors.TextMuted, modifier = Modifier.weight(1f))
                SystemButton(confirmText, onConfirm, accent = accent, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Themed text field so no screen has to repeat the colour plumbing. */
@Composable
fun SparkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        suffix = suffix?.let { text -> { Text(text, color = SparkColors.TextMuted) } },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SparkColors.Cyan,
            unfocusedBorderColor = SparkColors.Divider,
            focusedLabelColor = SparkColors.Cyan,
            unfocusedLabelColor = SparkColors.TextMuted,
            focusedTextColor = SparkColors.TextPrimary,
            unfocusedTextColor = SparkColors.TextPrimary,
            cursorColor = SparkColors.Cyan,
            focusedContainerColor = SparkColors.Panel,
            unfocusedContainerColor = SparkColors.Panel
        )
    )
}

/** Compact numeric field used in the set grid — no label, centred, tap to edit. */
@Composable
fun NumberCell(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SparkColors.Cyan,
            unfocusedBorderColor = SparkColors.Divider,
            disabledBorderColor = SparkColors.Divider.copy(alpha = 0.4f),
            focusedTextColor = SparkColors.TextPrimary,
            unfocusedTextColor = SparkColors.TextPrimary,
            disabledTextColor = SparkColors.TextMuted,
            cursorColor = SparkColors.Cyan,
            focusedContainerColor = SparkColors.Panel,
            unfocusedContainerColor = SparkColors.Panel,
            disabledContainerColor = SparkColors.PanelHigh
        )
    )
}

/** A tappable filter chip. */
@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.9f) else SparkColors.PanelHigh)
            .border(1.dp, if (selected) accent else SparkColors.Divider, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text.uppercase(),
            color = if (selected) Color.White else SparkColors.TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** Horizontal row of selectable chips with an "all" reset, used by every filter. */
@Composable
fun <T> ChipRow(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    allLabel: String = "All",
    accent: Color = SparkColors.Cyan
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            SelectableChip(allLabel, selected == null, { onSelect(null) }, accent = accent)
        }
        items(options.size) { index ->
            val option = options[index]
            SelectableChip(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(if (option == selected) null else option) },
                accent = accent
            )
        }
    }
}
