package com.sparkgym.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.foundation.Canvas

/**
 * A rectangle with its corners sliced off — the silhouette every System dialog uses.
 */
class CutCornerAngularShape(private val cutDp: Float = 14f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { cutDp.dp.toPx() }.coerceAtMost(size.minDimension / 2f)
        val path = Path().apply {
            moveTo(c, 0f)
            lineTo(size.width - c, 0f)
            lineTo(size.width, c)
            lineTo(size.width, size.height - c)
            lineTo(size.width - c, size.height)
            lineTo(c, size.height)
            lineTo(0f, size.height - c)
            lineTo(0f, c)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * The workhorse container. Everything the System says to you sits inside one of these.
 */
@Composable
fun SystemPanel(
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan,
    title: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = CutCornerAngularShape()
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SparkColors.Panel,
                        SparkColors.PanelHigh.copy(alpha = 0.5f)
                    )
                )
            )
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.25f)), shape)
            .padding(contentPadding)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(width = 3.dp, height = 12.dp)
                            .background(accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(title.uppercase(), style = SystemLabel.copy(color = accent))
                }
                trailing?.invoke()
            }
            Spacer(Modifier.height(12.dp))
        }
        content()
    }
}

/**
 * Segmented XP / progress bar. Segments read as "notches" rather than a smooth fill,
 * which is what makes it feel like a game meter instead of a download.
 */
@Composable
fun SegmentedBar(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 24,
    color: Color = SparkColors.Cyan,
    trackColor: Color = SparkColors.Divider.copy(alpha = 0.35f),
    height: androidx.compose.ui.unit.Dp = 10.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "segmented-bar"
    )
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val gap = size.width * 0.006f
        val segW = (size.width - gap * (segments - 1)) / segments
        val lit = animated * segments
        for (i in 0 until segments) {
            val fill = (lit - i).coerceIn(0f, 1f)
            val x = i * (segW + gap)
            drawRect(
                color = trackColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                size = Size(segW, size.height)
            )
            if (fill > 0f) {
                drawRect(
                    color = color.copy(alpha = 0.45f + 0.55f * fill),
                    topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                    size = Size(segW * fill, size.height)
                )
            }
        }
    }
}

/** Big number + caption, the unit every dashboard grid is made of. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan,
    icon: ImageVector? = null
) {
    SystemPanel(
        modifier = modifier,
        accent = accent,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label.uppercase(), style = SystemLabel)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = SparkColors.TextPrimary
        )
    }
}

/** Angular primary action. Disabled state dims rather than greys, to stay in theme. */
@Composable
fun SystemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val shape = CutCornerAngularShape(10f)
    val alpha = if (enabled) 1f else 0.35f
    Row(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.10f * alpha))
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.7f * alpha)), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent.copy(alpha = alpha), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text.uppercase(),
            color = accent.copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** Small status pill: rank badges, streak counters, "PR" flags. */
@Composable
fun SystemChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan,
    filled: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (filled) accent.copy(alpha = 0.9f) else accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text.uppercase(),
            color = if (filled) Color.White else accent,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** Circular macro / calorie ring with an optional second arc for "already burned". */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SparkColors.Cyan,
    trackColor: Color = SparkColors.Divider,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    overflowColor: Color = SparkColors.Danger,
    content: @Composable () -> Unit = {}
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceAtLeast(0f),
        animationSpec = tween(700),
        label = "ring"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated.coerceAtMost(1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
            )
            if (animated > 1f) {
                drawArc(
                    color = overflowColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (animated - 1f).coerceAtMost(1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
            }
        }
        content()
    }
}

/** Section heading used between panels on long scrolling screens. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text.uppercase(), style = SystemLabel.copy(color = SparkColors.TextSecondary))
        trailing?.invoke()
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = SparkColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
